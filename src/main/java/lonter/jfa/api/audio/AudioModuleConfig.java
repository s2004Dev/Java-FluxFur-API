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

package lonter.jfa.api.audio;

import lonter.jfa.api.audio.dave.DaveSession;
import lonter.jfa.api.audio.dave.DaveSessionFactory;
import lonter.jfa.api.audio.dave.PassthroughDaveSessionFactory;
import lonter.jfa.api.audio.factory.DefaultSendFactory;
import lonter.jfa.api.audio.factory.IAudioSendFactory;
import lonter.jfa.internal.utils.Checks;
import org.jetbrains.annotations.Contract;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration for audio features in JFA.
 *
 * <p>This config is immutable, any updates to the config return a new config instance.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * jfaBuilder.setAudioModuleConfig(
 *   new AudioModuleConfig()
 *     .withAudioSendFactory(new NativeAudioSendFactory())
 *     .withDaveSessionFactory(new JDaveSessionFactory())
 * )
 * }
 *
 * @see #withAudioSendFactory(IAudioSendFactory)
 * @see #withDaveSessionFactory(DaveSessionFactory)
 */
public class AudioModuleConfig {
    private DaveSessionFactory daveSessionFactory = new PassthroughDaveSessionFactory();
    private IAudioSendFactory audioSendFactory = new DefaultSendFactory();

    /**
     * The factory used for DAVE sessions.
     *
     * <p>Each audio / video connection on Fluxer requires End-to-End Encryption
     * using the "Fluxer Audio &amp; Video End-to-End Encryption" (DAVE) Protocol.
     * The {@link DaveSession} provided by this factory is responsible for managing this protocol for each connection.
     * By default, JFA does not implement this protocol.
     *
     * @return {@link DaveSessionFactory}
     *
     * @see <a href="https://github.com/fluxer/dave-protocol" target="_blank">DAVE Protocol</a>
     */
    @NotNull
    public DaveSessionFactory getDaveSessionFactory() {
        return daveSessionFactory;
    }

    /**
     * The factory used for DAVE sessions.
     *
     * <p>Each audio / video connection on Fluxer requires End-to-End Encryption
     * using the "Fluxer Audio &amp; Video End-to-End Encryption" (DAVE) Protocol.
     * The {@link DaveSession} provided by this factory is responsible for managing this protocol for each connection.
     * By default, JFA does not implement this protocol.
     *
     * @param  daveSessionFactory
     *         The dave session factory to use
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A <b>new</b> AudioModuleConfig with the provided factory
     *
     * @see <a href="https://github.com/fluxer/dave-protocol" target="_blank">DAVE Protocol</a>
     */
    @NotNull
    @Contract("_ -> new")
    @CheckReturnValue
    public AudioModuleConfig withDaveSessionFactory(@NotNull DaveSessionFactory daveSessionFactory) {
        Checks.notNull(daveSessionFactory, "Factory");
        AudioModuleConfig newConfig = copy();
        newConfig.daveSessionFactory = daveSessionFactory;
        return newConfig;
    }

    /**
     * The currently configured {@link IAudioSendFactory}.
     *
     * @return {@link IAudioSendFactory}
     *
     * @see #withAudioSendFactory(IAudioSendFactory)
     */
    @NotNull
    public IAudioSendFactory getAudioSendFactory() {
        return audioSendFactory;
    }

    /**
     * Changes the factory used to create {@link lonter.jfa.api.audio.factory.IAudioSendSystem IAudioSendSystem}
     * objects which handle the sending loop for audio packets.
     * <br>By default, JFA uses {@link lonter.jfa.api.audio.factory.DefaultSendFactory DefaultSendFactory}.
     *
     * @param  audioSendFactory
     *         The new {@link lonter.jfa.api.audio.factory.IAudioSendFactory IAudioSendFactory} to be used
     *         when creating new {@link lonter.jfa.api.audio.factory.IAudioSendSystem} objects.
     *
     * @return A <b>new</b> AudioModuleConfig with the provided factory
     */
    @NotNull
    @Contract("_ -> new")
    @CheckReturnValue
    public AudioModuleConfig withAudioSendFactory(@NotNull IAudioSendFactory audioSendFactory) {
        Checks.notNull(audioSendFactory, "Factory");
        AudioModuleConfig newConfig = copy();
        newConfig.audioSendFactory = audioSendFactory;
        return newConfig;
    }

    @NotNull
    private AudioModuleConfig copy() {
        AudioModuleConfig config = new AudioModuleConfig();
        config.daveSessionFactory = this.daveSessionFactory;
        config.audioSendFactory = this.audioSendFactory;
        return config;
    }
}
