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

package lonter.jfa.api.audio.factory;

import lonter.jfa.api.audio.hooks.ConnectionStatus;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Represents the connection between a {@link lonter.jfa.api.audio.factory.IAudioSendSystem IAudioSendSystem} and
 * JFA's internal audio system, providing access to audio packets built from data provided from
 * {@link lonter.jfa.api.audio.AudioSendHandler AudioSendHandlers}.
 *
 * <p><b>Note that this provider is not thread-safe!</b>
 */
@NotThreadSafe
public interface IPacketProvider {
    /**
     * Provides a unique String identifier for the connection.
     * <br>Uses shard information and specific audio connection information to build string.
     *
     * @return Never-null String unique to this audio connection.
     */
    @NotNull
    String getIdentifier();

    /**
     * Provides the current channel that this connection is transmitting to.
     *
     * @return The {@link AudioChannel} that this connection is sending to.
     */
    @NotNull
    AudioChannel getConnectedChannel();

    /**
     * The UDP connection for this audio connection. The {@link lonter.jfa.api.audio.factory.DefaultSendSystem DefaultSendSystem}
     * uses this socket to send audio packets to fluxer, and this is also the socket used to receive audio packets from fluxer.
     * <br>If you are implementing your own system, it is recommended that you used this connection as it is part of JFA's internal
     * system that JFA monitors for errors and closures. It should be noted however that using this is not required to
     * send audio packets if the developer wishes to open their own UDP socket to send from.
     *
     * @return The UDP socket connection used for audio sending.
     */
    @NotNull
    DatagramSocket getUdpSocket();

    /**
     * The connected socket address for this audio connection. This can be useful for developers
     * to open their own socket for datagram sending and allows to avoid using {@link #getNextPacket(boolean)}.
     *
     * @return {@link InetSocketAddress} of the current UDP connection
     */
    @NotNull
    InetSocketAddress getSocketAddress();

    /**
     * Used to retrieve an audio packet to send to Fluxer. The packet provided is already converted to Opus and
     * encrypted, and as such is completely ready to be sent to Fluxer.
     *
     * <p>The {@link java.nio.ByteBuffer#position()} will be positioned on the start of the packet to send
     * and the {@link java.nio.ByteBuffer#limit()} at the end of it. Use {@link java.nio.ByteBuffer#remaining()}
     * to check the length of the packet.
     *
     * <p><b>Note:</b> When the AudioSendHandler cannot or does not provide a new packet to send, this method will return null.
     *
     * <p><u>The buffer used here may be used again on the next call to this getter, if you plan on storing the data, copy it.
     * The buffer was created using {@link ByteBuffer#allocateDirect(int)} and is <b>always</b> direct.</u>
     *
     * @return Possibly-null {@link ByteBuffer} containing an encoded and encrypted packet
     *         of audio data ready to be sent to fluxer.
     */
    @Nullable
    ByteBuffer getNextPacketRaw(boolean unused);

    /**
     * Used to retrieve an audio packet to send to Fluxer. The packet provided is already converted to Opus and
     * encrypted, and as such is completely ready to be sent to Fluxer.
     *
     * <p><b>Note:</b> When the AudioSendHandler cannot or does not provide a new packet to send, this method will return null.
     *
     * @return Possibly-null {@link java.net.DatagramPacket DatagramPacket} containing an encoded and encrypted packet
     *         of audio data ready to be sent to fluxer.
     */
    @Nullable
    DatagramPacket getNextPacket(boolean unused);

    /**
     * This method is used to indicate a connection error to JFA so that the connection can be properly shutdown.
     * <br>This is useful if, during setup or operation, an unrecoverable error is encountered.
     *
     * @param  status
     *         The {@link lonter.jfa.api.audio.hooks.ConnectionStatus ConnectionStatus} being reported to JFA
     *         indicating an error with connection.
     */
    void onConnectionError(@NotNull ConnectionStatus status);

    /**
     * This method is used to indicate to JFA that the UDP connection has been lost, whether that be due internet loss
     * or some other unknown reason. This is similar to
     * {@link #onConnectionError(lonter.jfa.api.audio.hooks.ConnectionStatus)} as it provides a default error
     * reason of {@link lonter.jfa.api.audio.hooks.ConnectionStatus#ERROR_LOST_CONNECTION}.
     */
    void onConnectionLost();
}
