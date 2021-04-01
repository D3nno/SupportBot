package de.nachcrew.utils;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.nachcrew.SupportBot;
import de.nachcrew.thread.AutoMoveThread;
import de.nachcrew.thread.ChannelThread;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    private static List<String> sChannel = new ArrayList<>();
    private static final Map<Integer, Integer> lastChannelMap = new HashMap<>();

    public static void moveBack(final Client client) {
        final int currentChannelId = client.getChannelId();

        final List<Client> clients = ClientUtils.getClients(currentChannelId).stream().filter(client1 -> client1.getId() != client.getId()).collect(Collectors.toList());

        clients.forEach(client1 -> SupportBot.api.moveClient(client1.getId(), 40));

        final Integer lastChannelId = lastChannelMap.getOrDefault(client.getId(), 40);
        if (SupportBot.api.getChannels().stream().noneMatch(channel -> channel.getId() == lastChannelId)) {
            SupportBot.api.moveClient(client.getId(), 40);
            return;
        }

        if (lastChannelId != 31)
            SupportBot.api.moveClient(client.getId(), lastChannelId);
    }

    public static void support(Client client, final String reason) {
        final StringBuilder descriptionBuilder = new StringBuilder();

        ClientUtils.getOnlineSupporters(true).forEach(supporterClient -> SupportBot.api.sendPrivateMessage(supporterClient.getId(), "Der User [B]" + client.getNickname() + "[/B] wartet im Support!"));

        descriptionBuilder
                .append("[B]Anliegen: [/B]" + reason)
                .append("\n\n")
                .append("[B]Nickname: [/B]" + client.getNickname())
                .append("\n\n")
                .append("[B]IP: [/B]" + client.getIp())
                .append("\n\n")
                .append("[B]Land: [/B]" + client.getCountry())
                .append("\n\n")
                .append("[B]Platform: [/B]" + client.getPlatform())
                .append("\n\n")
                .append("[B]TS3 Version: [/B]" + client.getVersion())
                .append("\n\n")
                .append("\n\n")
                .append("[B]Zeitpunkt: [/B]" + DATE_FORMAT.format(new Date()));

        boolean var = false;
        Map<ChannelProperty, String> property = new HashMap<ChannelProperty, String>();
        property.put(ChannelProperty.CHANNEL_MAXCLIENTS, "0");
        property.put(ChannelProperty.CHANNEL_FLAG_MAXCLIENTS_UNLIMITED, "0");
        property.put(ChannelProperty.CHANNEL_FLAG_PERMANENT, "1");
        property.put(ChannelProperty.CHANNEL_DESCRIPTION, descriptionBuilder.toString());
        property.put(ChannelProperty.CPID, "31");
        System.out.println("Creating");

        final Channel lowestSupportChannel = ChannelUtils.getLowestSupportChannel();
        if (lowestSupportChannel != null) {
            if (lowestSupportChannel.getName().contains("┗")) {
                String channelName = lowestSupportChannel.getName().replaceAll("┗", "┣");
                SupportBot.api.editChannel(lowestSupportChannel.getId(), new HashMap<ChannelProperty, String>() {{
                    this.put(ChannelProperty.CHANNEL_NAME, channelName);
                }});
            }
        }
        System.out.println("renamed first");


        final Channel highestSupportChannel = ChannelUtils.getHighestSupportChannel();
        if (highestSupportChannel != null) {
            if (!highestSupportChannel.getName().contains("┏")) {
                String channelName = lowestSupportChannel.getName().replaceAll("┗", "┏");
                SupportBot.api.editChannel(highestSupportChannel.getId(), new HashMap<ChannelProperty, String>() {{
                    this.put(ChannelProperty.CHANNEL_NAME, channelName);
                }});
            }
        }
        System.out.println("renamed second");

        final String randomId = getRandomId();
        System.out.println("Created support with id " + randomId);
        ChannelInfo channelInfo = SupportBot.api.getChannelInfo(SupportBot.api.createChannel(((lowestSupportChannel == null) ? "┏" : "┗") + " Support Channel | " + randomId, property));
        final Channel supportChannel = SupportBot.api.getChannelByNameExact(channelInfo.getName(), false);

        SupportBot.api.addChannelPermission(channelInfo.getId(), "i_channel_needed_join_power", 75);
        SupportBot.api.addChannelPermission(channelInfo.getId(), "i_channel_needed_description_view_power", 75);

        if (SupportBot.api.whoAmI().getId() == client.getId()) return;
        SupportBot.api.moveClient(client.getId(), channelInfo.getId());
        if (!var) {
            sChannel.add(channelInfo.getName());

            new AutoMoveThread(supportChannel, client1 -> {

                final StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 15; i++) stringBuilder.append("\n");

                SupportBot.api.sendPrivateMessage(client1.getId(), stringBuilder.toString() + "Du bist nun im Supportgespräch mit der Id [B]" + randomId + "[/B]");
                try {
                    Thread.sleep(2250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lastChannelMap.put(client1.getId(), client1.getChannelId());
                if (supportChannel != null)
                    SupportBot.api.moveClient(client1.getId(), supportChannel.getId());
                System.out.println(client1.getId() + " removed!");
                ClientUtils.supportingClients.removeIf(integer -> integer == client1.getId());
            }).start();

            new ChannelThread(channelInfo.getId(), channel -> {
                System.out.println("Deleting channel " + channel.getName() + " because is empty.");
                SupportBot.api.deleteChannel(channel.getId(), true);

                final Channel lowestSupport = ChannelUtils.getLowestSupportChannel();

                if (lowestSupport != null)
                    if (!lowestSupport.getName().contains("┗"))
                        if (SupportBot.api.getChannelByNameExact(lowestSupport.getName().replaceAll("┣", "┗"), true) != null)
                            SupportBot.api.editChannel(lowestSupport.getId(), new HashMap<ChannelProperty, String>() {{
                                this.put(ChannelProperty.CHANNEL_NAME, lowestSupport.getName().replaceAll("┣", "┗"));
                            }});
            }).start();
        }

    }

    private static String getRandomId() {
        return StringUtils.generateRandomString(5);
    }
}