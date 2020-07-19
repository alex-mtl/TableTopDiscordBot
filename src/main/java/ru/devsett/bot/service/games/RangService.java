package ru.devsett.bot.service.games;

import discord4j.core.object.VoiceState;
import org.springframework.stereotype.Service;
import ru.devsett.db.service.ChannelService;
import ru.devsett.db.service.UserService;
import ru.devsett.db.service.WatchmanService;

@Service
public class RangService {
    private final UserService userService;
    private final WatchmanService watchmanService;
    private final ChannelService channelService;

    public RangService(UserService userService, WatchmanService watchmanService, ChannelService channelService) {
        this.userService = userService;
        this.watchmanService = watchmanService;
        this.channelService = channelService;
    }

    public void join(VoiceState current) {
        var user = userService.getOrNewUser(current.getMember().block());
        var channel = current.getChannel().block();
        var channelEntity = channelService.getOrNewChannel(channel.getName(), channel.getId().asLong(), true);
        watchmanService.join(channelEntity, user, System.currentTimeMillis());
    }

    public void exit(VoiceState current, VoiceState old) {
        var user = userService.getOrNewUser(current.getMember().block());
        var channel = old.getChannel().block();
        var channelEntity = channelService.getOrNewChannel(channel.getName(), channel.getId().asLong(), true);
        var watchman = watchmanService.exit(channelEntity, user, System.currentTimeMillis());

        var channelNew = current.getChannel().block();
        if (channelNew != null) {
            join(current);
        }

        if (watchman != null) {
            var timeSec = (watchman.getExitTime().getTime() - watchman.getJoinTime().getTime()) / 1000;
            var raite = (timeSec * 0.0006);
            if (raite >= 1) {
                userService.addRating(user, raite > 52 ? 52 : (int) raite);
            }
        }
    }
}
