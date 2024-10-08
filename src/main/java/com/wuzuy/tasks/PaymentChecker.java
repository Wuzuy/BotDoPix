package com.wuzuy.tasks;

import com.google.gson.JsonObject;
import com.wuzuy.database.DatabaseManager;
import com.wuzuy.models.PaymentData;
import com.wuzuy.pix.PixApiClient;
import com.wuzuy.pix.TokenGenerator;
import com.wuzuy.telegram.TelegramBot;
import net.dv8tion.jda.api.JDA;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PaymentChecker {

    private final ScheduledExecutorService scheduler;
    private final JDA jda;
    private final TelegramBot telegramBot;

    public PaymentChecker(ScheduledExecutorService scheduler, JDA jda, TelegramBot telegramBot) {
        this.scheduler = scheduler;
        this.jda = jda;
        this.telegramBot = telegramBot;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkPayments, 0, 1, TimeUnit.SECONDS);
    }

    private void checkPayments() {
        try {
            List<PaymentData> pendingPayments = DatabaseManager.getPaymentsToCheck();

            if (pendingPayments.isEmpty()) {
                return;
            }

            String token = TokenGenerator.getAccessToken();

            for (PaymentData payment : pendingPayments) {
                JsonObject response = PixApiClient.consultarCobrancaPix(token, payment.getIdCompra());

                if (response != null && response.has("status")) {
                    String status = response.get("status").getAsString();

                    if (status.equalsIgnoreCase("CONCLUIDA")) {
                        DatabaseManager.updatePagamentoStatus(payment.getUniqueId(), "Concluída");

                        if (payment.getMessageSent() == 0) {
                            sendProductMessage(payment);
                            DatabaseManager.updateMessageSentStatus(payment.getUniqueId(), 1);
                        }
                    } else if (status.equalsIgnoreCase("REMOVIDA_PELO_USUARIO_RECEBEDOR") ||
                            status.equalsIgnoreCase("EXPIRADA")) {
                        DatabaseManager.updatePagamentoStatus(payment.getUniqueId(), status);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendProductMessage(PaymentData payment) {
        try {
            System.out.println("Compra confirmada! \nUser ID: " + payment.getUserId()+"\n");

            jda.retrieveUserById(payment.getUserId()).queue(user -> {
                if (user != null) {
                    String linkProduto = telegramBot.generateUniqueInviteLink();

                    if (linkProduto != null) {
                        user.openPrivateChannel().queue(channel -> {
                            channel.sendMessage("Obrigado pelo seu pagamento! Aqui está o seu produto: " + linkProduto).queue();
                        });
                    } else {
                        user.openPrivateChannel().queue(channel -> {
                            channel.sendMessage("Obrigado pelo seu pagamento! Porém, ocorreu um erro ao gerar o link do produto. Por favor, entre em contato com o suporte.").queue();
                        });
                    }
                } else {
                    System.out.println("Usuário não encontrado: " + payment.getUserId());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
