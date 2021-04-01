package de.nachcrew.thread;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import de.nachcrew.channel.ChannelGroup;
import de.nachcrew.channel.ChannelProvider;

import java.util.List;

public class ChannelCheckerThread extends Thread{

    private final ChannelProvider channelProvider;
    private final List<ChannelGroup> groupList;

    public ChannelCheckerThread(ChannelProvider channelProvider, List<ChannelGroup> groupList) {
        this.channelProvider = channelProvider;
        this.groupList = groupList;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            groupList.forEach(channelGroup -> {
                final List<Channel> unusedChannels = channelProvider.getUnusedChannels(channelGroup);
                final List<Channel> channels = channelProvider.getChannels(channelGroup);

                if (unusedChannels.size() < channelGroup.getMinChannels()) {
                    channelProvider.createChannel(channelGroup);
                    System.out.println("Creating channel for group " + channelGroup.getDefaultChannelName());
                } else if (unusedChannels.size() > channelGroup.getMinChannels()) {
                    channelProvider.deleteChannel(channelGroup);
                }
            });

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        System.out.println("Thread interrupted!");
    }
}