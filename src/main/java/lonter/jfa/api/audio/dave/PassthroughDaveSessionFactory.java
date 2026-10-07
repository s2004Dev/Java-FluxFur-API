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

package lonter.jfa.api.audio.dave;

import lonter.jfa.internal.utils.JFALogger;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;

public class PassthroughDaveSessionFactory implements DaveSessionFactory {
    private static final Logger LOG = JFALogger.getLog(DaveSessionFactory.class);
    private static final LocalDate daveDeadline = LocalDate.of(2026, 3, 1);
    private static boolean isWarningPrinted = false;

    private static void printWarning() {
        if (isWarningPrinted) {
            return;
        }

        isWarningPrinted = true;
        LOG.warn(
                "Using passthrough dave session. Please migrate to an implementation of libdave! "
                        + "Your audio connections will stop working on {}.\n"
                        + "Read more: https://jfa.wiki/using-jfa/troubleshooting/#using-passthrough-dave-session-please-migrate-to-an-implementation-of-libdave",
                daveDeadline,
                new IllegalStateException());
    }

    @NotNull
    @Override
    public DaveSession createDaveSession(@NotNull DaveProtocolCallbacks callbacks, long userId, long channelId) {
        printWarning();
        return new PassthroughDaveSession();
    }

    private static class PassthroughDaveSession implements DaveSession {

        @Override
        public int getMaxProtocolVersion() {
            return 0;
        }

        @Override
        public int getMaxEncryptedFrameSize(@NotNull MediaType type, int frameSize) {
            return frameSize;
        }

        @Override
        public int getMaxDecryptedFrameSize(@NotNull MediaType type, long userId, int frameSize) {
            return frameSize;
        }

        @Override
        public void assignSsrcToCodec(@NotNull Codec codec, int ssrc) {}

        @Override
        public boolean encrypt(
                @NotNull MediaType mediaType, int ssrc, @NotNull ByteBuffer audio, @NotNull ByteBuffer encrypted) {
            // passthrough
            encrypted.put(audio);
            encrypted.flip();
            return true;
        }

        @Override
        public boolean decrypt(
                @NotNull MediaType mediaType,
                long userId,
                @NotNull ByteBuffer encrypted,
                @NotNull ByteBuffer decrypted) {
            // passthrough
            decrypted.put(encrypted);
            decrypted.flip();
            return true;
        }

        @Override
        public void addUser(long userId) {
            // passthrough
        }

        @Override
        public void removeUser(long userId) {
            // passthrough
        }

        @Override
        public void initialize() {}

        @Override
        public void destroy() {}

        @Override
        public void onSelectProtocolAck(int protocolVersion) {}

        @Override
        public void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion) {}

        @Override
        public void onDaveProtocolExecuteTransition(int transitionId) {}

        @Override
        public void onDaveProtocolPrepareEpoch(long epoch, int protocolVersion) {}

        @Override
        public void onDaveProtocolMLSExternalSenderPackage(@NotNull ByteBuffer externalSenderPackage) {}

        @Override
        public void onMLSProposals(@NotNull ByteBuffer proposals) {}

        @Override
        public void onMLSPrepareCommitTransition(int transitionId, @NotNull ByteBuffer commit) {}

        @Override
        public void onMLSWelcome(int transitionId, @NotNull ByteBuffer welcome) {}
    }
}
