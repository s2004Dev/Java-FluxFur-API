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
import lonter.jfa.api.entities.Member;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link lonter.jfa.api.entities.Member Member} turned on their camera.
 *
 * <p>Can be used to detect when a user starts/stops sending video.
 * <br>This does not include streams! Use {@link lonter.jfa.api.events.guild.voice.GuildVoiceStreamEvent GuildVoiceStreamEvent} for that!
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.utils.cache.CacheFlag#VOICE_STATE VOICE_STATE} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public class GuildVoiceVideoEvent extends GenericGuildVoiceEvent {
    private final boolean video;

    public GuildVoiceVideoEvent(@NotNull JFA api, long responseNumber, @NotNull Member member, boolean video) {
        super(api, responseNumber, member);
        this.video = video;
    }

    /**
     * True if this user started sending video. False if the user stopped sending video.
     *
     * @return True, if the user started sending video
     */
    public boolean isSendingVideo() {
        return video;
    }
}
