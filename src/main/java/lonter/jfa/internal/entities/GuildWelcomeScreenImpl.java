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

package lonter.jfa.internal.entities;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.GuildWelcomeScreen;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.emoji.CustomEmoji;
import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.entities.emoji.EmojiUnion;
import lonter.jfa.api.managers.GuildWelcomeScreenManager;
import lonter.jfa.api.utils.data.DataObject;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildWelcomeScreenImpl implements GuildWelcomeScreen {
    private final Guild guild;
    private final String description;
    private final List<Channel> channels;

    public GuildWelcomeScreenImpl(
            @Nullable Guild guild, @Nullable String description, @NotNull List<Channel> channels) {
        this.guild = guild;
        this.description = description;
        this.channels = channels;
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public GuildWelcomeScreenManager getManager() {
        if (guild == null) {
            throw new IllegalStateException("Cannot modify a guild welcome screen from an Invite");
        }
        return guild.modifyWelcomeScreen();
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @NotNull
    @Override
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * POJO for the recommended channels information provided by a welcome screen.
     * <br>Recommended channels are shown in the welcome screen after joining a server.
     *
     * @see GuildWelcomeScreen#getChannels()
     */
    public static class ChannelImpl implements GuildWelcomeScreen.Channel {
        private final Guild guild;
        private final long id;
        private final String description;
        private final EmojiUnion emoji;

        public ChannelImpl(@Nullable Guild guild, long id, @NotNull String description, @Nullable EmojiUnion emoji) {
            this.guild = guild;
            this.id = id;
            this.description = description;
            this.emoji = emoji;
        }

        @Nullable
        @Override
        public Guild getGuild() {
            return guild;
        }

        @Override
        public long getIdLong() {
            return id;
        }

        @Nullable
        @Override
        public GuildChannel getChannel() {
            if (guild == null) {
                return null;
            }

            return guild.getGuildChannelById(id);
        }

        @NotNull
        @Override
        public String getDescription() {
            return description;
        }

        @Nullable
        @Override
        public EmojiUnion getEmoji() {
            return emoji;
        }

        @NotNull
        @Override
        public DataObject toData() {
            DataObject data = DataObject.empty();
            data.put("channel_id", id);
            data.put("description", description);
            if (emoji != null) {
                if (emoji.getType() == Emoji.Type.CUSTOM) {
                    data.put("emoji_id", ((CustomEmoji) emoji).getId());
                }
                data.put("emoji_name", emoji.getName());
            }

            return data;
        }
    }
}
