package de.nachcrew.event;

import com.github.theholywaffle.teamspeak3.api.event.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import de.nachcrew.SupportBot;
import de.nachcrew.channel.ChannelGroup;
import de.nachcrew.channel.ChannelProvider;
import de.nachcrew.utils.Utils;

import java.io.*;
import java.net.URL;
import java.util.*;

public class EventListener {

    private static final List<Integer> TEAM_GROUP_IDS = Arrays.asList(27, 35, 14, 20, 23);
    static Map<Integer, Long> icons = new HashMap<>();
    private final List<Integer> supportClients = new ArrayList<>();
    private final ChannelProvider channelProvider;

    public EventListener(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public void loadEvents() {
        SupportBot.api.registerAllEvents();
        SupportBot.api.addTS3Listeners(new TS3Listener() {
            @Override
            public void onTextMessage(TextMessageEvent event) {
                final int invokerId = event.getInvokerId();
                final ClientInfo clientInfo = SupportBot.api.getClientInfo(invokerId);
                final ClientInfo client = SupportBot.api.getClientByUId(clientInfo.getUniqueIdentifier());

                System.out.println("1");
                if (supportClients.contains(invokerId)) {
                    System.out.println("1.1");
                    Utils.support(client, event.getMessage());
                    System.out.println("1.2");
                    supportClients.remove(invokerId);
                    System.out.println("1.3");
                }

                if(event.getMessage().toLowerCase().startsWith("end") && TEAM_GROUP_IDS.stream().anyMatch(client::isInServerGroup)) {
                    Utils.moveBack(client);
                }
            }

            @Override
            public void onClientJoin(ClientJoinEvent e) {
                Client c = SupportBot.api.getClientInfo(e.getClientId());
                if (!c.isServerQueryClient()) {
                    try {
                        URL url = new URL("https://minotar.net/avatar/"+c.getNickname().split("")[0]+"/16.png");
                        InputStream in = new BufferedInputStream(url.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int i = 0;
                        while (-1 != (i = in.read(buf))) {
                            out.write(buf, 0, i);
                        }
                        in.close();
                        out.close();
                        byte[] response = out.toByteArray();
                        long iconID = SupportBot.api.uploadIconDirect(response);
                        SupportBot.api.addClientPermission(c.getDatabaseId(), "i_icon_id", (int) iconID, true);
                        icons.put(c.getId(), iconID);
                    }catch(IOException e1) {}
                }

            }

            @Override
            public void onClientLeave(ClientLeaveEvent e) {

            }

            @Override
            public void onServerEdit(ServerEditedEvent serverEditedEvent) {

            }

            @Override
            public void onChannelEdit(ChannelEditedEvent channelEditedEvent) {

            }

            @Override
            public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {

            }

            @Override
            public void onClientMoved(ClientMovedEvent e) {
                Client c = SupportBot.api.getClientInfo(e.getClientId());
                if(e.getTargetChannelId() ==31) {
                    supportClients.add(c.getId());
                    final StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < 15; i++) stringBuilder.append("\n");
                    SupportBot.api.sendPrivateMessage(c.getId(), stringBuilder.toString() + "Bitte sende dein genaues Anliegen in diesen Chat.");
                }
            }

            @Override
            public void onChannelCreate(ChannelCreateEvent channelCreateEvent) {

            }

            @Override
            public void onChannelDeleted(ChannelDeletedEvent channelDeletedEvent) {
                channelProvider.deleteChannel(channelDeletedEvent.getChannelId());
            }

            @Override
            public void onChannelMoved(ChannelMovedEvent channelMovedEvent) {

            }

            @Override
            public void onChannelPasswordChanged(ChannelPasswordChangedEvent channelPasswordChangedEvent) {

            }

            @Override
            public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent privilegeKeyUsedEvent) {

            }
        });
    }
}