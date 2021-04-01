package de.nachcrew.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreationChannel {
    private final int id;
    private final int channelId;
    private final ChannelGroup channelGroup;
}