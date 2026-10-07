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

package lonter.jfa.api.utils;

import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lonter.jfa.annotations.UnknownNullability;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Utility methods for various aspects of the API.
 */
public class MiscUtil {
    /**
     * Returns the shard id the given guild will be loaded on for the given amount of shards.
     *
     * <p>Fluxer determines which guilds a shard is connect to using the following format:
     * {@code shardId == (guildId >>> 22) % totalShards}
     * <br>Source for formula: <a href="https://fluxer.com/developers/docs/topics/gateway#sharding">Fluxer Documentation</a>
     *
     * @param guildId
     *        The guild id.
     * @param shards
     *        The amount of shards.
     *
     * @return The shard id for the guild.
     */
    public static int getShardForGuild(long guildId, int shards) {
        return (int) ((guildId >>> 22) % shards);
    }

    /**
     * Returns the shard id the given guild will be loaded on for the given amount of shards.
     *
     * <p>Fluxer determines which guilds a shard is connect to using the following format:
     * {@code shardId == (guildId >>> 22) % totalShards}
     * <br>Source for formula: <a href="https://fluxer.com/developers/docs/topics/gateway#sharding">Fluxer Documentation</a>
     *
     * @param guildId
     *        The guild id.
     * @param shards
     *        The amount of shards.
     *
     * @return The shard id for the guild.
     */
    public static int getShardForGuild(@NotNull String guildId, int shards) {
        return getShardForGuild(parseSnowflake(guildId), shards);
    }

    /**
     * Returns the shard id the given {@link lonter.jfa.api.entities.Guild Guild} will be loaded on for the given amount of shards.
     *
     * <p>Fluxer determines which guilds a shard is connect to using the following format:
     * {@code shardId == (guildId >>> 22) % totalShards}
     * <br>Source for formula: <a href="https://fluxer.com/developers/docs/topics/gateway#sharding">Fluxer Documentation</a>
     *
     * @param guild
     *        The guild.
     * @param shards
     *        The amount of shards.
     *
     * @return The shard id for the guild.
     */
    public static int getShardForGuild(@NotNull Guild guild, int shards) {
        return getShardForGuild(guild.getIdLong(), shards);
    }

    /**
     * Generates a new thread-safe {@link gnu.trove.map.TLongObjectMap TLongObjectMap}
     *
     * @param  <T>
     *         The Object type
     *
     * @return a new thread-safe {@link gnu.trove.map.TLongObjectMap TLongObjectMap}
     */
    @NotNull
    public static <T> TLongObjectMap<T> newLongMap() {
        return new TSynchronizedLongObjectMap<>(new TLongObjectHashMap<T>(), new Object());
    }

    public static long parseLong(@NotNull String input) {
        if (input.startsWith("-")) {
            return Long.parseLong(input);
        } else {
            return Long.parseUnsignedLong(input);
        }
    }

    public static long parseSnowflake(@NotNull String input) {
        Checks.notEmpty(input, "ID");
        try {
            return parseLong(input);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException(Helpers.format(
                    "The specified ID is not a valid snowflake (%s). Expecting a valid long value!", input));
        }
    }

    @UnknownNullability
    public static <E> E locked(@NotNull ReentrantLock lock, @NotNull Supplier<E> task) {
        tryLock(lock);
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

    public static void locked(@NotNull ReentrantLock lock, @NotNull Runnable task) {
        tryLock(lock);
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tries to acquire the provided lock in a 10 second timeframe.
     *
     * @param  lock
     *         The lock to acquire
     *
     * @throws IllegalStateException
     *         If the lock could not be acquired
     */
    public static void tryLock(@NotNull Lock lock) {
        try {
            if (!lock.tryLock() && !lock.tryLock(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Could not acquire lock in a reasonable timeframe! (10 seconds)");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Unable to acquire lock while thread is interrupted!");
        }
    }

    /**
     * Can be used to append a String to a formatter.
     *
     * @param formatter
     *        The {@link java.util.Formatter Formatter}
     * @param width
     *        Minimum width to meet, filled with space if needed
     * @param precision
     *        Maximum amount of characters to append
     * @param leftJustified
     *        Whether or not to left-justify the value
     * @param out
     *        The String to append
     */
    public static void appendTo(
            @NotNull Formatter formatter, int width, int precision, boolean leftJustified, @NotNull String out) {
        try {
            Appendable appendable = formatter.out();
            if (precision > -1 && out.length() > precision) {
                appendable.append(Helpers.truncate(out, precision));
                return;
            }

            if (leftJustified) {
                appendable.append(Helpers.rightPad(out, width));
            } else {
                appendable.append(Helpers.leftPad(out, width));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
