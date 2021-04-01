package de.nachcrew.utils;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import de.nachcrew.SupportBot;

import java.util.List;
import java.util.stream.Collectors;

public class ChannelUtils {

    public static Channel getLowestSupportChannel() {
        final List<Channel> parentChannels = SupportBot.api.getChannels().stream().filter(channel -> channel.getParentChannelId() == 35).collect(Collectors.toList());
        if (parentChannels.isEmpty()) return null;
        final Channel lastChannel = parentChannels.get(parentChannels.size() - 1);
        return lastChannel;
    }

    public static Channel getHighestSupportChannel() {
        final List<Channel> parentChannels = SupportBot.api.getChannels().stream().filter(channel -> channel.getParentChannelId() == 34).collect(Collectors.toList());
        if (parentChannels.isEmpty()) return null;
        final Channel lastChannel = parentChannels.get(0);
        return lastChannel;
    }

    public static Channel getChannel(int currentChannelId) {
        final ChannelInfo channelInfo = SupportBot.api.getChannelInfo(currentChannelId);
        return SupportBot.api.getChannelByNameExact(channelInfo.getName(), true);
    }
}