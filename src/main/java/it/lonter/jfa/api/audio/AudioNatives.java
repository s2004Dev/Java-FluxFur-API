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

package it.lonter.jfa.api.audio;

import club.minnced.opus.util.OpusLibrary;
import it.lonter.jfa.internal.utils.JFALogger;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Controller used by JFA to ensure the native
 * binaries for opus en-/decoding are available.
 *
 * @see <a href="https://github.com/fluxer-java/opus-java" target="_blank">opus-java source</a>
 */
public final class AudioNatives {
    private static final Logger LOG = JFALogger.getLog(AudioNatives.class);
    private static boolean initialized;
    private static boolean audioSupported;

    private AudioNatives() {}

    /**
     * Whether the opus library is loaded or not.
     * <br>This is initialized by the first call to {@link #ensureOpus()}.
     *
     * @return True, opus library is loaded.
     */
    public static boolean isAudioSupported() {
        return audioSupported;
    }

    /**
     * Whether this class was already initialized or not.
     *
     * @return True, if this class was already initialized.
     *
     * @see    #ensureOpus()
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks whether the opus binary was loaded, if not it will be initialized here.
     * <br>This is used by JFA to check at runtime whether the opus library is available or not.
     *
     * @return True, if the library could be loaded.
     */
    public static synchronized boolean ensureOpus() {
        if (initialized) {
            return audioSupported;
        }
        initialized = true;
        try {
            if (OpusLibrary.isInitialized()) {
                return audioSupported = true;
            }
            audioSupported = OpusLibrary.loadFromJar();
        } catch (Throwable e) {
            handleException(e);
        } finally {
            if (audioSupported) {
                LOG.info("Audio System successfully setup!");
            } else {
                LOG.info("Audio System encountered problems while loading, thus, is disabled.");
            }
        }
        return audioSupported;
    }

    private static void handleException(Throwable e) {
      switch (e) {
        case UnsupportedOperationException unsupportedOperationException ->
          LOG.error("Sorry, JFA's audio system doesn't support this system.\n{}", e.getMessage());
        case NoClassDefFoundError noClassDefFoundError ->
          LOG.error("Missing opus dependency, unable to initialize audio!");
        case IOException ioException ->
          LOG.error("There was an IO Exception when setting up the temp files for audio.", e);
        case UnsatisfiedLinkError unsatisfiedLinkError ->
          LOG.error("JFA encountered a problem when attempting to load the Native libraries. Contact a DEV.", e);
        case Error error -> throw error;
        case null, default ->
          LOG.error("An unknown exception occurred while attempting to setup JFA's audio system!", e);
      }
    }
}
