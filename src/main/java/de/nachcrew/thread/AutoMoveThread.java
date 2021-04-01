package de.nachcrew.thread;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.nachcrew.utils.ClientUtils;

import java.util.function.Consumer;

public class AutoMoveThread extends Thread {

    private final Channel channel;
    private final Consumer<Client> clientConsumer;

    public AutoMoveThread(Channel channel, Consumer<Client> clientConsumer) {
        this.channel = channel;
        this.clientConsumer = clientConsumer;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            if (channel == null) this.interrupt();

            final Client randomClient = ClientUtils.getRandomClient();
            if (randomClient != null) {
                clientConsumer.accept(randomClient);
                this.interrupt();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }
}