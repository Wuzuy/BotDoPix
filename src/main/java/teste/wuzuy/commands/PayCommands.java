package teste.wuzuy.commands;

import com.google.zxing.WriterException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import teste.wuzuy.MercadoPagoIntegration;
import teste.wuzuy.QRCodeGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

public class PayCommands extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase("!pay")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Pagamento")
                    .setDescription("Clique no botão abaixo para pagar via Pix!")
                    .setColor(java.awt.Color.GREEN);

            event.getChannel().sendMessageEmbeds(embed.build())
                    .addActionRow(Button.of(ButtonStyle.PRIMARY, "pay_pix", "Me pague!"))
                    .queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (Objects.requireNonNull(event.getButton().getId()).equals("pay_pix")) {
            String pixCode = MercadoPagoIntegration.createPixPayment();

            try {
                byte[] qrCodeImage = QRCodeGenerator.generateQRCodeImage(pixCode, 400, 400);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(qrCodeImage);

                MessageChannel channel = event.getChannel();
                channel.sendMessage("Escaneie o QR code para efetuar o pagamento:")
                        .addFiles(Collections.singletonList(FileUpload.fromData(inputStream, "qrcode.png")))
                        .queue();

            } catch (WriterException | IOException e) {
                event.reply("Erro ao gerar o QR code. Tente novamente mais tarde.").queue();
                e.printStackTrace();
            }
        }
    }
}