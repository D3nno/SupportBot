package de.nachcrew;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import de.nachcrew.channel.ChannelProvider;
import de.nachcrew.event.EventListener;
import de.nachcrew.thread.ConsoleThread;
import de.nachcrew.thread.SupportChannelCheckerThread;

import java.io.IOException;
import java.util.ArrayList;

public class SupportBot {

    public static final TS3Config config = new TS3Config();
    public static TS3Query query;
    public static TS3Api api;

    public static ArrayList<Integer> onlineSups = new ArrayList<>();

    public static void main(String[] args) {
        config.setHost("IP-SETZEN");
        config.setEnableCommunicationsLogging(true);
        config.setFloodRate(TS3Query.FloodRate.UNLIMITED);
        query = new TS3Query(config);
        query.connect();
        api = query.getApi();
        api.login("username", "password");
        api.selectVirtualServerByPort(9987);
        api.setNickname("TSBot");
        new SupportChannelCheckerThread().start();
        final ChannelProvider channelProvider = new ChannelProvider();
        final EventListener eventListener = new EventListener(channelProvider);
        eventListener.loadEvents();
        System.out.println("Der Bot ist nun erfolgreich gestartet!");

        api.getChannels().forEach(channel -> {
            System.out.println(channel.getName() + " -> " + channel.getOrder());
        });

        api.getClients().forEach(channel -> {
            System.out.println(channel.getNickname() + " -> " + channel.getNickname());
        });

        api.getChannels().stream().filter(channel -> channel.getParentChannelId() == 31 && channel.getTotalClients() == 0).forEach(channel -> api.deleteChannel(channel.getId(), true));

        try {
            new ConsoleThread(channelProvider).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}