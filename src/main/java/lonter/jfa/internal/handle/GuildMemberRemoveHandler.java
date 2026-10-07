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

import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.events.guild.member.GuildMemberRemoveEvent;
import lonter.jfa.api.events.guild.voice.GuildVoiceUpdateEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.GuildVoiceStateImpl;
import lonter.jfa.internal.entities.MemberImpl;
import lonter.jfa.internal.utils.UnlockHook;
import lonter.jfa.internal.utils.cache.SnowflakeCacheViewImpl;

public class GuildMemberRemoveHandler extends SocketHandler {

    public GuildMemberRemoveHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long id = content.getLong("guild_id");
        boolean setup = getJFA().getGuildSetupController().onRemoveMember(id, content);
        if (setup) {
            return null;
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildsView().get(id);
        if (guild == null) {
            // We probably just left the guild and this event is trying to remove us from the guild,
            // therefore ignore
            return null;
        }

        long userId = content.getObject("user").getUnsignedLong("id");
        if (userId == getJFA().getSelfUser().getIdLong()) {
            // We probably just left the guild and this event is trying to remove us from the guild,
            // therefore ignore
            return null;
        }

        try {
            User user = api.getEntityBuilder().createUser(content.getObject("user"));

            GuildVoiceStateImpl voiceState = guild.getVoiceStateView().getElementById(userId);
            if (voiceState != null && voiceState.inAudioChannel()) // If this user was in an AudioChannel, fire
            // VoiceLeaveEvent.
            {
                AudioChannel channel = voiceState.getChannel();
                voiceState.updateConnectedChannel(null);

                getJFA().handleEvent(new GuildVoiceUpdateEvent(
                        getJFA(), responseNumber,
                        voiceState.getMember(), channel));
            }

            MemberImpl member = (MemberImpl) guild.getMembersView().remove(userId);

            SnowflakeCacheViewImpl<User> userView = getJFA().getUsersView();
            try (UnlockHook hook = userView.writeLock()) {
                if (user.getMutualGuilds().isEmpty()) {
                    userView.remove(userId);
                    getJFA().getEventCache().clear(EventCache.Type.USER, userId);
                }
            }

            // Cache independent event
            getJFA().handleEvent(new GuildMemberRemoveEvent(getJFA(), responseNumber, guild, user, member));
            return null;
        } finally {
            // Reduce member count and remove dependent caches
            guild.onMemberRemove(userId);
        }
    }
}
