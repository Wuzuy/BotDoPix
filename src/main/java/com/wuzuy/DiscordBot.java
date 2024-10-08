package com.wuzuy;

import com.wuzuy.commands.PayCommands;
import com.wuzuy.database.DatabaseManager;
import com.wuzuy.tasks.PaymentChecker;
import com.wuzuy.telegram.TelegramBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class DiscordBot {

    public static JDA jda;
    public static TelegramBot telegramBot;


    public static void main(String[] args) throws InterruptedException {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        jda = JDABuilder.createDefault("BOT_DS_TOKEN")
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .build();

        jda.addEventListener(
                new PayCommands()
        );

        jda.awaitReady();

        try {
            String botToken = "BOT_TL_TOKEN";
            String botUsername = "NAME_BOT";
            String groupChatId = "ID_CHANNEL";

            telegramBot = new TelegramBot(botToken, botUsername, groupChatId);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);

        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseManager.initializeDatabase();
        System.out.println("Bot está pronto e conectado!");

        PaymentChecker paymentChecker = new PaymentChecker(scheduler, jda, telegramBot);
        paymentChecker.start();
    }
}