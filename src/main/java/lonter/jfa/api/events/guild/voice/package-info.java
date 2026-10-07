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

/**
 * Events indicating the {@link lonter.jfa.api.entities.GuildVoiceState GuildVoiceState} updates
 * for one of the {@link lonter.jfa.api.entities.Guild Guild}'s {@link lonter.jfa.api.entities.Member Members}.
 * <br>Every update is an extensions of the {@link lonter.jfa.api.events.guild.voice.GenericGuildVoiceEvent GenericGuildVoiceEvent}
 * and has specifications for explicit voice state features such as mute/deafen
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.utils.cache.CacheFlag#VOICE_STATE VOICE_STATE} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
package lonter.jfa.api.events.guild.voice;
