package de.nachcrew.thread;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.nachcrew.SupportBot;
import de.nachcrew.utils.ClientUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportChannelCheckerThread extends Thread{

    private static final int SUPPORT_WAITING_CHANNEL_ID = 31;

    @Override
    public void run() {
        while (!interrupted()) {
            final List<Client> onlineSupporters = ClientUtils.getOnlineSupporters(false);

            Map<ChannelProperty, String> property = new HashMap<>();
            if (onlineSupporters.size() == 0) {
                if (!SupportBot.api.getChannelInfo(SUPPORT_WAITING_CHANNEL_ID).getName().equalsIgnoreCase("┏ Support | Kein Teammitglied verfügbar")) {
                    property.put(ChannelProperty.CHANNEL_NAME, "┏ Support | Kein Teammitglied verfügbar");
                    property.put(ChannelProperty.CHANNEL_MAXCLIENTS, "1");
                    property.put(ChannelProperty.CHANNEL_FLAG_MAXCLIENTS_UNLIMITED, "1");
                    SupportBot.api.editChannel(31, property);
                    property.clear();
                }
            } else {
                if (!SupportBot.api.getChannelInfo(SUPPORT_WAITING_CHANNEL_ID).getName().equalsIgnoreCase("┏ Support | Warteschlange [" + onlineSupporters.size() + "]")) {
                    property.put(ChannelProperty.CHANNEL_NAME, "┏ Support | Warteschlange [" + onlineSupporters.size() + "]");
                    property.put(ChannelProperty.CHANNEL_MAXCLIENTS, "1");
                    property.put(ChannelProperty.CHANNEL_FLAG_MAXCLIENTS_UNLIMITED, "1");
                    SupportBot.api.editChannel(31, property);
                    property.clear();
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}