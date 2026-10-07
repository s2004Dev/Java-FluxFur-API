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

package lonter.jfa.api.events.guild.voice;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.GuildVoiceState;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.requests.RestAction;

import java.time.OffsetDateTime;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a guild member has updated their {@link GuildVoiceState#getRequestToSpeakTimestamp() Request-to-Speak}.
 *
 * <p>If {@link #getNewTime()} is non-null, this means the member has <em>raised their hand</em> and wants to speak.
 * You can use {@link #approveSpeaker()} or {@link #declineSpeaker()} to handle this request if you have {@link lonter.jfa.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.utils.cache.CacheFlag#VOICE_STATE VOICE_STATE} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public class GuildVoiceRequestToSpeakEvent extends GenericGuildVoiceEvent {
    private final OffsetDateTime oldTime, newTime;

    public GuildVoiceRequestToSpeakEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Member member,
            @Nullable OffsetDateTime oldTime,
            @Nullable OffsetDateTime newTime) {
        super(api, responseNumber, member);
        this.oldTime = oldTime;
        this.newTime = newTime;
    }

    /**
     * The old {@link GuildVoiceState#getRequestToSpeakTimestamp()}
     *
     * @return The old timestamp, or null if this member did not request to speak before
     */
    @Nullable
    public OffsetDateTime getOldTime() {
        return oldTime;
    }

    /**
     * The new {@link GuildVoiceState#getRequestToSpeakTimestamp()}
     *
     * @return The new timestamp, or null if the request to speak was declined or cancelled
     */
    @Nullable
    public OffsetDateTime getNewTime() {
        return newTime;
    }

    /**
     * Promote the member to speaker.
     * <p>This requires a non-null {@link #getNewTime()}.
     * You can use {@link GuildVoiceState#inviteSpeaker()} to invite the member to become a speaker if they haven't requested to speak.
     *
     * <p>This does nothing if the member is not connected to a {@link StageChannel}.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link lonter.jfa.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}
     *         in the associated {@link StageChannel}
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    public RestAction<Void> approveSpeaker() {
        return getVoiceState().approveSpeaker();
    }

    /**
     * Reject this members {@link GuildVoiceState#getRequestToSpeakTimestamp() request to speak}.
     * <p>This requires a non-null {@link #getNewTime()}.
     * The member will have to request to speak again.
     *
     * <p>This does nothing if the member is not connected to a {@link StageChannel}.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link lonter.jfa.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}
     *         in the associated {@link StageChannel}
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    public RestAction<Void> declineSpeaker() {
        return getVoiceState().declineSpeaker();
    }
}
