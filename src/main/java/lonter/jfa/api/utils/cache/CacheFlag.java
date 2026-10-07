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

package lonter.jfa.api.utils.cache;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.requests.GatewayIntent;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Flags used to enable cache services for JFA.
 * <br>Check the flag descriptions to see which {@link lonter.jfa.api.requests.GatewayIntent intents} are required to use them.
 */
public enum CacheFlag {
    /**
     * Enables cache for {@link Member#getActivities()}
     *
     * <p>Requires {@link lonter.jfa.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
     */
    ACTIVITY(GatewayIntent.GUILD_PRESENCES),
    /**
     * Enables cache for {@link Member#getVoiceState()}
     * <br>This will always be cached for self member.
     *
     * <p><b>Voice states are only cached when the member is connected to an audio channel.</b>
     *
     * <p>Requires {@link lonter.jfa.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent to be enabled.
     */
    VOICE_STATE(GatewayIntent.GUILD_VOICE_STATES),
    /**
     * Enables cache for {@link Guild#getEmojiCache()}
     *
     * <p>Requires {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent to be enabled.
     */
    EMOJI(GatewayIntent.GUILD_EXPRESSIONS),
    /**
     * Enables cache for {@link Guild#getStickerCache()}
     *
     * <p>Requires {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent to be enabled.
     */
    STICKER(GatewayIntent.GUILD_EXPRESSIONS),
    /**
     * Enables cache for {@link Member#getOnlineStatus(lonter.jfa.api.entities.ClientType) Member.getOnlineStatus(ClientType)}
     *
     * <p>Requires {@link lonter.jfa.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
     */
    CLIENT_STATUS(GatewayIntent.GUILD_PRESENCES),
    /**
     * Enables cache for {@link lonter.jfa.api.entities.channel.attribute.IPermissionContainer#getMemberPermissionOverrides()}
     */
    MEMBER_OVERRIDES,
    /**
     * Enables cache for {@link Role#getTags()}
     */
    ROLE_TAGS,
    /**
     * Enables cache for {@link IPostContainer#getAvailableTagCache()} and {@link ThreadChannel#getAppliedTags()}
     */
    FORUM_TAGS,
    /**
     * Enables cache for {@link Member#getOnlineStatus()}
     * <br>This is enabled implicitly by {@link #ACTIVITY} and {@link #CLIENT_STATUS}.
     *
     * <p>Requires {@link lonter.jfa.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
     *
     * @since 4.3.0
     */
    ONLINE_STATUS(GatewayIntent.GUILD_PRESENCES),
    /**
     * Enables cache for {@link Guild#getScheduledEventCache()}
     *
     * <p>Requires {@link lonter.jfa.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent to be enabled.
     */
    SCHEDULED_EVENTS(GatewayIntent.SCHEDULED_EVENTS),
    ;

    private static final EnumSet<CacheFlag> privileged = EnumSet.of(ACTIVITY, CLIENT_STATUS, ONLINE_STATUS);
    private final GatewayIntent requiredIntent;

    CacheFlag() {
        this(null);
    }

    CacheFlag(GatewayIntent requiredIntent) {
        this.requiredIntent = requiredIntent;
    }

    /**
     * The required {@link GatewayIntent} for this cache flag.
     *
     * @return The required intent, or null if no intents are required.
     */
    @Nullable
    public GatewayIntent getRequiredIntent() {
        return requiredIntent;
    }

    /**
     * Whether this cache flag is for presence information of a member.
     *
     * @return True, if this is for presences
     */
    public boolean isPresence() {
        return requiredIntent == GatewayIntent.GUILD_PRESENCES;
    }

    /**
     * Collects all cache flags that require privileged intents
     *
     * @return {@link EnumSet} of the cache flags that require the privileged intents
     */
    @NotNull
    public static EnumSet<CacheFlag> getPrivileged() {
        return EnumSet.copyOf(privileged);
    }
}
