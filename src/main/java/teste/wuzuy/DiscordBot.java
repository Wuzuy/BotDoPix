package teste.wuzuy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import teste.wuzuy.commands.PayCommands;


public class DiscordBot {

    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        jda = JDABuilder.createDefault("MTI1Mzg0OTUzNjA5NTU4NDM4MA.GNVWno.DSuWUAj0Yj00rz-5inGlqjRx0sE4m2aEkY9LoY")
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .build();

        jda.addEventListener(
                new PayCommands()
        );

        jda.awaitReady();
    }
}
