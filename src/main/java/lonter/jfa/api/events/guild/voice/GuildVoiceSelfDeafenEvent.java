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
 * Indicates that a {@link lonter.jfa.api.entities.Member Member} (un-)deafened itself.
 *
 * <p>Can be used to detect when a member deafens or un-deafens itself.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.utils.cache.CacheFlag#VOICE_STATE VOICE_STATE} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public class GuildVoiceSelfDeafenEvent extends GenericGuildVoiceEvent {
    protected final boolean selfDeafened;

    public GuildVoiceSelfDeafenEvent(
            @NotNull JFA api, long responseNumber, @NotNull Member member, boolean isSelfDeafened) {
        super(api, responseNumber, member);
        this.selfDeafened = isSelfDeafened;
    }

    /**
     * Whether the member deafened itself in this event
     *
     * @return True, if the member deafened itself,
     *         <br>False, if the member un-deafened itself
     */
    public boolean isSelfDeafened() {
        return selfDeafened;
    }
}
