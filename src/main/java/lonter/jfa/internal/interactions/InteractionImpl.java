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

package lonter.jfa.internal.interactions;

import lonter.jfa.api.entities.Entitlement;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.api.interactions.IntegrationOwners;
import lonter.jfa.api.interactions.Interaction;
import lonter.jfa.api.interactions.InteractionContextType;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.InteractionEntityBuilder;
import lonter.jfa.internal.entities.MemberImpl;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.utils.Helpers;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InteractionImpl implements Interaction {
    protected final long id;
    protected final long channelId;
    protected final int type;
    protected final String token;
    protected final Guild guild;
    protected final Member member;
    protected final User user;
    protected final Channel channel;
    protected final FluxerLocale userLocale;
    protected final List<Entitlement> entitlements;
    protected final InteractionContextType context;
    protected final IntegrationOwners integrationOwners;
    protected final JFAImpl api;
    protected final InteractionEntityBuilder interactionEntityBuilder;

    // This is used to give a proper error when an interaction is ack'd twice
    // By default, fluxer only responds with "unknown interaction"
    // which is horrible UX so we add a check manually here
    private boolean isAck;

    public InteractionImpl(JFAImpl jfa, DataObject data) {
        DataObject userObj = data.optObject("member").orElse(data).getObject("user");
        this.api = jfa;
        this.interactionEntityBuilder =
                new InteractionEntityBuilder(jfa, data.getLong("channel_id"), userObj.getUnsignedLong("id"));
        this.id = data.getUnsignedLong("id");
        this.token = data.getString("token");
        this.type = data.getInt("type");
        this.guild = data.optObject("guild")
                .map(guildJson -> {
                    if (!guildJson.hasKey("preferred_locale")) {
                        guildJson.put("preferred_locale", data.getString("guild_locale", "en-US"));
                    }
                    return interactionEntityBuilder.getOrCreateGuild(guildJson);
                })
                .orElse(null);
        this.channelId = data.getUnsignedLong("channel_id", 0L);
        this.userLocale = FluxerLocale.from(data.getString("locale", "en-US"));
        this.context = InteractionContextType.fromKey(data.getString("context"));
        this.integrationOwners = new IntegrationOwnersImpl(data.getObject("authorizing_integration_owners"));

        DataObject channelJson = data.getObject("channel");
        ChannelType channelType = ChannelType.fromId(channelJson.getInt("type"));
        if (guild instanceof GuildImpl) {
            member = jfa.getEntityBuilder().createMember((GuildImpl) guild, data.getObject("member"));
            jfa.getEntityBuilder().updateMemberCache((MemberImpl) member);
            user = member.getUser();

            GuildChannel channel = guild.getGuildChannelById(channelJson.getUnsignedLong("id"));
            if (channel == null && channelType.isThread()) {
                channel = api.getEntityBuilder()
                        .createThreadChannel((GuildImpl) guild, channelJson, guild.getIdLong(), false);
            }
            if (channel == null) {
                throw new IllegalStateException("Failed to create channel instance for interaction! Channel Type: "
                        + channelJson.getInt("type"));
            }
            this.channel = channel;
        } else if (guild instanceof DetachedGuildImpl) {
            member = interactionEntityBuilder.createMember(guild, data.getObject("member"));
            user = member.getUser();

            if (channelType.isThread()) {
                channel = interactionEntityBuilder.createThreadChannel(guild, channelJson);
            } else {
                channel = interactionEntityBuilder.createGuildChannel(guild, channelJson);
            }
            if (channel == null) {
                throw new IllegalStateException("Failed to create channel instance for interaction! Channel Type: "
                        + channelJson.getInt("type"));
            }
        } else {
            // (G)DMs
            user = jfa.getEntityBuilder().createUser(userObj);
            member = null;
            ChannelType type = channelType;
            switch (type) {
                case PRIVATE:
                    this.channel = interactionEntityBuilder.createPrivateChannel(channelJson, user);
                    break;
                case GROUP:
                    this.channel = interactionEntityBuilder.createGroupChannel(channelJson);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Received interaction in unexpected channel type! Type " + type + " is not supported yet!");
            }
        }

        this.entitlements = data.optArray("entitlements").orElseGet(DataArray::empty).stream(DataArray::getObject)
                .map(jfa.getEntityBuilder()::createEntitlement)
                .collect(Helpers.toUnmodifiableList());
    }

    // Used to allow interaction hook to send messages after acknowledgements
    // This is implemented only in DeferrableInteractionImpl where a hook is present!
    public synchronized void releaseHook(boolean success) {}

    // Ensures that one cannot acknowledge an interaction twice
    public synchronized boolean ack() {
        boolean wasAck = isAck;
        this.isAck = true;
        return wasAck;
    }

    @Override
    public synchronized boolean isAcknowledged() {
        return isAck;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public int getTypeRaw() {
        return type;
    }

    @NotNull
    @Override
    public String getToken() {
        return token;
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return guild;
    }

    @Nullable
    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public long getChannelIdLong() {
        return channelId;
    }

    @NotNull
    public FluxerLocale getUserLocale() {
        return userLocale;
    }

    @NotNull
    @Override
    public InteractionContextType getContext() {
        return context;
    }

    @NotNull
    @Override
    public IntegrationOwners getIntegrationOwners() {
        return integrationOwners;
    }

    @NotNull
    @Override
    public User getUser() {
        return user;
    }

    @Nullable
    @Override
    public Member getMember() {
        return member;
    }

    @NotNull
    @Override
    public List<Entitlement> getEntitlements() {
        return entitlements;
    }

    @NotNull
    @Override
    public JFAImpl getJFA() {
        return api;
    }

    @NotNull
    public InteractionEntityBuilder getInteractionEntityBuilder() {
        return interactionEntityBuilder;
    }
}
