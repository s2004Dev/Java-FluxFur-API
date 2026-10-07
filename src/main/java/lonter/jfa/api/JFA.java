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

package lonter.jfa.api;

import lonter.jfa.annotations.Incubating;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.attribute.IGuildChannelContainer;
import lonter.jfa.api.entities.channel.concrete.PrivateChannel;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.emoji.ApplicationEmoji;
import lonter.jfa.api.entities.emoji.CustomEmoji;
import lonter.jfa.api.entities.emoji.RichCustomEmoji;
import lonter.jfa.api.entities.sticker.*;
import lonter.jfa.api.events.GenericEvent;
import lonter.jfa.api.hooks.IEventManager;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.build.CommandData;
import lonter.jfa.api.interactions.commands.build.Commands;
import lonter.jfa.api.managers.ApplicationManager;
import lonter.jfa.api.managers.AudioManager;
import lonter.jfa.api.managers.DirectAudioController;
import lonter.jfa.api.managers.Presence;
import lonter.jfa.api.requests.GatewayIntent;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.*;
import lonter.jfa.api.requests.restaction.pagination.EntitlementPaginationAction;
import lonter.jfa.api.sharding.ShardManager;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.Once;
import lonter.jfa.api.utils.cache.CacheFlag;
import lonter.jfa.api.utils.cache.CacheView;
import lonter.jfa.api.utils.cache.SnowflakeCacheView;
import lonter.jfa.internal.interactions.CommandDataImpl;
import lonter.jfa.internal.requests.CompletedRestAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Matcher;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The core of JFA. Acts as a registry system of JFA. All parts of the API can be accessed starting from this class.
 *
 * @see JFABuilder
 */
public interface JFA extends IGuildChannelContainer<Channel> {
    /**
     * Represents the connection status of JFA and its Main WebSocket.
     */
    enum Status {
        /**JFA is currently setting up supporting systems like the AudioSystem.*/
        INITIALIZING(true),
        /**JFA has finished setting up supporting systems and is ready to log in.*/
        INITIALIZED(true),
        /**JFA is currently attempting to log in.*/
        LOGGING_IN(true),
        /**JFA is currently attempting to connect it's websocket to Fluxer.*/
        CONNECTING_TO_WEBSOCKET(true),
        /**JFA has successfully connected it's websocket to Fluxer and is sending authentication*/
        IDENTIFYING_SESSION(true),
        /**JFA has sent authentication to fluxer and is awaiting confirmation*/
        AWAITING_LOGIN_CONFIRMATION(true),
        /**JFA is populating internal objects.
         * This process often takes the longest of all Statuses (besides CONNECTED)*/
        LOADING_SUBSYSTEMS(true),
        /**JFA has finished loading everything, is receiving information from Fluxer and is firing events.*/
        CONNECTED(true),
        /**JFA's main websocket has been disconnected. This <b>DOES NOT</b> mean JFA has shutdown permanently.
         * This is an in-between status. Most likely ATTEMPTING_TO_RECONNECT or SHUTTING_DOWN/SHUTDOWN will soon follow.*/
        DISCONNECTED,
        /** JFA session has been added to {@link lonter.jfa.api.utils.SessionController SessionController}
         * and is awaiting to be dequeued for reconnecting.*/
        RECONNECT_QUEUED,
        /**When trying to reconnect to Fluxer JFA encountered an issue, most likely related to a lack of internet connection,
         * and is waiting to try reconnecting again.*/
        WAITING_TO_RECONNECT,
        /**JFA has been disconnected from Fluxer and is currently trying to reestablish the connection.*/
        ATTEMPTING_TO_RECONNECT,
        /**JFA has received a shutdown request or has been disconnected from Fluxer and reconnect is disabled, thus,
         * JFA is in the process of shutting down*/
        SHUTTING_DOWN,
        /**JFA has finished shutting down and this instance can no longer be used to communicate with the Fluxer servers.*/
        SHUTDOWN,
        /**While attempting to authenticate, Fluxer reported that the provided authentication information was invalid.*/
        FAILED_TO_LOGIN;

        private final boolean isInit;

        Status(boolean isInit) {
            this.isInit = isInit;
        }

        Status() {
            this.isInit = false;
        }

        public boolean isInit() {
            return isInit;
        }
    }

    /**
     * Represents the information used to create this shard.
     */
    class ShardInfo {
        /** Default sharding config with one shard */
        public static final ShardInfo SINGLE = new ShardInfo(0, 1);

        int shardId;
        int shardTotal;

        public ShardInfo(int shardId, int shardTotal) {
            this.shardId = shardId;
            this.shardTotal = shardTotal;
        }

        /**
         * Represents the id of the shard of the current instance.
         * <br>This value will be between 0 and ({@link #getShardTotal()} - 1).
         *
         * @return The id of the currently logged in shard.
         */
        public int getShardId() {
            return shardId;
        }

        /**
         * The total amount of shards based on the value provided during JFA instance creation using
         * {@link JFABuilder#useSharding(int, int)}.
         * <br>This <b>does not</b> query Fluxer to determine the total number of shards.
         * <br>This <b>does not</b> represent the amount of logged in shards.
         * <br>It strictly represents the integer value provided to fluxer
         * representing the total amount of shards that the developer indicated that it was going to use when
         * initially starting JFA.
         *
         * @return The total of shards based on the total provided by the developer during JFA initialization.
         */
        public int getShardTotal() {
            return shardTotal;
        }

        /**
         * Provides a shortcut method for easily printing shard info.
         * <br>Format: "[# / #]"
         * <br>Where the first # is shardId and the second # is shardTotal.
         *
         * @return A String representing the information used to build this shard.
         */
        @NotNull
        public String getShardString() {
            return "[" + shardId + " / " + shardTotal + "]";
        }

        @NotNull
        @Override
        public String toString() {
            return new EntityString(this)
                    .addMetadata("currentShard", getShardString())
                    .addMetadata("totalShards", getShardTotal())
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ShardInfo)) {
                return false;
            }

            ShardInfo oInfo = (ShardInfo) o;
            return shardId == oInfo.getShardId() && shardTotal == oInfo.getShardTotal();
        }

        @Override
        public int hashCode() {
            return Objects.hash(shardId, shardTotal);
        }
    }

    /**
     * Gets the current {@link lonter.jfa.api.JFA.Status Status} of the JFA instance.
     *
     * @return Current JFA status.
     */
    @NotNull
    Status getStatus();

    /**
     * The {@link GatewayIntent GatewayIntents} for this JFA session.
     *
     * @return {@link EnumSet} of active gateway intents
     */
    @NotNull
    EnumSet<GatewayIntent> getGatewayIntents();

    /**
     * The {@link CacheFlag cache flags} that have been enabled for this JFA session.
     *
     * @return Copy of the EnumSet of cache flags for this session
     */
    @NotNull
    EnumSet<CacheFlag> getCacheFlags();

    /**
     * Attempts to remove the user with the provided id from the cache.
     * <br>If you attempt to remove the {@link #getSelfUser() SelfUser} this will simply return {@code false}.
     *
     * <p>This should be used by an implementation of {@link lonter.jfa.api.utils.MemberCachePolicy MemberCachePolicy}
     * as an upstream request to remove a member.
     *
     * @param  userId
     *         The target user id
     *
     * @return True, if the cache was changed
     */
    boolean unloadUser(long userId);

    /**
     * The time in milliseconds that fluxer took to respond to our last heartbeat
     * <br>This roughly represents the WebSocket ping of this session
     *
     * <p><b>{@link lonter.jfa.api.requests.RestAction RestAction} request times do not
     * correlate to this value!</b>
     *
     * <p>The {@link lonter.jfa.api.events.GatewayPingEvent GatewayPingEvent} indicates an update to this value.
     *
     * @return time in milliseconds between heartbeat and the heartbeat ack response
     *
     * @see    #getRestPing() Getting RestAction ping
     */
    long getGatewayPing();

    /**
     * The time in milliseconds that fluxer took to respond to a REST request.
     * <br>This will request the current user from the API and calculate the time the response took.
     *
     * <p><b>Example</b><br>
     * {@snippet lang="java":
     * jfa.getRestPing().queue((time) ->
     *     channel.sendMessageFormat("Ping: %d ms", time).queue()
     * );
     * }
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: long
     *
     * @see    #getGatewayPing()
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Long> getRestPing() {
        AtomicLong time = new AtomicLong();
        Route.CompiledRoute route = Route.Self.GET_SELF.compile();
        RestActionImpl<Long> action =
                new RestActionImpl<>(this, route, (response, request) -> System.currentTimeMillis() - time.get());
        action.setCheck(() -> {
            time.set(System.currentTimeMillis());
            return true;
        });
        return action;
    }

    /**
     * This method will block until JFA has reached the specified connection status.
     *
     * <p><b>Login Cycle</b><br>
     * <ol>
     *  <li>{@link lonter.jfa.api.JFA.Status#INITIALIZING INITIALIZING}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#INITIALIZED INITIALIZED}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#LOGGING_IN LOGGING_IN}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#CONNECTING_TO_WEBSOCKET CONNECTING_TO_WEBSOCKET}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#IDENTIFYING_SESSION IDENTIFYING_SESSION}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#AWAITING_LOGIN_CONFIRMATION AWAITING_LOGIN_CONFIRMATION}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#LOADING_SUBSYSTEMS LOADING_SUBSYSTEMS}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#CONNECTED CONNECTED}</li>
     * </ol>
     *
     * @param  status
     *         The init status to wait for, once JFA has reached the specified
     *         stage of the startup cycle this method will return.
     *
     * @throws InterruptedException
     *         If this thread is interrupted while waiting
     * @throws IllegalArgumentException
     *         If the provided status is null or not an init status ({@link Status#isInit()})
     * @throws IllegalStateException
     *         If JFA is shutdown during this wait period
     *
     * @return The current JFA instance, for chaining convenience
     */
    @NotNull
    default JFA awaitStatus(@NotNull JFA.Status status) throws InterruptedException {
        // This is done to retain backwards compatible ABI as it would otherwise change the
        // signature of the method
        // which would require recompilation for all users (including extension libraries)
        return awaitStatus(status, new JFA.Status[0]);
    }

    /**
     * This method will block until JFA has reached the specified connection status.
     *
     * <p><b>Login Cycle</b><br>
     * <ol>
     *  <li>{@link lonter.jfa.api.JFA.Status#INITIALIZING INITIALIZING}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#INITIALIZED INITIALIZED}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#LOGGING_IN LOGGING_IN}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#CONNECTING_TO_WEBSOCKET CONNECTING_TO_WEBSOCKET}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#IDENTIFYING_SESSION IDENTIFYING_SESSION}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#AWAITING_LOGIN_CONFIRMATION AWAITING_LOGIN_CONFIRMATION}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#LOADING_SUBSYSTEMS LOADING_SUBSYSTEMS}</li>
     *  <li>{@link lonter.jfa.api.JFA.Status#CONNECTED CONNECTED}</li>
     * </ol>
     *
     * @param  status
     *         The init status to wait for, once JFA has reached the specified
     *         stage of the startup cycle this method will return.
     * @param  failOn
     *         Optional failure states that will force a premature return
     *
     * @throws InterruptedException
     *         If this thread is interrupted while waiting
     * @throws IllegalArgumentException
     *         If the provided status is null or not an init status ({@link Status#isInit()})
     * @throws IllegalStateException
     *         If JFA is shutdown during this wait period
     *
     * @return The current JFA instance, for chaining convenience
     */
    @NotNull
    JFA awaitStatus(@NotNull JFA.Status status, @NotNull JFA.Status... failOn) throws InterruptedException;

    /**
     * This method will block until JFA has reached the status {@link Status#CONNECTED}.
     * <br>This status means that JFA finished setting up its internal cache and is ready to be used.
     *
     * @throws InterruptedException
     *         If this thread is interrupted while waiting
     * @throws IllegalStateException
     *         If JFA is shutdown during this wait period
     *
     * @return The current JFA instance, for chaining convenience
     */
    @NotNull
    default JFA awaitReady() throws InterruptedException {
        return awaitStatus(Status.CONNECTED);
    }

    /**
     * Blocks the current thread until {@link #getStatus()} returns {@link Status#SHUTDOWN}.
     * <br>This can be useful in certain situations like disabling class loading.
     *
     * <p>Note that shutdown time depends on the length of the rate-limit queue.
     * You can use {@link #shutdownNow()} to cancel all pending requests and immediately shutdown.
     *
     * <p><b>Example</b>
     * {@snippet lang="java":
     * jfa.shutdown();
     * // Allow at most 10 seconds for remaining requests to finish
     * if (!jfa.awaitShutdown(10, TimeUnit.SECONDS)) {
     *     jfa.shutdownNow(); // Cancel all remaining requests
     *     jfa.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
     * }
     * }
     *
     * <p><b>This will not implicitly call {@code shutdown()}, you are responsible to ensure that the shutdown process has started.</b>
     *
     * @param  duration
     *         The maximum time to wait, or 0 to wait indefinitely
     * @param  unit
     *         The time unit for the duration
     *
     * @throws IllegalArgumentException
     *         If the provided unit is null
     * @throws InterruptedException
     *         If the current thread is interrupted while waiting
     *
     * @return False, if the timeout has elapsed before the shutdown has completed, true otherwise.
     */
    @CheckReturnValue
    boolean awaitShutdown(long duration, @NotNull TimeUnit unit) throws InterruptedException;

    /**
     * Blocks the current thread until {@link #getStatus()} returns {@link Status#SHUTDOWN}.
     * <br>This can be useful in certain situations like disabling class loading.
     *
     * <p>Note that shutdown time depends on the length of the rate-limit queue.
     * You can use {@link #shutdownNow()} to cancel all pending requests and immediately shutdown.
     *
     * <p><b>Example</b>
     * {@snippet lang="java":
     * jfa.shutdown();
     * // Allow at most 10 seconds for remaining requests to finish
     * if (!jfa.awaitShutdown(Duration.ofSeconds(10))) {
     *     jfa.shutdownNow(); // Cancel all remaining requests
     *     jfa.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
     * }
     * }
     *
     * <p><b>This will not implicitly call {@code shutdown()}, you are responsible to ensure that the shutdown process has started.</b>
     *
     * @param  timeout
     *         The maximum time to wait, or {@link Duration#ZERO} to wait indefinitely
     *
     * @throws IllegalArgumentException
     *         If the provided timeout is null
     * @throws InterruptedException
     *         If the current thread is interrupted while waiting
     *
     * @return False, if the timeout has elapsed before the shutdown has completed, true otherwise.
     */
    @CheckReturnValue
    default boolean awaitShutdown(@NotNull Duration timeout) throws InterruptedException {
        Checks.notNull(timeout, "Timeout");
        return awaitShutdown(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Blocks the current thread until {@link #getStatus()} returns {@link Status#SHUTDOWN}.
     * <br>This can be useful in certain situations like disabling class loading.
     *
     * <p>This will wait indefinitely by default. Use {@link #awaitShutdown(Duration)} to set a timeout.
     *
     * <p>Note that shutdown time depends on the length of the rate-limit queue.
     * You can use {@link #shutdownNow()} to cancel all pending requests and immediately shutdown.
     *
     * <p><b>Example</b>
     * {@snippet lang="java":
     * jfa.shutdown();
     * // Allow at most 10 seconds for remaining requests to finish
     * if (!jfa.awaitShutdown(Duration.ofSeconds(10))) {
     *     jfa.shutdownNow(); // Cancel all remaining requests
     *     jfa.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
     * }
     * }
     *
     * <p><b>This will not implicitly call {@code shutdown()}, you are responsible to ensure that the shutdown process has started.</b>
     *
     * @throws IllegalArgumentException
     *         If the provided timeout is null
     * @throws InterruptedException
     *         If the current thread is interrupted while waiting
     *
     * @return Always true
     */
    default boolean awaitShutdown() throws InterruptedException {
        return awaitShutdown(0, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancels all currently scheduled {@link RestAction} requests.
     * <br>When a {@link RestAction} is cancelled, a {@link java.util.concurrent.CancellationException} will be provided
     * to the failure callback. This means {@link RestAction#queue(Consumer, Consumer)} will invoke the second callback
     * and {@link RestAction#complete()} will throw an exception.
     *
     * <p><b>This is only recommended as an extreme last measure to avoid backpressure.</b>
     * If you want to stop requests on shutdown you should use {@link #shutdownNow()} instead of this method.
     *
     * @return how many requests were cancelled
     *
     * @see    RestAction#setCheck(BooleanSupplier)
     */
    int cancelRequests();

    /**
     * {@link ScheduledExecutorService} used to handle rate-limits for {@link RestAction}
     * executions. This is also used in other parts of JFA related to http requests.
     *
     * @return The {@link ScheduledExecutorService} used for http request handling
     */
    @NotNull
    ScheduledExecutorService getRateLimitPool();

    /**
     * {@link ScheduledExecutorService} used to send WebSocket messages to fluxer.
     * <br>This involves initial setup of guilds as well as keeping the connection alive.
     *
     * @return The {@link ScheduledExecutorService} used for WebSocket transmissions
     */
    @NotNull
    ScheduledExecutorService getGatewayPool();

    /**
     * {@link ExecutorService} used to handle {@link RestAction} callbacks
     * and completions. This is also used for handling {@link lonter.jfa.api.entities.Message.Attachment} downloads
     * when needed.
     * <br>By default this uses the {@link ForkJoinPool#commonPool() CommonPool} of the runtime.
     *
     * @return The {@link ExecutorService} used for callbacks
     */
    @NotNull
    ExecutorService getCallbackPool();

    /**
     * The {@link OkHttpClient} used for handling http requests from {@link RestAction RestActions}.
     *
     * @return The http client
     */
    @NotNull
    OkHttpClient getHttpClient();

    /**
     * Direct access to audio (dis-)connect requests.
     * <br>This should not be used when normal audio operation is desired.
     *
     * <p>The correct way to open and close an audio connection is through the {@link Guild Guild's}
     * {@link AudioManager}.
     *
     * @throws IllegalStateException
     *         If {@link GatewayIntent#GUILD_VOICE_STATES} is disabled
     *
     * @return The {@link DirectAudioController} for this JFA instance
     */
    @NotNull
    DirectAudioController getDirectAudioController();

    /**
     * Changes the internal EventManager.
     *
     * <p>The default EventManager is {@link lonter.jfa.api.hooks.InterfacedEventManager InterfacedEventListener}.
     * <br>There is also an {@link lonter.jfa.api.hooks.AnnotatedEventManager AnnotatedEventManager} available.
     *
     * @param  manager
     *         The new EventManager to use
     */
    void setEventManager(@Nullable IEventManager manager);

    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
     * This uses the {@link lonter.jfa.api.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link lonter.jfa.api.hooks.AnnotatedEventManager AnnotatedEventManager}, use {@link #setEventManager(IEventManager)}.
     *
     * <p>Note: when using the {@link lonter.jfa.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link lonter.jfa.api.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) which will react to events.
     *
     * @throws java.lang.IllegalArgumentException
     *         If either listeners or one of it's objects is {@code null}.
     */
    void addEventListener(@NotNull Object... listeners);

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param  listeners
     *         The listener(s) to be removed.
     *
     * @throws java.lang.IllegalArgumentException
     *         If either listeners or one of it's objects is {@code null}.
     */
    void removeEventListener(@NotNull Object... listeners);

    /**
     * Immutable List of Objects that have been registered as EventListeners.
     *
     * @return List of currently registered Objects acting as EventListeners.
     */
    @NotNull
    List<Object> getRegisteredListeners();

    /**
     * Returns a reusable builder for a one-time event listener.
     *
     * <p>Note that this method only works if the {@link JFABuilder#setEventManager(IEventManager) event manager}
     * is either the {@link lonter.jfa.api.hooks.InterfacedEventManager InterfacedEventManager}
     * or {@link lonter.jfa.api.hooks.AnnotatedEventManager AnnotatedEventManager}.
     * <br>Other implementations can support it as long as they call
     * {@link lonter.jfa.api.hooks.EventListener#onEvent(GenericEvent) EventListener.onEvent(GenericEvent)}.
     *
     * <p><b>Example:</b>
     *
     * <p>Listening to a message from a channel and a user, after using a slash command:
     * {@snippet lang="java":
     * final Duration timeout = Duration.ofSeconds(5);
     * event.reply("Reply in " + TimeFormat.RELATIVE.after(timeout) + " if you can!")
     *         .setEphemeral(true)
     *         .queue();
     *
     * event.getJFA().listenOnce(MessageReceivedEvent.class)
     *         .filter(messageEvent -> messageEvent.getChannel().getIdLong() == event.getChannel().getIdLong())
     *         .filter(messageEvent -> messageEvent.getAuthor().getIdLong() == event.getUser().getIdLong())
     *         .timeout(timeout, () -> {
     *             event.getHook().editOriginal("Timeout!").queue();
     *         })
     *         .subscribe(messageEvent -> {
     *             event.getHook().editOriginal("You sent: " + messageEvent.getMessage().getContentRaw()).queue();
     *         });
     * }
     *
     * @param  eventType
     *         Type of the event to listen to
     *
     * @throws IllegalArgumentException
     *         If the provided event type is {@code null}
     *
     * @return The one-time event listener builder
     */
    @NotNull
    @CheckReturnValue
    <E extends GenericEvent> Once.Builder<E> listenOnce(@NotNull Class<E> eventType);

    /**
     * Retrieves the list of global commands.
     * <br>This list does not include guild commands! Use {@link Guild#retrieveCommands()} for guild commands.
     * <br>This list does not include localization data. Use {@link #retrieveCommands(boolean)} to get localization data
     *
     * @return {@link RestAction} - Type: {@link List} of {@link Command}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<List<Command>> retrieveCommands() {
        return retrieveCommands(false);
    }

    /**
     * Retrieves the list of global commands.
     * <br>This list does not include guild commands! Use {@link Guild#retrieveCommands()} for guild commands.
     *
     * @param  withLocalizations
     *         {@code true} if the localization data (such as name and description) should be included
     *
     * @return {@link RestAction} - Type: {@link List} of {@link Command}
     */
    @NotNull
    @CheckReturnValue
    RestAction<List<Command>> retrieveCommands(boolean withLocalizations);

    /**
     * Retrieves the existing {@link Command} instance by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The command id
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link Command}
     */
    @NotNull
    @CheckReturnValue
    RestAction<Command> retrieveCommandById(@NotNull String id);

    /**
     * Retrieves the existing {@link Command} instance by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The command id
     *
     * @return {@link RestAction} - Type: {@link Command}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Command> retrieveCommandById(long id) {
        return retrieveCommandById(Long.toUnsignedString(id));
    }

    /**
     * Creates or updates a global command.
     * <br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>To specify a complete list of all commands you can use {@link #updateCommands()} instead.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * @param  command
     *         The {@link CommandData} for the command
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link RestAction} - Type: {@link Command}
     *         <br>The RestAction used to create or update the command
     *
     * @see    Commands#slash(String, String) Commands.slash(...)
     * @see    Commands#message(String) Commands.message(...)
     * @see    Commands#user(String) Commands.user(...)
     * @see    Guild#upsertCommand(CommandData) Guild.upsertCommand(...)
     */
    @NotNull
    @CheckReturnValue
    RestAction<Command> upsertCommand(@NotNull CommandData command);

    /**
     * Creates or updates a global slash command.
     * <br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>To specify a complete list of all commands you can use {@link #updateCommands()} instead.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     * @param  description
     *         The description for the command, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name/description do not meet the requirements
     *
     * @return {@link CommandCreateAction}
     *
     * @see Guild#upsertCommand(String, String)
     */
    @NotNull
    @CheckReturnValue
    default CommandCreateAction upsertCommand(@NotNull String name, @NotNull String description) {
        return (CommandCreateAction) upsertCommand(new CommandDataImpl(name, description));
    }

    /**
     * Configures the complete list of global commands.
     * <br>This will replace the existing command list for this bot. You should only use this once on startup!
     *
     * <p>This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * <p><b>Examples</b>
     *
     * <p>Set list to 2 commands:
     * {@snippet lang="java":
     * jfa.updateCommands()
     *   .addCommands(Commands.slash("ping", "Gives the current ping"))
     *   .addCommands(Commands.slash("ban", "Ban the target user")
     *     .setContexts(InteractionContextType.GUILD)
     *     .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
     *     .addOption(OptionType.USER, "user", "The user to ban", true))
     *   .queue();
     * }
     *
     * <p>Delete all commands:
     * {@snippet lang="java":
     * jfa.updateCommands().queue();
     * }
     *
     * @return {@link CommandListUpdateAction}
     *
     * @see    Guild#updateCommands()
     */
    @NotNull
    @CheckReturnValue
    CommandListUpdateAction updateCommands();

    /**
     * Edit an existing global command by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  type
     *         The command type
     * @param  id
     *         The id of the command to edit
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake or the type is {@link Command.Type#UNKNOWN}
     *
     * @return {@link CommandEditAction} used to edit the command
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction editCommandById(@NotNull Command.Type type, @NotNull String id);

    /**
     * Edit an existing global command by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  type
     *         The command type
     * @param  id
     *         The id of the command to edit
     *
     * @throws IllegalArgumentException
     *         If the type is {@link Command.Type#UNKNOWN}
     *
     * @return {@link CommandEditAction} used to edit the command
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction editCommandById(@NotNull Command.Type type, long id) {
        return editCommandById(type, Long.toUnsignedString(id));
    }

    /**
     * Delete the global command for this id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  commandId
     *         The id of the command that should be deleted
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> deleteCommandById(@NotNull String commandId);

    /**
     * Delete the global command for this id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  commandId
     *         The id of the command that should be deleted
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> deleteCommandById(long commandId) {
        return deleteCommandById(Long.toUnsignedString(commandId));
    }

    /**
     * Retrieves the currently configured {@link RoleConnectionMetadata} records for this application.
     *
     * @return {@link RestAction} - Type: {@link List} of {@link RoleConnectionMetadata}
     *
     * @see <a href="https://fluxer.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles" target="_blank">Configuring App Metadata for Linked Roles</a>
     */
    @NotNull
    @CheckReturnValue
    RestAction<List<RoleConnectionMetadata>> retrieveRoleConnectionMetadata();

    /**
     * Updates the currently configured {@link RoleConnectionMetadata} records for this application.
     *
     * <p>Returns the updated connection metadata records on success.
     *
     * @param  records
     *         The new records to set
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than {@value RoleConnectionMetadata#MAX_RECORDS} records are configured.
     *
     * @return {@link RestAction} - Type: {@link List} of {@link RoleConnectionMetadata}
     *
     * @see <a href="https://fluxer.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles" target="_blank">Configuring App Metadata for Linked Roles</a>
     */
    @NotNull
    @CheckReturnValue
    RestAction<List<RoleConnectionMetadata>> updateRoleConnectionMetadata(
            @NotNull Collection<? extends RoleConnectionMetadata> records);

    /**
     * {@link lonter.jfa.api.utils.cache.CacheView CacheView} of
     * all cached {@link lonter.jfa.api.managers.AudioManager AudioManagers} created for this JFA instance.
     * <br>AudioManagers are created when first retrieved via {@link Guild#getAudioManager() Guild.getAudioManager()}.
     * <u>Using this will perform better than calling {@code Guild.getAudioManager()} iteratively as that would cause many useless audio managers to be created!</u>
     *
     * <p>AudioManagers are cross-session persistent!
     *
     * @return {@link lonter.jfa.api.utils.cache.CacheView CacheView}
     */
    @NotNull
    CacheView<AudioManager> getAudioManagerCache();

    /**
     * Immutable list of all created {@link lonter.jfa.api.managers.AudioManager AudioManagers} for this JFA instance!
     *
     * @return Immutable list of all created AudioManager instances
     */
    @NotNull
    @Unmodifiable
    default List<AudioManager> getAudioManagers() {
        return getAudioManagerCache().asList();
    }

    /**
     * {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all <b>cached</b> {@link lonter.jfa.api.entities.User Users} visible to this JFA session.
     *
     * @return {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<User> getUserCache();

    /**
     * An immutable list of all {@link lonter.jfa.api.entities.User Users} that share a
     * {@link Guild Guild} with the currently logged in account.
     * <br>This list will never contain duplicates and represents all
     * {@link lonter.jfa.api.entities.User Users} that JFA can currently see.
     *
     * <p><b>This will only check cached users!</b>
     *
     * <p>If the developer is sharding, then only users from guilds connected to the specifically logged in
     * shard will be returned in the List.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getUserCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Immutable list of all {@link lonter.jfa.api.entities.User Users} that are visible to JFA.
     */
    @NotNull
    @Unmodifiable
    default List<User> getUsers() {
        return getUserCache().asList();
    }

    /**
     * This returns the {@link lonter.jfa.api.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * <p><b>This will only check cached users!</b>
     *
     * @param  id
     *         The id of the requested {@link lonter.jfa.api.entities.User User}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.User User} with matching id.
     *
     * @see    #retrieveUserById(String)
     */
    @Nullable
    default User getUserById(@NotNull String id) {
        return getUserCache().getElementById(id);
    }

    /**
     * This returns the {@link lonter.jfa.api.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * <p><b>This will only check cached users!</b>
     *
     * @param  id
     *         The id of the requested {@link lonter.jfa.api.entities.User User}.
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.User User} with matching id.
     *
     * @see    #retrieveUserById(long)
     */
    @Nullable
    default User getUserById(long id) {
        return getUserCache().getElementById(id);
    }

    /**
     * Searches for a user that has the matching Fluxer Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     * <p>This only checks users that are known to the currently logged in account (shard). If a user exists
     * with the tag that is not available in the {@link #getUserCache() User-Cache} it will not be detected.
     * <br>Currently Fluxer does not offer a way to retrieve a user by their fluxer tag.
     *
     * <p><b>This will only check cached users!</b>
     *
     * <p>To check users without discriminators, use {@code username#0000} instead.
     *
     * @param  tag
     *         The Fluxer Tag in the format {@code Username#Discriminator}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided tag is null or not in the described format
     *
     * @return The {@link lonter.jfa.api.entities.User} for the fluxer tag or null if no user has the provided tag
     */
    @Nullable
    default User getUserByTag(@NotNull String tag) {
        Checks.notNull(tag, "Tag");
        Matcher matcher = User.USER_TAG.matcher(tag);
        Checks.check(matcher.matches(), "Invalid tag format!");
        String username = matcher.group(1);
        String discriminator = matcher.group(2);
        return getUserByTag(username, discriminator);
    }

    /**
     * Searches for a user that has the matching Fluxer Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     *
     * <p>This only checks users that are known to the currently logged in account (shard). If a user exists
     * with the tag that is not available in the {@link #getUserCache() User-Cache} it will not be detected.
     * <br>Currently Fluxer does not offer a way to retrieve a user by their fluxer tag.
     *
     * <p><b>This will only check cached users!</b>
     *
     * @param  username
     *         The name of the user
     * @param  discriminator
     *         The discriminator of the user
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided arguments are null or not in the described format
     *
     * @return The {@link lonter.jfa.api.entities.User} for the fluxer tag or null if no user has the provided tag
     */
    @Nullable
    default User getUserByTag(@NotNull String username, @Nullable String discriminator) {
        Checks.inRange(username, 2, 32, "Username");
        Checks.check(
                discriminator == null || discriminator.length() == 4 && Helpers.isNumeric(discriminator),
                "Invalid format for discriminator! Provided: %s",
                discriminator);
        String actualDiscriminator = discriminator == null ? "0000" : discriminator;
        return getUserCache()
                .applyStream(stream -> stream.filter(it -> it.getDiscriminator().equals(actualDiscriminator))
                        .filter(it -> it.getName().equals(username))
                        .findFirst()
                        .orElse(null));
    }

    /**
     * This immutable returns all {@link lonter.jfa.api.entities.User Users} that have the same username as the one provided.
     * <br>If there are no {@link lonter.jfa.api.entities.User Users} with the provided name, then this returns an empty list.
     *
     * <p><b>This will only check cached users!</b>
     *
     * <p><b>Note: </b> This does **not** consider nicknames, it only considers {@link lonter.jfa.api.entities.User#getName()}
     *
     * @param  name
     *         The name of the requested {@link lonter.jfa.api.entities.User Users}.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link lonter.jfa.api.entities.User#getName()}.
     *
     * @return Possibly-empty immutable list of {@link lonter.jfa.api.entities.User Users} that all have the same name as the provided name.
     *
     * @incubating This will be replaced in the future when the rollout of globally unique usernames has been completed.
     */
    @NotNull
    @Incubating
    @Unmodifiable
    default List<User> getUsersByName(@NotNull String name, boolean ignoreCase) {
        return getUserCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets all {@link Guild Guilds} that contain all given users as their members.
     *
     * @param  users
     *         The users which all the returned {@link Guild Guilds} must contain.
     *
     * @return Immutable list of all {@link Guild Guild} instances which have all {@link lonter.jfa.api.entities.UserSnowflake Users} in them.
     *
     * @see    Guild#isMember(UserSnowflake)
     */
    @NotNull
    @Unmodifiable
    List<Guild> getMutualGuilds(@NotNull UserSnowflake... users);

    /**
     * Gets all {@link Guild Guilds} that contain all given users as their members.
     *
     * @param users
     *        The users which all the returned {@link Guild Guilds} must contain.
     *
     * @return Immutable list of all {@link Guild Guild} instances which have all {@link lonter.jfa.api.entities.UserSnowflake Users} in them.
     */
    @NotNull
    @Unmodifiable
    List<Guild> getMutualGuilds(@NotNull Collection<? extends UserSnowflake> users);

    /**
     * Attempts to retrieve a {@link lonter.jfa.api.entities.User User} object based on the provided id.
     *
     * <p>If {@link #getUserById(long)} is cached, this will directly return the user in a completed {@link RestAction} without making a request.
     * When both {@link lonter.jfa.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} and {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intents
     * are disabled this will always make a request even if the user is cached.
     * You can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * <p>The returned {@link lonter.jfa.api.requests.RestAction RestAction} can encounter the following Fluxer errors:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_USER ErrorResponse.UNKNOWN_USER}
     *     <br>Occurs when the provided id does not refer to a {@link lonter.jfa.api.entities.User User}
     *     known by Fluxer. Typically occurs when developers provide an incomplete id (cut short).</li>
     * </ul>
     *
     * @param  id
     *         The id of the requested {@link lonter.jfa.api.entities.User User}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided id String is null.</li>
     *             <li>If the provided id String is empty.</li>
     *         </ul>
     *
     * @return {@link CacheRestAction} - Type: {@link User}
     *         <br>On request, gets the User with id matching provided id from Fluxer.
     */
    @NotNull
    @CheckReturnValue
    default CacheRestAction<User> retrieveUserById(@NotNull String id) {
        return retrieveUserById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Attempts to retrieve a {@link lonter.jfa.api.entities.User User} object based on the provided id.
     *
     * <p>If {@link #getUserById(long)} is cached, this will directly return the user in a completed {@link RestAction} without making a request.
     * When both {@link lonter.jfa.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} and {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intents
     * are disabled this will always make a request even if the user is cached.
     * You can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * <p>The returned {@link lonter.jfa.api.requests.RestAction RestAction} can encounter the following Fluxer errors:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_USER ErrorResponse.UNKNOWN_USER}
     *     <br>Occurs when the provided id does not refer to a {@link lonter.jfa.api.entities.User User}
     *     known by Fluxer. Typically occurs when developers provide an incomplete id (cut short).</li>
     * </ul>
     *
     * @param  id
     *         The id of the requested {@link User}.
     *
     * @return {@link CacheRestAction} - Type: {@link User}
     *         <br>On request, gets the User with id matching provided id from Fluxer.
     */
    @NotNull
    @CheckReturnValue
    CacheRestAction<User> retrieveUserById(long id);

    /**
     * {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link Guild Guilds} visible to this JFA session.
     *
     * @return {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<Guild> getGuildCache();

    /**
     * An immutable List of all {@link Guild Guilds} that the logged account is connected to.
     * <br>If this account is not connected to any {@link Guild Guilds}, this will return an empty list.
     *
     * <p>If the developer is sharding ({@link lonter.jfa.api.JFABuilder#useSharding(int, int)}, then this list
     * will only contain the {@link Guild Guilds} that the shard is actually connected to.
     * Fluxer determines which guilds a shard is connect to using the following format:
     * <br>Guild connected if shardId == (guildId {@literal >>} 22) % totalShards;
     * <br>Source for formula: <a href="https://fluxer.com/developers/docs/topics/gateway#sharding">Fluxer Documentation</a>
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getGuildCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possibly-empty immutable list of all the {@link Guild Guilds} that this account is connected to.
     */
    @NotNull
    @Unmodifiable
    default List<Guild> getGuilds() {
        return getGuildCache().asList();
    }

    /**
     * This returns the {@link Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link Guild Guild}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link Guild Guild} with matching id.
     */
    @Nullable
    default Guild getGuildById(@NotNull String id) {
        return getGuildCache().getElementById(id);
    }

    /**
     * This returns the {@link Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link Guild Guild}.
     *
     * @return Possibly-null {@link Guild Guild} with matching id.
     */
    @Nullable
    default Guild getGuildById(long id) {
        return getGuildCache().getElementById(id);
    }

    /**
     * An immutable list of all {@link Guild Guilds} that have the same name as the one provided.
     * <br>If there are no {@link Guild Guilds} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name of the requested {@link Guild Guilds}.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link Guild#getName()}.
     *
     * @return Possibly-empty immutable list of all the {@link Guild Guilds} that all have the same name as the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<Guild> getGuildsByName(@NotNull String name, boolean ignoreCase) {
        return getGuildCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Set of {@link Guild} IDs for guilds that were marked unavailable by the gateway.
     * <br>When a guild becomes unavailable a {@link lonter.jfa.api.events.guild.GuildUnavailableEvent GuildUnavailableEvent}
     * is emitted and a {@link lonter.jfa.api.events.guild.GuildAvailableEvent GuildAvailableEvent} is emitted
     * when it becomes available again. During the time a guild is unavailable it its not reachable through
     * cache such as {@link #getGuildById(long)}.
     *
     * @return Possibly-empty set of guild IDs for unavailable guilds
     */
    @NotNull
    Set<String> getUnavailableGuilds();

    /**
     * Whether the guild is unavailable. If this returns true, the guild id should be in {@link #getUnavailableGuilds()}.
     *
     * @param  guildId
     *         The guild id
     *
     * @return True, if this guild is unavailable
     */
    boolean isUnavailable(long guildId);

    /**
     * Unified {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link lonter.jfa.api.entities.Role Roles} visible to this JFA session.
     *
     * @return Unified {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     *
     * @see    lonter.jfa.api.utils.cache.CacheView#allSnowflakes(java.util.function.Supplier) CacheView.allSnowflakes(...)
     */
    @NotNull
    SnowflakeCacheView<Role> getRoleCache();

    /**
     * All {@link lonter.jfa.api.entities.Role Roles} this JFA instance can see. <br>This will iterate over each
     * {@link Guild Guild} retrieved from {@link #getGuilds()} and collect its {@link
     * Guild#getRoles() Guild.getRoles()}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getRoleCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Immutable List of all visible Roles
     */
    @NotNull
    @Unmodifiable
    default List<Role> getRoles() {
        return getRoleCache().asList();
    }

    /**
     * Retrieves the {@link lonter.jfa.api.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     *         The id of the searched Role
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.Role Role} for the specified ID
     */
    @Nullable
    default Role getRoleById(@NotNull String id) {
        return getRoleCache().getElementById(id);
    }

    /**
     * Retrieves the {@link lonter.jfa.api.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param  id
     *         The id of the searched Role
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.Role Role} for the specified ID
     */
    @Nullable
    default Role getRoleById(long id) {
        return getRoleCache().getElementById(id);
    }

    /**
     * Retrieves all {@link lonter.jfa.api.entities.Role Roles} visible to this JFA instance.
     * <br>This simply filters the Roles returned by {@link #getRoles()} with the provided name, either using
     * {@link String#equals(Object)} or {@link String#equalsIgnoreCase(String)} on {@link lonter.jfa.api.entities.Role#getName()}.
     *
     * @param  name
     *         The name for the Roles
     * @param  ignoreCase
     *         Whether to use {@link String#equalsIgnoreCase(String)}
     *
     * @return Immutable List of all Roles matching the parameters provided.
     */
    @NotNull
    @Unmodifiable
    default List<Role> getRolesByName(@NotNull String name, boolean ignoreCase) {
        return getRoleCache().getElementsByName(name, ignoreCase);
    }

    //    /**
    //     * {@link SnowflakeCacheView} of
    //     * all cached {@link ScheduledEvent ScheduledEvents} visible to this JFA session.
    //     *
    //     * <p>This requires {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
    //     *
    //     * @return {@link SnowflakeCacheView}
    //     */
    //    @NotNull
    //    SnowflakeCacheView<ScheduledEvent> getScheduledEventCache();
    //
    //    /**
    //     * An unmodifiable list of all {@link ScheduledEvent ScheduledEvents} of all connected
    //     * {@link lonter.jfa.api.entities.Guild Guilds}.
    //     *
    //     * <p>This copies the backing store into a list. This means every call
    //     * creates a new list with O(n) complexity. It is recommended to store this into
    //     * a local variable or use {@link #getScheduledEventCache()} and use its more efficient
    //     * versions of handling these values.
    //     *
    //     * <p>This requires {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
    //     *
    //     * @return Possibly-empty immutable list of all known {@link ScheduledEvent ScheduledEvents}.
    //     */
    //    @NotNull
    //    @Unmodifiable
    //    default List<ScheduledEvent> getScheduledEvents() {
    //        return getScheduledEventCache().asList();
    //    }
    //
    //    /**
    //     * This returns the {@link ScheduledEvent} which has the same id as the one provided.
    //     * <br>If there is no known {@link ScheduledEvent} with an id that matches the provided
    //     * one, then this returns {@code null}.
    //     *
    //     * <p>This requires {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
    //     *
    //     * @param  id
    //     *         The id of the {@link ScheduledEvent}.
    //     *
    //     * @throws java.lang.NumberFormatException
    //     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
    //     *
    //     * @return Possibly-null {@link ScheduledEvent} with a matching id.
    //     */
    //    @Nullable
    //    default ScheduledEvent getScheduledEventById(@NotNull String id) {
    //        return getScheduledEventCache().getElementById(id);
    //    }
    //
    //    /**
    //     * This returns the {@link ScheduledEvent} which has the same id as the one provided.
    //     * <br>If there is no known {@link ScheduledEvent} with an id that matches the provided
    //     * one, then this returns {@code null}.
    //     *
    //     * <p>This requires {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
    //     *
    //     * @param  id
    //     *         The id of the {@link ScheduledEvent}.
    //     *
    //     * @return Possibly-null {@link ScheduledEvent} with a matching id.
    //     */
    //    @Nullable
    //    default ScheduledEvent getScheduledEventById(long id) {
    //        return getScheduledEventCache().getElementById(id);
    //    }
    //
    //    /**
    //     * An unmodifiable list of all {@link ScheduledEvent ScheduledEvents} that have the same name as the one
    // provided.
    //     * <br>If there are no {@link ScheduledEvent ScheduledEvents} with the provided name, then this returns an
    // empty list.
    //     *
    //     * <p>This requires {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
    //     *
    //     * @param  name
    //     *         The name of the requested {@link ScheduledEvent}.
    //     * @param  ignoreCase
    //     *         Whether to ignore case or not when comparing the provided name to each {@link
    // ScheduledEvent#getName()}.
    //     *
    //     * @throws IllegalArgumentException
    //     *         If the provided name is null.
    //     *
    //     * @return Possibly-empty immutable list of all the {@link ScheduledEvent ScheduledEvents} that all have the
    //     *         same name as the provided name.
    //     */
    //    @NotNull
    //    @Unmodifiable
    //    default List<ScheduledEvent> getScheduledEventsByName(@NotNull String name, boolean ignoreCase) {
    //        return getScheduledEventCache().getElementsByName(name, ignoreCase);
    //    }

    /**
     * {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link PrivateChannel PrivateChannels} visible to this JFA session.
     *
     * @return {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<PrivateChannel> getPrivateChannelCache();

    /**
     * An unmodifiable list of all known {@link PrivateChannel PrivateChannels}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getPrivateChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Possibly-empty list of all {@link PrivateChannel PrivateChannels}.
     */
    @NotNull
    @Unmodifiable
    default List<PrivateChannel> getPrivateChannels() {
        return getPrivateChannelCache().asList();
    }

    /**
     * This returns the {@link PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link PrivateChannel PrivateChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link PrivateChannel PrivateChannel} with matching id.
     */
    @Nullable
    default PrivateChannel getPrivateChannelById(@NotNull String id) {
        return getPrivateChannelCache().getElementById(id);
    }

    /**
     * This returns the {@link PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link PrivateChannel PrivateChannel}.
     *
     * @return Possibly-null {@link PrivateChannel PrivateChannel} with matching id.
     */
    @Nullable
    default PrivateChannel getPrivateChannelById(long id) {
        return getPrivateChannelCache().getElementById(id);
    }

    /**
     * Opens a {@link PrivateChannel} with the provided user by id.
     * <br>This will fail with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     * if the user does not exist.
     *
     * <p>If the channel is cached, this will directly return the channel in a completed {@link RestAction} without making a request.
     * You can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * <p><b>Example</b><br>
     * {@snippet lang="java":
     * public void sendMessage(JFA jfa, long userId, String content) {
     *     jfa.openPrivateChannelById(userId)
     *        .flatMap(channel -> channel.sendMessage(content))
     *        .queue();
     * }
     * }
     *
     * @param  userId
     *         The id of the target user
     *
     * @throws UnsupportedOperationException
     *         If the target user is the currently logged in account
     *
     * @return {@link CacheRestAction} - Type: {@link PrivateChannel}
     *
     * @see    User#openPrivateChannel()
     */
    @NotNull
    @CheckReturnValue
    CacheRestAction<PrivateChannel> openPrivateChannelById(long userId);

    /**
     * Opens a {@link PrivateChannel} with the provided user by id.
     * <br>This will fail with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     * if the user does not exist.
     *
     * <p>If the channel is cached, this will directly return the channel in a completed {@link RestAction} without making a request.
     * You can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * <p><b>Example</b><br>
     * {@snippet lang="java":
     * public void sendMessage(JFA jfa, String userId, String content) {
     *     jfa.openPrivateChannelById(userId)
     *        .flatMap(channel -> channel.sendMessage(content))
     *        .queue();
     * }
     * }
     *
     * @param  userId
     *         The id of the target user
     *
     * @throws UnsupportedOperationException
     *         If the target user is the currently logged in account
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link PrivateChannel}
     *
     * @see    User#openPrivateChannel()
     */
    @NotNull
    @CheckReturnValue
    default CacheRestAction<PrivateChannel> openPrivateChannelById(@NotNull String userId) {
        return openPrivateChannelById(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Unified {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link RichCustomEmoji Custom Emojis} visible to this JFA session.
     *
     * @return Unified {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     *
     * @see    lonter.jfa.api.utils.cache.CacheView#allSnowflakes(java.util.function.Supplier) CacheView.allSnowflakes(...)
     */
    @NotNull
    SnowflakeCacheView<RichCustomEmoji> getEmojiCache();

    /**
     * A collection of all to us known custom emoji (managed/restricted included).
     * <br>This will be empty if {@link lonter.jfa.api.utils.cache.CacheFlag#EMOJI} is disabled.
     *
     * <p><b>Hint</b>: To check whether you can use an {@link RichCustomEmoji} in a specific
     * context you can use {@link RichCustomEmoji#canInteract(lonter.jfa.api.entities.Member)} or {@link
     * RichCustomEmoji#canInteract(lonter.jfa.api.entities.User, MessageChannel)}
     *
     * <p><b>Unicode emojis are not included as {@link RichCustomEmoji Custom Emoji}!</b>
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getEmojiCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return An immutable list of Custom Emojis (which may or may not be available to usage).
     */
    @NotNull
    @Unmodifiable
    default List<RichCustomEmoji> getEmojis() {
        return getEmojiCache().asList();
    }

    /**
     * Retrieves a custom emoji matching the specified {@code id} if one is available in our cache.
     * <br>This will be null if {@link lonter.jfa.api.utils.cache.CacheFlag#EMOJI} is disabled.
     *
     * <p><b>Unicode emojis are not included as {@link RichCustomEmoji Custom Emoji}!</b>
     *
     * @param  id
     *         The id of the requested {@link RichCustomEmoji}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return A {@link RichCustomEmoji Custom Emoji} represented by this id or null if none is found in
     *         our cache.
     */
    @Nullable
    default RichCustomEmoji getEmojiById(@NotNull String id) {
        return getEmojiCache().getElementById(id);
    }

    /**
     * Retrieves a custom emoji matching the specified {@code id} if one is available in our cache.
     * <br>This will be null if {@link lonter.jfa.api.utils.cache.CacheFlag#EMOJI} is disabled.
     *
     * <p><b>Unicode emojis are not included as {@link RichCustomEmoji Custom Emoji}!</b>
     *
     * @param  id
     *         The id of the requested {@link RichCustomEmoji}.
     *
     * @return A {@link RichCustomEmoji Custom Emoji} represented by this id or null if none is found in
     *         our cache.
     */
    @Nullable
    default RichCustomEmoji getEmojiById(long id) {
        return getEmojiCache().getElementById(id);
    }

    /**
     * An unmodifiable list of all {@link RichCustomEmoji Custom Emojis} that have the same name as the one
     * provided. <br>If there are no {@link RichCustomEmoji Custom Emojis} with the provided name, then
     * this returns an empty list.
     * <br>This will be empty if {@link lonter.jfa.api.utils.cache.CacheFlag#EMOJI} is disabled.
     *
     * <p><b>Unicode emojis are not included as {@link RichCustomEmoji Custom Emoji}!</b>
     *
     * @param  name
     *         The name of the requested {@link RichCustomEmoji Custom Emojis}. Without colons.
     * @param  ignoreCase
     *         Whether to ignore case or not when comparing the provided name to each {@link
     *         RichCustomEmoji#getName()}.
     *
     * @return Possibly-empty list of all the {@link RichCustomEmoji Custom Emojis} that all have the same
     *         name as the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<RichCustomEmoji> getEmojisByName(@NotNull String name, boolean ignoreCase) {
        return getEmojiCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Creates a new {@link ApplicationEmoji} for this bot.
     *
     * <p>Note that the bot is limited to {@value ApplicationEmoji#MAX_APPLICATION_EMOJIS} Application Emojis (normal and animated).
     *
     * @param  name
     *         The name for the new emoji (2-{@value CustomEmoji#EMOJI_NAME_MAX_LENGTH} characters)
     * @param  icon
     *         The {@link Icon} for the new emoji
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is not alphanumeric or not between 2 and {@value CustomEmoji#EMOJI_NAME_MAX_LENGTH} characters long
     *
     * @return {@link RestAction} - Type: {@link ApplicationEmoji}
     */
    @NotNull
    @CheckReturnValue
    RestAction<ApplicationEmoji> createApplicationEmoji(@NotNull String name, @NotNull Icon icon);

    /**
     * Retrieves a list of Application Emojis together with their respective creators.
     *
     * @return {@link RestAction RestAction} - Type: List of {@link ApplicationEmoji}
     */
    @NotNull
    @CheckReturnValue
    RestAction<List<ApplicationEmoji>> retrieveApplicationEmojis();

    /**
     * Retrieves an application emoji together with its respective creator.
     *
     * @param  emojiId
     *         The emoji id
     *
     * @return {@link RestAction RestAction} - Type: {@link ApplicationEmoji}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<ApplicationEmoji> retrieveApplicationEmojiById(long emojiId) {
        return retrieveApplicationEmojiById(Long.toUnsignedString(emojiId));
    }

    /**
     * Retrieves an application emoji together with its respective creator.
     *
     * @param  emojiId
     *         The emoji id
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction RestAction} - Type: {@link ApplicationEmoji}
     */
    @NotNull
    @CheckReturnValue
    RestAction<ApplicationEmoji> retrieveApplicationEmojiById(@NotNull String emojiId);

    /**
     * Attempts to retrieve a {@link Sticker} object based on the provided snowflake reference.
     * <br>This works for both {@link StandardSticker} and {@link GuildSticker}, and you can resolve them using the provided {@link StickerUnion}.
     *
     * <p>If the sticker is not one of the supported {@link Sticker.Type Types}, the request fails with {@link IllegalArgumentException}.
     *
     * <p>The returned {@link lonter.jfa.api.requests.RestAction RestAction} can encounter the following Fluxer errors:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_STICKER UNKNOWN_STICKER}
     *     <br>Occurs when the provided id does not refer to a sticker known by Fluxer.</li>
     * </ul>
     *
     * @param  sticker
     *         The reference of the requested {@link Sticker}.
     *         <br>Can be {@link RichSticker}, {@link StickerItem}, or {@link Sticker#fromId(long)}.
     *
     * @throws IllegalArgumentException
     *         If the provided sticker is null
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link StickerUnion}
     *         <br>On request, gets the sticker with id matching provided id from Fluxer.
     */
    @NotNull
    @CheckReturnValue
    RestAction<StickerUnion> retrieveSticker(@NotNull StickerSnowflake sticker);

    /**
     * Retrieves a list of all the public {@link StickerPack StickerPacks} used for nitro.
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: List of {@link StickerPack}
     */
    @NotNull
    @CheckReturnValue
    RestAction<@Unmodifiable List<StickerPack>> retrieveNitroStickerPacks();

    /**
     * The EventManager used by this JFA instance.
     *
     * @return The {@link lonter.jfa.api.hooks.IEventManager}
     */
    @NotNull
    IEventManager getEventManager();

    /**
     * Returns the currently logged in account represented by {@link lonter.jfa.api.entities.SelfUser SelfUser}.
     * <br>Account settings <b>cannot</b> be modified using this object. If you wish to modify account settings please
     * use the AccountManager which is accessible by {@link lonter.jfa.api.entities.SelfUser#getManager()}.
     *
     * @return The currently logged in account.
     */
    @NotNull
    SelfUser getSelfUser();

    /**
     * The {@link lonter.jfa.api.managers.Presence Presence} controller for the current session.
     * <br>Used to set {@link lonter.jfa.api.entities.Activity} and {@link lonter.jfa.api.OnlineStatus} information.
     *
     * @return The never-null {@link lonter.jfa.api.managers.Presence Presence} for this session.
     */
    @NotNull
    Presence getPresence();

    /**
     * The shard information used when creating this instance of JFA.
     * <br>Represents the information provided to {@link lonter.jfa.api.JFABuilder#useSharding(int, int)}.
     *
     * @return The shard information for this shard
     */
    @NotNull
    ShardInfo getShardInfo();

    /**
     * The login token that is currently being used for Fluxer authentication.
     *
     * @return Never-null, 18 character length string containing the auth token.
     */
    @NotNull
    String getToken();

    /**
     * This value is the total amount of JSON responses that fluxer has sent.
     * <br>This value resets every time the websocket has to perform a full reconnect (not resume).
     *
     * @return Never-negative long containing total response amount.
     */
    long getResponseTotal();

    /**
     * This value is the maximum amount of time, in seconds, that JFA will wait between reconnect attempts.
     * <br>Can be set using {@link lonter.jfa.api.JFABuilder#setMaxReconnectDelay(int) JFABuilder.setMaxReconnectDelay(int)}.
     *
     * @return The maximum amount of time JFA will wait between reconnect attempts in seconds.
     */
    int getMaxReconnectDelay();

    /**
     * Sets whether or not JFA should try to automatically reconnect if a connection-error is encountered.
     * <br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * <p>Default is <b>true</b>.
     *
     * @param  reconnect If true - enables autoReconnect
     */
    void setAutoReconnect(boolean reconnect);

    /**
     * Whether the Requester should retry when
     * a {@link java.net.SocketTimeoutException SocketTimeoutException} occurs.
     *
     * @param  retryOnTimeout
     *         True, if the Request should retry once on a socket timeout
     */
    void setRequestTimeoutRetry(boolean retryOnTimeout);

    /**
     * Used to determine whether or not autoReconnect is enabled for JFA.
     *
     * @return True if JFA will attempt to automatically reconnect when a connection-error is encountered.
     */
    boolean isAutoReconnect();

    /**
     * Used to determine if JFA will process MESSAGE_DELETE_BULK messages received from Fluxer as a single
     * {@link lonter.jfa.api.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent} or split
     * the deleted messages up and fire multiple {@link lonter.jfa.api.events.message.MessageDeleteEvent MessageDeleteEvents},
     * one for each deleted message.
     *
     * <p>By default, JFA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link lonter.jfa.api.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}
     *
     * @return Whether or not JFA currently handles the BULK_MESSAGE_DELETE event by splitting it into individual MessageDeleteEvents or not.
     */
    boolean isBulkDeleteSplittingEnabled();

    /**
     * Shuts down this JFA instance, closing all its connections.
     * After this command is issued the JFA Instance can not be used anymore.
     * Already enqueued {@link lonter.jfa.api.requests.RestAction RestActions} are still going to be executed.
     *
     * <p>If you want this instance to shutdown without executing, use {@link #shutdownNow() shutdownNow()}
     *
     * <p>This will interrupt the default JFA event thread, due to the gateway connection being interrupted.
     *
     * @see #shutdownNow()
     */
    void shutdown();

    /**
     * Shuts down this JFA instance instantly, closing all its connections.
     * After this command is issued the JFA Instance can not be used anymore.
     * This will also cancel all queued {@link lonter.jfa.api.requests.RestAction RestActions}.
     *
     * <p>If you want this instance to shutdown without cancelling enqueued RestActions use {@link #shutdown() shutdown()}
     *
     * <p>This will interrupt the default JFA event thread, due to the gateway connection being interrupted.
     *
     * @see #shutdown()
     */
    void shutdownNow();

    /**
     * Retrieves the {@link ApplicationInfo ApplicationInfo} for
     * the application that owns the logged in Bot-Account.
     * <br>This contains information about the owner of the currently logged in bot account!
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link ApplicationInfo ApplicationInfo}
     *         <br>The {@link ApplicationInfo ApplicationInfo} of the bot's application.
     */
    @NotNull
    @CheckReturnValue
    RestAction<ApplicationInfo> retrieveApplicationInfo();

    /**
     * Retrieves all {@link SKU SKUs} for
     * the application that owns the logged in Bot-Account.
     *
     * <br>Because of how SKUs and subscription systems work, you will see two SKUs for a subscription offering.
     * For integration and testing entitlements for Subscriptions, you should use the SKU with type: {@link SKUType#SUBSCRIPTION}.
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link List} of {@link SKU SKU}
     *         <br>The {@link SKU SKUs} of the bot's application.
     */
    @NotNull
    @CheckReturnValue
    RestAction<List<SKU>> retrieveSKUList();

    /**
     * A {@link lonter.jfa.api.requests.restaction.pagination.PaginationAction PaginationAction} implementation
     * which allows you to {@link Iterable iterate} over {@link Entitlement}s that are applicable to the logged in application.
     *
     * @return {@link EntitlementPaginationAction EntitlementPaginationAction}
     */
    @NotNull
    @CheckReturnValue
    EntitlementPaginationAction retrieveEntitlements();

    /**
     * Retrieves an {@link Entitlement} by its id.
     *
     * @param  entitlementId
     *         The id of the entitlement to retrieve
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link Entitlement}
     *         <br>The entitlement with the provided id
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Entitlement> retrieveEntitlementById(@NotNull String entitlementId) {
        return retrieveEntitlementById(MiscUtil.parseSnowflake(entitlementId));
    }

    /**
     * Retrieves an {@link Entitlement} by its id.
     *
     * @param  entitlementId
     *         The id of the entitlement to retrieve
     *
     * @return {@link RestAction} - Type: {@link Entitlement}
     *         <br>The entitlement with the provided id
     */
    @NotNull
    @CheckReturnValue
    RestAction<Entitlement> retrieveEntitlementById(long entitlementId);

    /**
     * Constructs a new {@link Entitlement Entitlement} with the skuId and the type.
     * <br>Use the returned {@link TestEntitlementCreateAction TestEntitlementCreateAction} to provide more details.
     *
     * @param  skuId
     *         The id of the SKU the entitlement is for
     * @param ownerId
     *        The id of the owner of the entitlement
     * @param ownerType
     *        The type of the owner of the entitlement
     *
     * @throws IllegalArgumentException
     *         If the provided skuId or ownerId is not a valid snowflake
     *
     * @return {@link TestEntitlementCreateAction TestEntitlementCreateAction}
     *         <br>Allows for setting various details for the resulting Entitlement
     */
    @NotNull
    @CheckReturnValue
    default TestEntitlementCreateAction createTestEntitlement(
            @NotNull String skuId, @NotNull String ownerId, @NotNull TestEntitlementCreateAction.OwnerType ownerType) {
        return createTestEntitlement(MiscUtil.parseSnowflake(skuId), MiscUtil.parseSnowflake(ownerId), ownerType);
    }

    /**
     * Constructs a new {@link Entitlement Entitlement} with the skuId and the type.
     * <br>Use the returned {@link TestEntitlementCreateAction TestEntitlementCreateAction} to provide more details.
     *
     * @param  skuId
     *         The id of the SKU the entitlement is for
     * @param ownerId
     *        The id of the owner of the entitlement
     * @param ownerType
     *        The type of the owner of the entitlement
     *
     * @throws IllegalArgumentException
     *         If the provided ownerType is null
     *
     * @return {@link TestEntitlementCreateAction TestEntitlementCreateAction}
     *         <br>Allows for setting various details for the resulting Entitlement
     */
    @NotNull
    @CheckReturnValue
    TestEntitlementCreateAction createTestEntitlement(
            long skuId, long ownerId, @NotNull TestEntitlementCreateAction.OwnerType ownerType);

    /**
     * Deletes a test entitlement by its id.
     *
     * @param  entitlementId
     *         The id of the entitlement to delete
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: Void
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> deleteTestEntitlement(@NotNull String entitlementId) {
        return deleteTestEntitlement(MiscUtil.parseSnowflake(entitlementId));
    }

    /**
     * Deletes a test entitlement by its id.
     *
     * @param  entitlementId
     *         The id of the entitlement to delete
     *
     * @return {@link RestAction} - Type: Void
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> deleteTestEntitlement(long entitlementId);

    /**
     * Configures the required scopes applied to the {@link #getInviteUrl(Permission...)} and similar methods.
     * <br>To use slash commands you must add {@code "applications.commands"} to these scopes. The scope {@code "bot"} is always applied.
     *
     * @param  scopes
     *         The scopes to use with {@link #getInviteUrl(Permission...)} and the likes
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current JFA instance
     */
    @NotNull
    default JFA setRequiredScopes(@NotNull String... scopes) {
        Checks.noneNull(scopes, "Scopes");
        return setRequiredScopes(Arrays.asList(scopes));
    }

    /**
     * Configures the required scopes applied to the {@link #getInviteUrl(Permission...)} and similar methods.
     * <br>To use slash commands you must add {@code "applications.commands"} to these scopes. The scope {@code "bot"} is always applied.
     *
     * @param  scopes
     *         The scopes to use with {@link #getInviteUrl(Permission...)} and the likes
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current JFA instance
     */
    @NotNull
    JFA setRequiredScopes(@NotNull Collection<String> scopes);

    /**
     * Creates an authorization invite url for the currently logged in Bot-Account.
     * <br>Example Format:
     * {@code https://fluxer.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8}
     *
     * <p><b>Hint:</b> To enable a pre-selected Guild of choice append the parameter {@code &guild_id=YOUR_GUILD_ID}
     *
     * @param  permissions
     *         The permissions to use in your invite, these can be changed by the link user.
     *         <br>If no permissions are provided the {@code permissions} parameter is omitted
     *
     * @return A valid OAuth2 invite url for the currently logged in Bot-Account
     */
    @NotNull
    String getInviteUrl(@Nullable Permission... permissions);

    /**
     * Creates an authorization invite url for the currently logged in Bot-Account.
     * <br>Example Format:
     * {@code https://fluxer.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8}
     *
     * <p><b>Hint:</b> To enable a pre-selected Guild of choice append the parameter {@code &guild_id=YOUR_GUILD_ID}
     *
     * @param  permissions
     *         The permissions to use in your invite, these can be changed by the link user.
     *         <br>If no permissions are provided the {@code permissions} parameter is omitted
     *
     * @return A valid OAuth2 invite url for the currently logged in Bot-Account
     */
    @NotNull
    String getInviteUrl(@Nullable Collection<Permission> permissions);

    /**
     * Returns the {@link lonter.jfa.api.sharding.ShardManager ShardManager} that manages this JFA instances or null if this instance is not managed
     * by any {@link lonter.jfa.api.sharding.ShardManager ShardManager}.
     *
     * @return The corresponding ShardManager or {@code null} if there is no such manager
     */
    @Nullable
    ShardManager getShardManager();

    /**
     * Retrieves a {@link lonter.jfa.api.entities.Webhook Webhook} by its id.
     * <br>If the webhook does not belong to any known guild of this JFA session, it will be {@link Webhook#isPartial() partial}.
     *
     * <p>Possible {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link lonter.jfa.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>We do not have the required permissions</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>A webhook with this id does not exist</li>
     * </ul>
     *
     * @param  webhookId
     *         The webhook id
     *
     * @throws IllegalArgumentException
     *         If the {@code webhookId} is null or empty
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link lonter.jfa.api.entities.Webhook Webhook}
     *          <br>The webhook object.
     *
     * @see    Guild#retrieveWebhooks()
     * @see    TextChannel#retrieveWebhooks()
     */
    @NotNull
    @CheckReturnValue
    RestAction<Webhook> retrieveWebhookById(@NotNull String webhookId);

    /**
     * Retrieves a {@link lonter.jfa.api.entities.Webhook Webhook} by its id.
     * <br>If the webhook does not belong to any known guild of this JFA session, it will be {@link Webhook#isPartial() partial}.
     *
     * <p>Possible {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link lonter.jfa.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>We do not have the required permissions</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>A webhook with this id does not exist</li>
     * </ul>
     *
     * @param  webhookId
     *         The webhook id
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link lonter.jfa.api.entities.Webhook Webhook}
     *          <br>The webhook object.
     *
     * @see    Guild#retrieveWebhooks()
     * @see    TextChannel#retrieveWebhooks()
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Webhook> retrieveWebhookById(long webhookId) {
        return retrieveWebhookById(Long.toUnsignedString(webhookId));
    }

    /**
     * Installs an auxiliary port for audio transfer.
     *
     * @throws IllegalStateException
     *         If this is a headless environment or no port is available
     *
     * @return {@link AuditableRestAction} - Type: int
     *         Provides the resulting used port
     */
    @NotNull
    @CheckReturnValue
    default AuditableRestAction<Integer> installAuxiliaryPort() {
        int port = ThreadLocalRandom.current().nextInt();
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
            } catch (IOException | URISyntaxException e) {
                throw new IllegalStateException("No port available");
            }
        } else {
            throw new IllegalStateException("No port available");
        }
        return new CompletedRestAction<>(this, port);
    }

    /**
     * Returns the {@link ApplicationManager} that manages the application associated with the bot.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link lonter.jfa.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @return The corresponding ApplicationManager
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager getApplicationManager();
}
