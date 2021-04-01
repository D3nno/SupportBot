package de.nachcrew.thread;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import de.nachcrew.SupportBot;

import java.util.function.Consumer;

public class ChannelThread extends Thread {

    private final int channelId;
    private final Consumer<Channel> consumer;

    public ChannelThread(int channelId, Consumer<Channel> consumer) {
        this.channelId = channelId;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            ChannelInfo channelInfo = SupportBot.api.getChannelInfo(channelId);
            if(channelInfo == null) interrupt();
            Channel channel = SupportBot.api.getChannelByNameExact(channelInfo.getName(), true);
            if (channel == null) interrupt();

            if (channel.getTotalClients() == 0) {
                consumer.accept(channel);
                this.interrupt();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Started thread with id " + getId());
    }

    @Override
    public void interrupt() {
        super.interrupt();
        System.out.println("Interrupted thread with id " + getId());
    }
}