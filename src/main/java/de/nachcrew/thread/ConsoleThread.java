package de.nachcrew.thread;

import de.nachcrew.channel.ChannelProvider;
import jline.ConsoleReader;

import java.io.IOException;

public class ConsoleThread extends Thread{

    private final ConsoleReader consoleReader = new ConsoleReader();
    private final ChannelProvider channelProvider;

    public ConsoleThread(ChannelProvider channelProvider) throws IOException {
        this.channelProvider = channelProvider;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            String line;

            while (true) {
                try {
                    if (((line = consoleReader.readLine("> ")) != null)) {
                        if (line.toLowerCase().startsWith("stop")) {
                             channelProvider.deleteAll();
                            System.exit(0);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}