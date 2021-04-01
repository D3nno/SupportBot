package de.nachcrew.channel;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ChannelGroup {
    private final String defaultChannelName;
    private int minChannels;
    private int maxChannels;
    private final int maxClients;
    private final int subChannelId;
    private final boolean subChannel;
    private final int neededJoinPower;
    private final int orderId;
    private final boolean orderFromGroupAbove;
    private final boolean limitedArea;
}