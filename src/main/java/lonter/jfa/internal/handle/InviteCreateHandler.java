/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JFA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lonter.jfa.internal.handle;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Invite;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.events.guild.invite.GuildInviteCreateEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.InviteImpl;

import java.time.OffsetDateTime;
import java.util.Optional;

public class InviteCreateHandler extends SocketHandler {
    public InviteCreateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getUnsignedLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }
        Guild realGuild = getJFA().getGuildById(guildId);
        if (realGuild == null) {
            EventCache.LOG.debug("Caching INVITE_CREATE for unknown guild with id {}", guildId);
            getJFA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        long channelId = content.getUnsignedLong("channel_id");
        GuildChannel realChannel = realGuild.getGuildChannelById(channelId);
        if (realChannel == null) {
            EventCache.LOG.debug(
                    "Caching INVITE_CREATE for unknown channel with id {} in guild with id {}", channelId, guildId);
            getJFA().getEventCache()
                    .cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            return null;
        }

        String code = content.getString("code");
        boolean temporary = content.getBoolean("temporary");
        boolean guest = (content.getInt("flags", 0) & 1) == 1;
        int maxAge = content.getInt("max_age", -1);
        int maxUses = content.getInt("max_uses", -1);
        OffsetDateTime creationTime = content.opt("created_at")
                .map(String::valueOf)
                .map(OffsetDateTime::parse)
                .orElse(null);

        Optional<DataObject> inviterJson = content.optObject("inviter");
        boolean expanded = maxUses != -1;

        User inviter = inviterJson
                .map(json -> getJFA().getEntityBuilder().createUser(json))
                .orElse(null);
        InviteImpl.ChannelImpl channel = new InviteImpl.ChannelImpl(realChannel);
        InviteImpl.GuildImpl guild = new InviteImpl.GuildImpl(realGuild);

        Invite.TargetType targetType = Invite.TargetType.fromId(content.getInt("target_type", 0));
        Invite.InviteTarget target;

        switch (targetType) {
            case STREAM:
                DataObject targetUserObject = content.getObject("target_user");
                target = new InviteImpl.InviteTargetImpl(
                        targetType, null, getJFA().getEntityBuilder().createUser(targetUserObject));
                break;
            case EMBEDDED_APPLICATION:
                DataObject applicationObject = content.getObject("target_application");
                Invite.EmbeddedApplication application = new InviteImpl.EmbeddedApplicationImpl(
                        applicationObject.getString("icon", null),
                        applicationObject.getString("name"),
                        applicationObject.getString("description"),
                        applicationObject.getString("summary"),
                        applicationObject.getLong("id"),
                        applicationObject.getInt("max_participants", -1));
                target = new InviteImpl.InviteTargetImpl(targetType, application, null);
                break;
            case NONE:
                target = null;
                break;
            default:
                target = new InviteImpl.InviteTargetImpl(targetType, null, null);
        }

        Invite invite = new InviteImpl(
                getJFA(),
                code,
                expanded,
                inviter,
                maxAge,
                maxUses,
                temporary,
                guest,
                creationTime,
                0,
                channel,
                guild,
                null,
                target,
                Invite.InviteType.GUILD);
        getJFA().handleEvent(new GuildInviteCreateEvent(getJFA(), responseNumber, invite, realChannel));
        return null;
    }
}
