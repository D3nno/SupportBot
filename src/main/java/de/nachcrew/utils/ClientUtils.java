package de.nachcrew.utils;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.nachcrew.SupportBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ClientUtils {

    private static final int SUPPORT_GROUP_ID = 28;
    private static final int SUPPORT_CHANNEL_PARENT_ID = 30;
    public static final List<Integer> supportingClients = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final int TEAM_AFK_CHANNEL_ID = 384;
    private static final int AFK_CHANNEL_ID = 44;
    private static final List<Integer> TEAM_GROUP_IDS = Arrays.asList(27, 39, 14, 20, 23);

    public static Client getRandomClient() {
        final List<Client> clients = getOnlineSupporters(false);

        if (clients.isEmpty()) return null;

        final int i = RANDOM.nextInt(clients.size());
        Client client = clients.get(i);
        return client;
    }

    public static List<Client> getOnlineSupporters(final boolean ignoreState) {
        if (ignoreState)
            return SupportBot.api.getClients().stream().filter(client -> TEAM_GROUP_IDS.stream().anyMatch(client::isInServerGroup)).collect(Collectors.toList());
        return SupportBot.api.getClients().stream().filter(client -> (!client.isInputMuted() && !client.isOutputMuted() && client.isInputHardware()) && (client.getChannelId() != TEAM_AFK_CHANNEL_ID && client.getChannelId() != AFK_CHANNEL_ID) && !supportingClients.contains(client.getId()) && client.isInServerGroup(SUPPORT_GROUP_ID) && SupportBot.api.getChannelInfo(client.getChannelId()).getParentChannelId() != SUPPORT_CHANNEL_PARENT_ID).collect(Collectors.toList());
    }

    public static List<Client> getClients(final int channelid) {
        return SupportBot.api.getClients().stream().filter(client -> client.getChannelId() == channelid).collect(Collectors.toList());
    }
}