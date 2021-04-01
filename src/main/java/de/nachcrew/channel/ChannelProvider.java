package de.nachcrew.channel;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import de.nachcrew.SupportBot;
import de.nachcrew.thread.ChannelCheckerThread;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.xkuyax.utils.config.Config;

import java.util.*;
import java.util.stream.Collectors;

public class ChannelProvider {

    private static final Config CONFIG = new Config("channelgroups.yml");
    private final List<CreationChannel> creationChannels = new ArrayList<>();
    private final Map<String, Map<String, Integer>> permissionsForGroupMap;
    private final List<ChannelGroup> channelGroups;
    private final Thread checkerThread;

    public ChannelProvider() {
        this.channelGroups = CONFIG.getGenericType("groups", GroupList.class).getChannelGroups();
        this.permissionsForGroupMap = CONFIG.getGenericType("permissions", PermissionsForGroupMap.class).getPermissionsForGroupMap();

        this.checkerThread = new ChannelCheckerThread(this, channelGroups);
        this.checkerThread.start();
    }

    public synchronized void createChannel(final ChannelGroup channelGroup) {
        final int freeId = getFreeId(channelGroup);
        if (freeId == -1) {
            return;
        }

        String channelName = "┗ " + channelGroup.getDefaultChannelName() + " | " + freeId;
        final List<Channel> channels = SupportBot.api.getChannels();
        String finalChannelName = channelName;
        final Channel channel = channels.stream().filter(channel1 -> channel1.getName().substring(1).equalsIgnoreCase(finalChannelName.substring(1))).findFirst().orElse(null);

        if (channel != null && creationChannels.stream().noneMatch(creationChannel -> creationChannel.getChannelId() == channel.getId()) && channel.getMaxClients() == channelGroup.getMaxClients()) {
            creationChannels.add(new CreationChannel(freeId, channel.getId(), channelGroup));
            return;
        }

        if (channel != null) {
            this.deleteChannel(channel.getId());
            SupportBot.api.deleteChannel(channel.getId(), true);
        }

        if (channelGroup.isSubChannel()) {
            if (getHighestChannel(channelGroup.getSubChannelId()) == null) {
                renameHighestChannel(channelGroup);
                renameLowestChannel(channelGroup);
            } else {
                channelName = channelName.replaceAll("┗", "┣");
            }
        }

        if (!channelGroup.isLimitedArea()) {
            channelName = channelName.replaceAll("┗", "┣")
                    .replaceAll("┏", "┣");
        }

        final List<CreationChannel> creationChannels = this.creationChannels.stream().filter(creationChannel -> creationChannel.getChannelGroup().equals(channelGroup)).collect(Collectors.toList());
        final int orderId = ((creationChannels.isEmpty()) ? channelGroup.getOrderId() : creationChannels.get(this.getCreationChannels(channelGroup).size() - 1).getChannelId());

        final int channelId = SupportBot.api.createChannel(channelName, new HashMap<ChannelProperty, String>() {{
            this.put(ChannelProperty.CHANNEL_FLAG_PERMANENT, "1");
            if (channelGroup.isSubChannel() && channelGroup.getSubChannelId() != -1)
                this.put(ChannelProperty.CPID, Integer.toString(channelGroup.getSubChannelId()));
            this.put(ChannelProperty.CHANNEL_MAXCLIENTS, Integer.toString(channelGroup.getMaxClients()));
            if (orderId != -1)
                this.put(ChannelProperty.CHANNEL_ORDER, Integer.toString(orderId));
        }});

        final Channel channelAbove = this.getChannelAbove(channelId);
        if (channelGroup.isLimitedArea()) {
            if (this.getChannelGroup(channelAbove.getId()) != null) {
                if (this.getHighestChannel(channelGroup).getId() != channelAbove.getId())
                    this.renameChannel(channelAbove.getId());
            }

            if (channelGroup.isOrderFromGroupAbove()) {
                final int newOrderId = channelAbove.getId();
                SupportBot.api.editChannel(channelId, new HashMap<ChannelProperty, String>() {{
                    this.put(ChannelProperty.CHANNEL_ORDER, Integer.toString(newOrderId));
                }});
            }
        }

        if (channelGroup.isLimitedArea()) {
            final Channel lowestChannel = this.getLowestChannel(channelGroup);
            if (lowestChannel != null)
                if (lowestChannel.getId() == channelId) {
                    SupportBot.api.editChannel(channelId, new HashMap<ChannelProperty, String>() {{
                        this.put(ChannelProperty.CHANNEL_NAME, lowestChannel.getName().replaceAll("┣", "┗"));
                    }});
                }

            final Channel highestChannel = this.getHighestChannel(channelGroup);
            if (highestChannel != null && this.creationChannels.stream().noneMatch(creationChannel -> channelAbove.getId() == creationChannel.getChannelId()))
                if (highestChannel.getId() == channelId) {
                    SupportBot.api.editChannel(channelId, new HashMap<ChannelProperty, String>() {{
                        this.put(ChannelProperty.CHANNEL_NAME, lowestChannel.getName().replaceAll("┗", "┏"));
                    }});
                }

            this.renameLowestChannel(channelGroup);
            this.creationChannels.add(new CreationChannel(freeId, channelId, channelGroup));

            if (channelGroup.isLimitedArea()) {
                this.renameHighestChannel(channelGroup);
                this.renameLowestChannel(channelGroup);
            }

        }
        getPermissionsForGroup(channelGroup).forEach((s, integer) -> SupportBot.api.addChannelPermission(channelId, s, integer));
    }

    private void renameChannel(int id) {
        if (this.getChannelUnder(id) != null && this.getChannelAbove(id) != null) {
            if (this.getChannelGroup(this.getChannelUnder(id).getId()) != null && this.getChannelGroup(this.getChannelAbove(id).getId()) != null) {
                final Channel channel = SupportBot.api.getChannels().stream().filter(channel1 -> channel1.getId() == id).findFirst().orElse(null);
                if (channel != null)
                    SupportBot.api.editChannel(id, new HashMap<ChannelProperty, String>() {{
                        this.put(ChannelProperty.CHANNEL_NAME, channel.getName()
                                .replaceAll("┗", "┣"))
                                .replaceAll("┏", "┣");
                    }});
                else
                    System.out.println("Channel to rename is null!");
            }
        }
    }

    public synchronized void renameLowestChannel(final ChannelGroup channelGroup) {
        final Channel lowestChannel = this.getLowestChannel(channelGroup);

        if (lowestChannel == null) {
            System.out.println("lowest channel is null!");
            return;
        }
        if (lowestChannel.getName().contains("┗")) {
            SupportBot.api.editChannel(lowestChannel.getId(), new HashMap<ChannelProperty, String>() {{
                this.put(ChannelProperty.CHANNEL_NAME, lowestChannel.getName().replaceAll("┗", "┣"));
            }});
            return;
        }

        final Channel channelUnder = this.getChannelUnder(lowestChannel.getId());
        if (channelUnder == null || this.getChannelGroup(channelUnder.getId()) == null) {
            final String name = lowestChannel.getName().replaceAll("┣", "┗");
            if (SupportBot.api.getChannelByNameExact(name, true) == null) {
                SupportBot.api.editChannel(lowestChannel.getId(), new HashMap<ChannelProperty, String>() {{
                    this.put(ChannelProperty.CHANNEL_NAME, name);
                }});
            } else {
                System.out.println("Channel " + name + " already exists!");
            }
        }
    }


    public synchronized void renameHighestChannel(final ChannelGroup channelGroup) {
        final Channel highestChannel = this.getHighestChannel(channelGroup);

        if (highestChannel == null) {
            return;
        }
        if (!highestChannel.getName().contains("┏") && (highestChannel.getName().contains("┣") || highestChannel.getName().contains("┗")))
            SupportBot.api.editChannel(highestChannel.getId(), new HashMap<ChannelProperty, String>() {{
                this.put(ChannelProperty.CHANNEL_NAME, highestChannel.getName()
                        .replaceAll("┣", "┏")
                        .replaceAll("┗", "┏"));
            }});

        if (this.getChannelAbove(highestChannel.getId()) == null || this.getChannelGroup(this.getChannelAbove(highestChannel.getId()).getId()) == null) {
            final String name = highestChannel.getName()
                    .replaceAll("┗", "┏");
            if (SupportBot.api.getChannelByNameExact(name, true) == null)
                SupportBot.api.editChannel(highestChannel.getId(), new HashMap<ChannelProperty, String>() {{
                    this.put(ChannelProperty.CHANNEL_NAME, name);
                }});
        }
    }

    public synchronized List<Channel> getChannels(final ChannelGroup channelGroup) {
        creationChannels.sort(Comparator.comparingInt(CreationChannel::getId));
        final List<Channel> channels = new ArrayList<>();
        creationChannels.stream().filter(creationChannel -> creationChannel.getChannelGroup().equals(channelGroup)).forEach(creationChannel -> {
            channels.add(SupportBot.api.getChannels().stream().filter(channel -> channel.getId() == creationChannel.getChannelId()).findFirst().orElse(null));
        });
        return channels;
    }

    public synchronized List<Channel> getUnusedChannels(final ChannelGroup channelGroup) {
        return getChannels(channelGroup).stream().filter(Objects::nonNull).filter(Channel::isEmpty).collect(Collectors.toList());
    }

    public synchronized Channel getHighestChannel(final ChannelGroup channelGroup) {
        final List<Channel> channels = getChannels(channelGroup);
        if (channels.isEmpty()) return null;
        return channels.get(0);
    }

    public synchronized Channel getLowestChannel(final ChannelGroup channelGroup) {
        final List<Channel> channels = getChannels(channelGroup);
        if (channels.isEmpty()) return null;
        return channels.get(channels.size() - 1);
    }

    public synchronized Channel getLowestUnusedChannel(final ChannelGroup channelGroup) {
        final List<Channel> channels = getChannels(channelGroup).stream().filter(Channel::isEmpty).collect(Collectors.toList());
        if (channels.isEmpty()) return null;
        return channels.get(channels.size() - 1);
    }

    public synchronized List<CreationChannel> getCreationChannels(final ChannelGroup channelGroup) {
        return creationChannels.stream().filter(creationChannel -> creationChannel.getChannelGroup().equals(channelGroup)).collect(Collectors.toList());
    }

    public synchronized Map<String, Integer> getPermissionsForGroup(final ChannelGroup channelGroup) {
        return this.permissionsForGroupMap.getOrDefault(channelGroup.getDefaultChannelName(), new HashMap<>());
    }

    public synchronized Channel getHighestChannel(final int subChannelId) {
        final List<Channel> collect = SupportBot.api.getChannels().stream().filter(channel -> channel.getParentChannelId() == subChannelId).collect(Collectors.toList());
        if (collect.isEmpty()) return null;
        return collect.get(0);
    }

    public synchronized Channel getChannelAbove(final int channelId) {
        final List<Channel> channels = SupportBot.api.getChannels();
        final Channel channel = channels.stream().filter(channel1 -> channel1.getId() == channelId).findFirst().orElse(null);
        if (channel == null) return null;
        final int i = channels.indexOf(channel);
        if (channels.size() >= i - 1 && (i - 1) > 0)
            return channels.get(i - 1);
        return null;
    }

    public synchronized Channel getChannelUnder(final int channelId) {
        final List<Channel> channels = SupportBot.api.getChannels();
        final Channel channel = channels.stream().filter(channel1 -> channel1.getId() == channelId).findFirst().orElse(null);
        if (channel == null) {
            return null;
        }
        final int i = channels.indexOf(channel);
        if (channels.size() < i + 1) {
            return null;
        }
        return channels.get(i + 1);
    }

    public synchronized int getFreeId(final ChannelGroup channelGroup) {
        int i = 1;
        while (i <= 100) {
            final List<CreationChannel> creationChannels = getCreationChannels(channelGroup);
            int finalI = i;
            if (creationChannels.stream().noneMatch(creationChannel -> creationChannel.getId() == finalI))
                return i;
            i++;
        }
        return -1;
    }

    public synchronized void deleteChannel(int channelId) {
        this.creationChannels.removeIf(creationChannel -> creationChannel.getChannelId() == channelId);
    }

    public synchronized void deleteAll() {
        this.checkerThread.interrupt();
        this.creationChannels.forEach(creationChannel -> SupportBot.api.deleteChannel(creationChannel.getChannelId(), true));
    }

    public ChannelGroup getChannelGroup(int channelId) {
        final CreationChannel creationChannel1 = this.creationChannels.stream().filter(creationChannel -> creationChannel.getChannelId() == channelId).findFirst().orElse(null);
        if (creationChannel1 == null) return null;
        return creationChannel1.getChannelGroup();
    }

    public void deleteChannel(ChannelGroup channelGroup) {
        final Channel lowestUnusedChannel = this.getLowestUnusedChannel(channelGroup);
        final Channel channelUnder = this.getChannelUnder(lowestUnusedChannel.getId());
        SupportBot.api.deleteChannel(lowestUnusedChannel.getId(), true);
        System.out.println("Deleted channel " + lowestUnusedChannel.getId() + " because is unused and too much");

        if (channelGroup.isLimitedArea()) {
            final Channel lowestChannel = getLowestChannel(channelGroup);
            if (lowestChannel != null && lowestChannel.getName().contains("┣") && channelUnder == null) {
                SupportBot.api.editChannel(lowestChannel.getId(), new HashMap<ChannelProperty, String>() {{
                    this.put(ChannelProperty.CHANNEL_NAME, lowestChannel.getName().replaceAll("┣", "┗"));
                }});
            }
            this.renameHighestChannel(channelGroup);
            this.renameLowestChannel(channelGroup);
        }
    }

    @AllArgsConstructor
    @Getter
    private static class GroupList {
        private final List<ChannelGroup> channelGroups;
    }

    @AllArgsConstructor
    @Getter
    private static class PermissionsForGroupMap {
        private final Map<String, Map<String, Integer>> permissionsForGroupMap;
    }
}