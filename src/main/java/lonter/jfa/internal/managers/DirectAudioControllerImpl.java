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

package lonter.jfa.internal.managers;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.managers.DirectAudioController;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.requests.WebSocketClient;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public class DirectAudioControllerImpl implements DirectAudioController {
    private final JFAImpl api;

    public DirectAudioControllerImpl(JFAImpl api) {
        this.api = api;
    }

    @NotNull
    @Override
    public JFAImpl getJFA() {
        return api;
    }

    @Override
    public void connect(@NotNull AudioChannel channel) {
        Checks.notNull(channel, "Audio Channel");
        JFAImpl jfa = getJFA();
        WebSocketClient client = jfa.getClient();
        client.queueAudioConnect(channel);
    }

    @Override
    public void disconnect(@NotNull Guild guild) {
        Checks.notNull(guild, "Guild");
        JFAImpl jfa = getJFA();
        WebSocketClient client = jfa.getClient();
        client.queueAudioDisconnect(guild);
    }

    @Override
    public void reconnect(@NotNull AudioChannel channel) {
        Checks.notNull(channel, "Audio Channel");
        JFAImpl jfa = getJFA();
        WebSocketClient client = jfa.getClient();
        client.queueAudioReconnect(channel);
    }

    /**
     * Used to update the internal state of the voice request. When a connection
     * was successfully established JFA will stop sending requests for the initial connect.
     * <br>This is done to retry the voice updates in case of a partial service failure.
     *
     * <p>Should be called when:
     * <ol>
     *     <li>Receiving a Voice State Update for the current account and we were previously connected (moved or disconnected)</li>
     *     <li>Receiving a Voice Server Update (initial connect or region change)</li>
     * </ol>
     *
     * Note that the voice state update will always be received prior to a voice server update.
     * <br>The internal dispatch handlers already call this when needed, a library end-user never needs to call this method.
     *
     * @param guild
     *        The guild to update the state for
     * @param channel
     *        The new channel, or null to signal disconnect
     */
    public void update(Guild guild, AudioChannel channel) {
        Checks.notNull(guild, "Guild");
        JFAImpl jfa = getJFA();
        WebSocketClient client = jfa.getClient();
        client.updateAudioConnection(guild.getIdLong(), channel);
    }
}
