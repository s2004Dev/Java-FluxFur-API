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

package lonter.jfa.internal;

import com.neovisionaries.ws.client.WebSocketFactory;
import gnu.trove.set.TLongSet;
import lonter.jfa.api.GatewayEncoding;
import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.audio.AudioModuleConfig;
import lonter.jfa.api.audio.factory.IAudioSendFactory;
import lonter.jfa.api.audio.hooks.ConnectionStatus;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.*;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.emoji.ApplicationEmoji;
import lonter.jfa.api.entities.emoji.CustomEmoji;
import lonter.jfa.api.entities.emoji.RichCustomEmoji;
import lonter.jfa.api.entities.sticker.StickerPack;
import lonter.jfa.api.entities.sticker.StickerSnowflake;
import lonter.jfa.api.entities.sticker.StickerUnion;
import lonter.jfa.api.events.GatewayPingEvent;
import lonter.jfa.api.events.GenericEvent;
import lonter.jfa.api.events.StatusChangeEvent;
import lonter.jfa.api.events.session.ShutdownEvent;
import lonter.jfa.api.exceptions.InvalidTokenException;
import lonter.jfa.api.exceptions.ParsingException;
import lonter.jfa.api.exceptions.RateLimitedException;
import lonter.jfa.api.hooks.IEventManager;
import lonter.jfa.api.hooks.InterfacedEventManager;
import lonter.jfa.api.hooks.VoiceDispatchInterceptor;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.build.CommandData;
import lonter.jfa.api.managers.ApplicationManager;
import lonter.jfa.api.managers.AudioManager;
import lonter.jfa.api.managers.Presence;
import lonter.jfa.api.requests.*;
import lonter.jfa.api.requests.restaction.*;
import lonter.jfa.api.requests.restaction.pagination.EntitlementPaginationAction;
import lonter.jfa.api.sharding.ShardManager;
import lonter.jfa.api.utils.*;
import lonter.jfa.api.utils.cache.CacheFlag;
import lonter.jfa.api.utils.cache.CacheView;
import lonter.jfa.api.utils.cache.ChannelCacheView;
import lonter.jfa.api.utils.cache.SnowflakeCacheView;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.UserImpl;
import lonter.jfa.internal.handle.EventCache;
import lonter.jfa.internal.handle.GuildSetupController;
import lonter.jfa.internal.hooks.EventManagerProxy;
import lonter.jfa.internal.interactions.CommandDataImpl;
import lonter.jfa.internal.interactions.command.CommandImpl;
import lonter.jfa.internal.managers.ApplicationManagerImpl;
import lonter.jfa.internal.managers.AudioManagerImpl;
import lonter.jfa.internal.managers.DirectAudioControllerImpl;
import lonter.jfa.internal.managers.PresenceImpl;
import lonter.jfa.internal.requests.*;
import lonter.jfa.internal.requests.restaction.CommandCreateActionImpl;
import lonter.jfa.internal.requests.restaction.CommandEditActionImpl;
import lonter.jfa.internal.requests.restaction.CommandListUpdateActionImpl;
import lonter.jfa.internal.requests.restaction.TestEntitlementCreateActionImpl;
import lonter.jfa.internal.requests.restaction.pagination.EntitlementPaginationActionImpl;
import lonter.jfa.internal.utils.*;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.cache.AbstractCacheView;
import lonter.jfa.internal.utils.cache.ChannelCacheViewImpl;
import lonter.jfa.internal.utils.cache.SnowflakeCacheViewImpl;
import lonter.jfa.internal.utils.config.AuthorizationConfig;
import lonter.jfa.internal.utils.config.MetaConfig;
import lonter.jfa.internal.utils.config.SessionConfig;
import lonter.jfa.internal.utils.config.ThreadingConfig;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class JFAImpl implements JFA {
    public static final Logger LOG = JFALogger.getLog(JFA.class);

    protected final SnowflakeCacheViewImpl<User> userCache = new SnowflakeCacheViewImpl<>(User.class, User::getName);
    protected final SnowflakeCacheViewImpl<Guild> guildCache =
            new SnowflakeCacheViewImpl<>(Guild.class, Guild::getName);
    protected final ChannelCacheViewImpl<Channel> channelCache = new ChannelCacheViewImpl<>(Channel.class);
    protected final ArrayDeque<Long> privateChannelLRU = new ArrayDeque<>();

    protected final AbstractCacheView<AudioManager> audioManagers = new CacheView.SimpleCacheView<>(
            AudioManager.class, m -> m.getGuild().getName());

    protected final PresenceImpl presence;
    protected final Thread shutdownHook;
    protected final EntityBuilder entityBuilder = new EntityBuilder(this);
    protected final EventCache eventCache;
    protected final EventManagerProxy eventManager;

    protected final GuildSetupController guildSetupController;
    protected final DirectAudioControllerImpl audioController;

    protected final AuthorizationConfig authConfig;
    protected final ThreadingConfig threadConfig;
    protected final SessionConfig sessionConfig;
    protected final MetaConfig metaConfig;
    protected final RestConfig restConfig;
    protected final AudioModuleConfig audioModuleConfig;

    public ShutdownReason shutdownReason =
            ShutdownReason.USER_SHUTDOWN; // indicates why shutdown happened in awaitStatus / awaitReady
    protected WebSocketClient client;
    protected Requester requester;
    protected SelfUser selfUser;
    protected ShardInfo shardInfo;
    protected long responseTotal;
    protected long gatewayPing = -1;
    protected String gatewayUrl;
    protected ChunkingFilter chunkingFilter;

    protected String clientId = null, requiredScopes = "bot";
    protected ShardManager shardManager = null;
    protected MemberCachePolicy memberCachePolicy = MemberCachePolicy.ALL;

    protected final AtomicReference<Status> status = new AtomicReference<>(Status.INITIALIZING);
    protected final ReentrantLock statusLock = new ReentrantLock();
    protected final Condition statusCondition = statusLock.newCondition();
    protected final AtomicBoolean requesterShutdown = new AtomicBoolean(false);
    protected final AtomicReference<ShutdownEvent> shutdownEvent = new AtomicReference<>(null);

    public JFAImpl(AuthorizationConfig authConfig) {
        this(authConfig, null, null, null, null, null);
    }

    public JFAImpl(
            AuthorizationConfig authConfig,
            SessionConfig sessionConfig,
            ThreadingConfig threadConfig,
            MetaConfig metaConfig,
            RestConfig restConfig,
            AudioModuleConfig audioModuleConfig) {
        this.authConfig = authConfig;
        this.threadConfig = threadConfig == null ? ThreadingConfig.getDefault() : threadConfig;
        this.sessionConfig = sessionConfig == null ? SessionConfig.getDefault() : sessionConfig;
        this.metaConfig = metaConfig == null ? MetaConfig.getDefault() : metaConfig;
        this.restConfig = restConfig == null ? new RestConfig() : restConfig;
        this.audioModuleConfig = audioModuleConfig == null ? new AudioModuleConfig() : audioModuleConfig;
        this.shutdownHook =
                this.metaConfig.isUseShutdownHook() ? new Thread(this::shutdownNow, "JFA Shutdown Hook") : null;
        this.presence = new PresenceImpl(this);
        this.guildSetupController = new GuildSetupController(this);
        this.audioController = new DirectAudioControllerImpl(this);
        this.eventCache = new EventCache();
        this.eventManager = new EventManagerProxy(new InterfacedEventManager(), this.threadConfig.getEventPool());
    }

    public void handleEvent(@NotNull GenericEvent event) {
        eventManager.handle(event);
    }

    public boolean isRawEvents() {
        return sessionConfig.isRawEvents();
    }

    public boolean isEventPassthrough() {
        return sessionConfig.isEventPassthrough();
    }

    public boolean isCacheFlagSet(CacheFlag flag) {
        return metaConfig.getCacheFlags().contains(flag);
    }

    public boolean isIntent(GatewayIntent intent) {
        int raw = intent.getRawValue();
        return (client.getGatewayIntents() & raw) == raw;
    }

    public int getLargeThreshold() {
        return sessionConfig.getLargeThreshold();
    }

    public int getMaxBufferSize() {
        return metaConfig.getMaxBufferSize();
    }

    public boolean chunkGuild(long id) {
        try {
            return isIntent(GatewayIntent.GUILD_MEMBERS) && chunkingFilter.filter(id);
        } catch (Exception e) {
            LOG.error("Uncaught exception from chunking filter", e);
            return true;
        }
    }

    public void setChunkingFilter(ChunkingFilter filter) {
        this.chunkingFilter = filter;
    }

    public boolean cacheMember(Member member) {
        try {
            return member.getUser().equals(getSelfUser()) // always cache self
                    || memberCachePolicy.cacheMember(member); // ask policy, should we cache?
        } catch (Exception e) {
            LOG.error("Uncaught exception from member cache policy", e);
            return true;
        }
    }

    public void setMemberCachePolicy(MemberCachePolicy policy) {
        this.memberCachePolicy = policy;
    }

    public SessionController getSessionController() {
        return sessionConfig.getSessionController();
    }

    public GuildSetupController getGuildSetupController() {
        return guildSetupController;
    }

    public VoiceDispatchInterceptor getVoiceInterceptor() {
        return sessionConfig.getVoiceDispatchInterceptor();
    }

    public void usedPrivateChannel(long id) {
        synchronized (privateChannelLRU) {
            // We could probably make a special LRU cache view too,
            // might not be worth it though
            privateChannelLRU.remove(id);
            privateChannelLRU.addFirst(id);
            // This could probably be a config option
            if (privateChannelLRU.size() > 10) {
                long removed = privateChannelLRU.removeLast();
                channelCache.remove(ChannelType.PRIVATE, removed);
            }
        }
    }

    public void initRequester() {
        if (this.requester != null) {
            return;
        }
        RestRateLimiter rateLimiter = this.restConfig
                .getRateLimiterFactory()
                .apply(new RestRateLimiter.RateLimitConfig(
                        this.threadConfig.getRateLimitScheduler(),
                        this.threadConfig.getRateLimitElastic(),
                        getSessionController().getRateLimitHandle(),
                        this.sessionConfig.isRelativeRateLimit() && this.restConfig.isRelativeRateLimit()));
        this.requester = new Requester(this, this.authConfig, this.restConfig, rateLimiter);
        this.requester.setRetryOnTimeout(this.sessionConfig.isRetryOnTimeout());
    }

    public int login() {
        return login(null, null, Compression.ZLIB, true, GatewayIntent.ALL_INTENTS, GatewayEncoding.JSON);
    }

    public int login(
            ShardInfo shardInfo,
            Compression compression,
            boolean validateToken,
            int intents,
            GatewayEncoding encoding) {
        return login(null, shardInfo, compression, validateToken, intents, encoding);
    }

    public int login(
            String gatewayUrl,
            ShardInfo shardInfo,
            Compression compression,
            boolean validateToken,
            int intents,
            GatewayEncoding encoding) {
        this.shardInfo = shardInfo;

        // Delayed init for thread-pools so they can set the shard info as their name
        this.threadConfig.init(this::getIdentifierString);
        // Setup rest-module and rate-limiter subsystem
        initRequester();

        this.gatewayUrl = gatewayUrl == null ? getGateway() : gatewayUrl;
        Checks.notNull(this.gatewayUrl, "Gateway URL");

        setStatus(Status.LOGGING_IN);

        Map<String, String> previousContext = null;
        ConcurrentMap<String, String> contextMap = metaConfig.getMdcContextMap();
        if (contextMap != null) {
            if (shardInfo != null) {
                contextMap.put("jfa.shard", shardInfo.getShardString());
                contextMap.put("jfa.shard.id", String.valueOf(shardInfo.getShardId()));
                contextMap.put("jfa.shard.total", String.valueOf(shardInfo.getShardTotal()));
            }
            // set MDC metadata for build thread
            previousContext = MDC.getCopyOfContextMap();
            contextMap.forEach(MDC::put);
            requester.setContextReady(true);
        }
        if (validateToken) {
            verifyToken();
            LOG.info("Login Successful!");
        }

        client = new WebSocketClient(this, compression, intents, encoding);
        // remove our MDC metadata when we exit our code
        if (previousContext != null) {
            previousContext.forEach(MDC::put);
        }

        if (shutdownHook != null) {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }

        return shardInfo == null ? -1 : shardInfo.getShardTotal();
    }

    public String getGateway() {
        return getSessionController().getGateway(this);
    }

    // This method also checks for a valid bot token
    // as it is required to get the recommended shard count.
    public SessionController.ShardedGateway getShardedGateway() {
        return getSessionController().getShardedGateway(this);
    }

    public ConcurrentMap<String, String> getContextMap() {
        return metaConfig.getMdcContextMap() == null ? null : new ConcurrentHashMap<>(metaConfig.getMdcContextMap());
    }

    public void setContext() {
        if (metaConfig.getMdcContextMap() != null) {
            metaConfig.getMdcContextMap().forEach(MDC::put);
        }
    }

    public void setToken(String token) {
        this.authConfig.setToken(token);
    }

    public void setStatus(Status status) {
        StatusChangeEvent event = MiscUtil.locked(statusLock, () -> {
            Status oldStatus = this.status.getAndSet(status);
            this.statusCondition.signalAll();

            return new StatusChangeEvent(this, status, oldStatus);
        });

        if (event.getOldStatus() != event.getNewStatus()) {
            handleEvent(event);
        }
    }

    public void verifyToken() {
        RestActionImpl<DataObject> login = new RestActionImpl<DataObject>(this, Route.Self.GET_SELF.compile()) {
            @Override
            public void handleResponse(Response response, Request<DataObject> request) {
                if (response.isOk()) {
                    request.onSuccess(response.getObject());
                } else if (response.isRateLimit()) {
                    request.onFailure(new RateLimitedException(request.getRoute(), response.retryAfter));
                } else if (response.code == 401) {
                    request.onSuccess(null);
                } else {
                    request.onFailure(response);
                }
            }
        }.priority();

        try {
            DataObject userResponse = login.complete();
            if (userResponse != null) {
                getEntityBuilder().createSelfUser(userResponse);
                return;
            }

            throw new InvalidTokenException();
        } catch (Throwable error) {
            shutdownNow();
            throw error;
        }
    }

    @NotNull
    public AuthorizationConfig getAuthorizationConfig() {
        return authConfig;
    }

    @NotNull
    public AudioModuleConfig getAudioModuleConfig() {
        return audioModuleConfig;
    }

    @NotNull
    @Override
    public String getToken() {
        return authConfig.getToken();
    }

    @Override
    public boolean isBulkDeleteSplittingEnabled() {
        return sessionConfig.isBulkDeleteSplittingEnabled();
    }

    @Override
    public void setAutoReconnect(boolean autoReconnect) {
        sessionConfig.setAutoReconnect(autoReconnect);
        WebSocketClient client = getClient();
        if (client != null) {
            client.setAutoReconnect(autoReconnect);
        }
    }

    @Override
    public void setRequestTimeoutRetry(boolean retryOnTimeout) {
        requester.setRetryOnTimeout(retryOnTimeout);
    }

    @Override
    public boolean isAutoReconnect() {
        return sessionConfig.isAutoReconnect();
    }

    @NotNull
    @Override
    public Status getStatus() {
        return status.get();
    }

    @NotNull
    @Override
    public EnumSet<GatewayIntent> getGatewayIntents() {
        return GatewayIntent.getIntents(client.getGatewayIntents());
    }

    @NotNull
    @Override
    public EnumSet<CacheFlag> getCacheFlags() {
        return Helpers.copyEnumSet(CacheFlag.class, metaConfig.getCacheFlags());
    }

    @Override
    public boolean unloadUser(long userId) {
        if (userId == selfUser.getIdLong()) {
            return false;
        }
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        // We avoid to lock both the guild cache and member cache to make a deadlock impossible
        return getGuildCache().stream()
                        .filter(guild -> guild.unloadMember(userId)) // this also removes it from user cache
                        .count()
                > 0L; // we use count to make sure it iterates all guilds not just one
    }

    @Override
    public long getGatewayPing() {
        return gatewayPing;
    }

    @NotNull
    @Override
    public JFA awaitStatus(@NotNull Status status, @NotNull Status... failOn) throws InterruptedException {
        Checks.notNull(status, "Status");
        if (getStatus() == Status.CONNECTED) {
            return this;
        }

        MiscUtil.tryLock(statusLock);
        try {
            EnumSet<Status> endCondition = EnumSet.of(status, failOn);
            Status current = getStatus();
            while (!current.isInit() // In case of disconnects during startup
                    || current.ordinal() < status.ordinal()) // If we missed the status (e.g. LOGGING_IN ->
            // CONNECTED happened while waiting for lock)
            {
                if (current == Status.SHUTDOWN) {
                    throw new IllegalStateException("Was shutdown trying to await status.\nReason: " + shutdownReason);
                }
                if (endCondition.contains(current)) {
                    return this;
                }

                statusCondition.await();
                current = getStatus();
            }
        } finally {
            statusLock.unlock();
        }

        return this;
    }

    @Override
    public boolean awaitShutdown(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        timeout = unit.toMillis(timeout);
        long deadline = timeout == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
        MiscUtil.tryLock(statusLock);
        try {
            Status current = getStatus();
            while (current != Status.SHUTDOWN) {
                if (!statusCondition.await(deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS)) {
                    return false;
                }
                current = getStatus();
            }
            return true;
        } finally {
            statusLock.unlock();
        }
    }

    @Override
    public int cancelRequests() {
        return requester.getRateLimiter().cancelRequests();
    }

    @NotNull
    @Override
    public ScheduledExecutorService getRateLimitPool() {
        return threadConfig.getRateLimitScheduler();
    }

    @NotNull
    @Override
    public ScheduledExecutorService getGatewayPool() {
        return threadConfig.getGatewayPool();
    }

    @NotNull
    @Override
    public ExecutorService getCallbackPool() {
        return threadConfig.getCallbackPool();
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions") // this can't really happen unless you pass bad configs
    public OkHttpClient getHttpClient() {
        return sessionConfig.getHttpClient();
    }

    @NotNull
    @Override
    public DirectAudioControllerImpl getDirectAudioController() {
        if (!isIntent(GatewayIntent.GUILD_VOICE_STATES)) {
            throw new IllegalStateException("Cannot use audio features with disabled GUILD_VOICE_STATES intent!");
        }
        return this.audioController;
    }

    @NotNull
    @Override
    public List<Guild> getMutualGuilds(@NotNull UserSnowflake... users) {
        Checks.notNull(users, "users");
        return getMutualGuilds(Arrays.asList(users));
    }

    @NotNull
    @Override
    public List<Guild> getMutualGuilds(@NotNull Collection<? extends UserSnowflake> users) {
        Checks.notNull(users, "users");
        for (UserSnowflake u : users) {
            Checks.notNull(u, "All users");
        }
        return getGuilds().stream()
                .filter(guild -> users.stream().allMatch(guild::isMember))
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public CacheRestAction<User> retrieveUserById(long id) {
        return new DeferredRestAction<>(
                this,
                User.class,
                () -> isIntent(GatewayIntent.GUILD_MEMBERS) || isIntent(GatewayIntent.GUILD_PRESENCES)
                        ? getUserById(id)
                        : null,
                () -> {
                    if (id == getSelfUser().getIdLong()) {
                        return new CompletedRestAction<>(this, getSelfUser());
                    }
                    Route.CompiledRoute route = Route.Users.GET_USER.compile(Long.toUnsignedString(id));
                    return new RestActionImpl<>(this, route, (response, request) -> getEntityBuilder()
                            .createUser(response.getObject()));
                });
    }

    @NotNull
    @Override
    public CacheView<AudioManager> getAudioManagerCache() {
        return audioManagers;
    }

    @NotNull
    @Override
    public SnowflakeCacheView<Guild> getGuildCache() {
        return guildCache;
    }

    @NotNull
    @Override
    public Set<String> getUnavailableGuilds() {
        TLongSet unavailableGuilds = guildSetupController.getUnavailableGuilds();
        Set<String> copy = new HashSet<>();
        unavailableGuilds.forEach(id -> copy.add(Long.toUnsignedString(id)));
        return copy;
    }

    @Override
    public boolean isUnavailable(long guildId) {
        return guildSetupController.isUnavailable(guildId);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<Role> getRoleCache() {
        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getRoleCache));
    }

    @NotNull
    @Override
    public SnowflakeCacheView<RichCustomEmoji> getEmojiCache() {
        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getEmojiCache));
    }

    @NotNull
    @Override
    public RestAction<ApplicationEmoji> createApplicationEmoji(@NotNull String name, @NotNull Icon icon) {
        Checks.inRange(name, 2, CustomEmoji.EMOJI_NAME_MAX_LENGTH, "Emoji name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Emoji name");
        Checks.notNull(icon, "Emoji icon");

        DataObject body = DataObject.empty();
        body.put("name", name);
        body.put("image", icon.getEncoding());

        Route.CompiledRoute route = Route.Applications.CREATE_APPLICATION_EMOJI.compile(
                getSelfUser().getApplicationId());
        return new RestActionImpl<>(this, route, body, (response, request) -> {
            DataObject obj = response.getObject();
            User selfUser = getSelfUser();
            return entityBuilder.createApplicationEmoji(this, obj, selfUser);
        });
    }

    @NotNull
    @Override
    public RestAction<List<ApplicationEmoji>> retrieveApplicationEmojis() {
        Route.CompiledRoute route =
                Route.Applications.GET_APPLICATION_EMOJIS.compile(getSelfUser().getApplicationId());
        return new RestActionImpl<>(this, route, (response, request) -> {
            DataArray emojis = response.getObject().getArray("items");
            List<ApplicationEmoji> list = new ArrayList<>(emojis.length());
            for (int i = 0; i < emojis.length(); i++) {
                try {
                    DataObject emoji = emojis.getObject(i);
                    User owner = emoji.optObject("user")
                            .map(entityBuilder::createUser)
                            .orElse(null);

                    list.add(entityBuilder.createApplicationEmoji(this, emoji, owner));
                } catch (ParsingException e) {
                    LOG.error("Failed to parse application emoji with JSON: {}", emojis.getObject(i), e);
                }
            }

            return Collections.unmodifiableList(list);
        });
    }

    @NotNull
    @Override
    public RestAction<ApplicationEmoji> retrieveApplicationEmojiById(@NotNull String emojiId) {
        Checks.isSnowflake(emojiId);
        Route.CompiledRoute route =
                Route.Applications.GET_APPLICATION_EMOJI.compile(getSelfUser().getApplicationId(), emojiId);
        return new RestActionImpl<>(this, route, (response, request) -> {
            DataObject emoji = response.getObject();
            User owner = emoji.optObject("user").map(entityBuilder::createUser).orElse(null);

            return entityBuilder.createApplicationEmoji(this, emoji, owner);
        });
    }

    @NotNull
    @Override
    public RestAction<StickerUnion> retrieveSticker(@NotNull StickerSnowflake sticker) {
        Checks.notNull(sticker, "Sticker");
        Route.CompiledRoute route = Route.Stickers.GET_STICKER.compile(sticker.getId());
        return new RestActionImpl<>(
                this, route, (response, request) -> entityBuilder.createRichSticker(response.getObject()));
    }

    @NotNull
    @Override
    public RestAction<List<StickerPack>> retrieveNitroStickerPacks() {
        Route.CompiledRoute route = Route.Stickers.LIST_PACKS.compile();
        return new RestActionImpl<>(this, route, (response, request) -> {
            DataArray array = response.getObject().getArray("sticker_packs");
            List<StickerPack> packs = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                DataObject object = null;
                try {
                    object = array.getObject(i);
                    StickerPack pack = entityBuilder.createStickerPack(object);
                    packs.add(pack);
                } catch (ParsingException ex) {
                    EntityBuilder.LOG.error("Failed to parse sticker pack. JSON: {}", object);
                }
            }
            return Collections.unmodifiableList(packs);
        });
    }

    //    @NotNull
    //    @Override
    //    public SnowflakeCacheView<ScheduledEvent> getScheduledEventCache() {
    //        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getScheduledEventCache));
    //    }

    @NotNull
    @Override
    public ChannelCacheView<Channel> getChannelCache() {
        return channelCache;
    }

    @NotNull
    @Override
    public SnowflakeCacheView<Category> getCategoryCache() {
        return channelCache.ofType(Category.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache() {
        return channelCache.ofType(TextChannel.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<NewsChannel> getNewsChannelCache() {
        return channelCache.ofType(NewsChannel.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        return channelCache.ofType(VoiceChannel.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<StageChannel> getStageChannelCache() {
        return channelCache.ofType(StageChannel.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<ThreadChannel> getThreadChannelCache() {
        return channelCache.ofType(ThreadChannel.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<ForumChannel> getForumChannelCache() {
        return channelCache.ofType(ForumChannel.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<MediaChannel> getMediaChannelCache() {
        return channelCache.ofType(MediaChannel.class);
    }

    @NotNull
    @Override
    public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
        return channelCache.ofType(PrivateChannel.class);
    }

    @Override
    public PrivateChannel getPrivateChannelById(@NotNull String id) {
        return getPrivateChannelById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public PrivateChannel getPrivateChannelById(long id) {
        PrivateChannel channel = JFA.super.getPrivateChannelById(id);
        if (channel != null) {
            usedPrivateChannel(id);
        }
        return channel;
    }

    @Override
    public <T extends Channel> T getChannelById(@NotNull Class<T> type, long id) {
        return channelCache.ofType(type).getElementById(id);
    }

    @Override
    public GuildChannel getGuildChannelById(long id) {
        return channelCache.ofType(GuildChannel.class).getElementById(id);
    }

    @Override
    public GuildChannel getGuildChannelById(@NotNull ChannelType type, long id) {
        Channel channel = channelCache.getElementById(type, id);
        return channel instanceof GuildChannel ? (GuildChannel) channel : null;
    }

    @NotNull
    @Override
    public CacheRestAction<PrivateChannel> openPrivateChannelById(long userId) {
        if (selfUser != null && userId == selfUser.getIdLong()) {
            throw new UnsupportedOperationException("Cannot open private channel with yourself!");
        }
        return new DeferredRestAction<>(
                this,
                PrivateChannel.class,
                () -> {
                    User user = getUserById(userId);
                    if (user instanceof UserImpl) {
                        return ((UserImpl) user).getPrivateChannel();
                    }
                    return null;
                },
                () -> {
                    Route.CompiledRoute route = Route.Self.CREATE_PRIVATE_CHANNEL.compile();
                    DataObject body = DataObject.empty().put("recipient_id", userId);
                    return new RestActionImpl<>(this, route, body, (response, request) -> getEntityBuilder()
                            .createPrivateChannel(response.getObject()));
                });
    }

    @NotNull
    @Override
    public SnowflakeCacheView<User> getUserCache() {
        return userCache;
    }

    public boolean hasSelfUser() {
        return selfUser != null;
    }

    @NotNull
    @Override
    public SelfUser getSelfUser() {
        if (selfUser == null) {
            throw new IllegalStateException("Session is not yet ready!");
        }
        return selfUser;
    }

    @Override
    public synchronized void shutdownNow() {
        requester.stop(true, this::shutdownRequester); // stop all requests
        shutdown();
        threadConfig.shutdownNow();
    }

    @Override
    public synchronized void shutdown() {
        Status status = getStatus();
        if (status == Status.SHUTDOWN || status == Status.SHUTTING_DOWN) {
            return;
        }

        setStatus(Status.SHUTTING_DOWN);

        WebSocketClient client = getClient();
        if (client != null) {
            client.getChunkManager().shutdown();
            client.shutdown();
        } else {
            shutdownInternals(new ShutdownEvent(this, OffsetDateTime.now(), 1000));
        }
    }

    public void shutdownInternals(ShutdownEvent event) {
        if (getStatus() == Status.SHUTDOWN) {
            return;
        }
        // so we can shutdown from WebSocketClient properly
        closeAudioConnections();
        guildSetupController.close();

        // stop accepting new requests
        requester.stop(false, this::shutdownRequester);
        threadConfig.shutdown();

        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (Exception ignored) {
            }
        }

        // If the requester has been shutdown too, we can fire the shutdown event
        boolean signal =
                MiscUtil.locked(statusLock, () -> shutdownEvent.getAndSet(event) == null && requesterShutdown.get());
        if (signal) {
            signalShutdown();
        }
    }

    public void shutdownRequester() {
        // Stop all request processing
        threadConfig.shutdownRequester();

        // If the websocket has been shutdown too, we can fire the shutdown event
        boolean signal =
                MiscUtil.locked(statusLock, () -> !requesterShutdown.getAndSet(true) && shutdownEvent.get() != null);
        if (signal) {
            signalShutdown();
        }
    }

    private void signalShutdown() {
        setStatus(Status.SHUTDOWN);
        handleEvent(shutdownEvent.get());
    }

    private void closeAudioConnections() {
        getAudioManagerCache().stream()
                .map(AudioManagerImpl.class::cast)
                .forEach(m -> m.closeAudioConnection(ConnectionStatus.SHUTTING_DOWN));
    }

    @Override
    public long getResponseTotal() {
        return responseTotal;
    }

    @Override
    public int getMaxReconnectDelay() {
        return sessionConfig.getMaxReconnectDelay();
    }

    @NotNull
    @Override
    public ShardInfo getShardInfo() {
        return shardInfo == null ? ShardInfo.SINGLE : shardInfo;
    }

    @NotNull
    @Override
    public Presence getPresence() {
        return presence;
    }

    @NotNull
    @Override
    public IEventManager getEventManager() {
        return eventManager.getSubject();
    }

    @Override
    public void setEventManager(IEventManager eventManager) {
        this.eventManager.setSubject(eventManager);
    }

    @Override
    public void addEventListener(@NotNull Object... listeners) {
        Checks.noneNull(listeners, "listeners");

        for (Object listener : listeners) {
            eventManager.register(listener);
        }
    }

    @Override
    public void removeEventListener(@NotNull Object... listeners) {
        Checks.noneNull(listeners, "listeners");

        for (Object listener : listeners) {
            eventManager.unregister(listener);
        }
    }

    @NotNull
    @Override
    public List<Object> getRegisteredListeners() {
        return eventManager.getRegisteredListeners();
    }

    @NotNull
    @Override
    public <E extends GenericEvent> Once.Builder<E> listenOnce(@NotNull Class<E> eventType) {
        return new Once.Builder<>(this, eventType);
    }

    @NotNull
    @Override
    public RestAction<List<Command>> retrieveCommands(boolean withLocalizations) {
        Route.CompiledRoute route = Route.Interactions.GET_COMMANDS
                .compile(getSelfUser().getApplicationId())
                .withQueryParams("with_localizations", String.valueOf(withLocalizations));

        return new RestActionImpl<>(this, route, (response, request) -> response.getArray().stream(DataArray::getObject)
                .map(json -> new CommandImpl(this, null, json))
                .collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public RestAction<Command> retrieveCommandById(@NotNull String id) {
        Checks.isSnowflake(id);
        Route.CompiledRoute route =
                Route.Interactions.GET_COMMAND.compile(getSelfUser().getApplicationId(), id);
        return new RestActionImpl<>(
                this, route, (response, request) -> new CommandImpl(this, null, response.getObject()));
    }

    @NotNull
    @Override
    public CommandCreateAction upsertCommand(@NotNull CommandData command) {
        Checks.notNull(command, "CommandData");
        return new CommandCreateActionImpl(this, (CommandDataImpl) command);
    }

    @NotNull
    @Override
    public CommandListUpdateAction updateCommands() {
        Route.CompiledRoute route =
                Route.Interactions.UPDATE_COMMANDS.compile(getSelfUser().getApplicationId());
        return new CommandListUpdateActionImpl(this, null, route);
    }

    @NotNull
    @Override
    public CommandEditAction editCommandById(@NotNull Command.Type type, @NotNull String id) {
        Checks.isSnowflake(id);
        Checks.notNull(type, "CommandType");
        Checks.check(type != Command.Type.UNKNOWN, "Type must not be UNKNOWN");
        return new CommandEditActionImpl(this, type, id);
    }

    @NotNull
    @Override
    public RestAction<Void> deleteCommandById(@NotNull String commandId) {
        Checks.isSnowflake(commandId);
        Route.CompiledRoute route =
                Route.Interactions.DELETE_COMMAND.compile(getSelfUser().getApplicationId(), commandId);
        return new RestActionImpl<>(this, route);
    }

    @NotNull
    @Override
    public RestAction<List<RoleConnectionMetadata>> retrieveRoleConnectionMetadata() {
        Route.CompiledRoute route = Route.Applications.GET_ROLE_CONNECTION_METADATA.compile(
                getSelfUser().getApplicationId());
        return new RestActionImpl<>(this, route, (response, request) -> response.getArray().stream(DataArray::getObject)
                .map(RoleConnectionMetadata::fromData)
                .collect(Helpers.toUnmodifiableList()));
    }

    @NotNull
    @Override
    public RestAction<List<RoleConnectionMetadata>> updateRoleConnectionMetadata(
            @NotNull Collection<? extends RoleConnectionMetadata> records) {
        Checks.noneNull(records, "Records");
        Checks.check(
                records.size() <= RoleConnectionMetadata.MAX_RECORDS,
                "An application can have a maximum of %d metadata records",
                RoleConnectionMetadata.MAX_RECORDS);

        Route.CompiledRoute route = Route.Applications.UPDATE_ROLE_CONNECTION_METADATA.compile(
                getSelfUser().getApplicationId());

        DataArray array = DataArray.fromCollection(records);
        RequestBody body = RequestBody.create(array.toJson(), Requester.MEDIA_TYPE_JSON);

        return new RestActionImpl<>(
                this, route, body, (response, request) -> response.getArray().stream(DataArray::getObject)
                        .map(RoleConnectionMetadata::fromData)
                        .collect(Helpers.toUnmodifiableList()));
    }

    @NotNull
    @Override
    public RestAction<Webhook> retrieveWebhookById(@NotNull String webhookId) {
        Checks.isSnowflake(webhookId, "Webhook ID");

        Route.CompiledRoute route = Route.Webhooks.GET_WEBHOOK.compile(webhookId);

        return new RestActionImpl<>(this, route, (response, request) -> {
            DataObject object = response.getObject();
            EntityBuilder builder = getEntityBuilder();
            return builder.createWebhook(object, true);
        });
    }

    @NotNull
    @Override
    public ApplicationManager getApplicationManager() {
        return new ApplicationManagerImpl(this);
    }

    @NotNull
    @Override
    public RestAction<ApplicationInfo> retrieveApplicationInfo() {
        Route.CompiledRoute route = Route.Applications.GET_BOT_APPLICATION.compile();
        return new RestActionImpl<>(this, route, (response, request) -> {
            ApplicationInfo info = getEntityBuilder().createApplicationInfo(response.getObject());
            this.clientId = info.getId();
            return info;
        });
    }

    @NotNull
    @Override
    public RestAction<List<SKU>> retrieveSKUList() {
        Route.CompiledRoute route =
                Route.Applications.GET_SKUS.compile(getSelfUser().getApplicationId());
        return new RestActionImpl<>(this, route, (response, request) -> Helpers.mapGracefully(
                        response.getArray().stream(DataArray::getObject),
                        EntityBuilder::createSKU,
                        "Failed to parse SKU")
                .collect(Helpers.toUnmodifiableList()));
    }

    @NotNull
    @Override
    public EntitlementPaginationAction retrieveEntitlements() {
        return new EntitlementPaginationActionImpl(this);
    }

    @NotNull
    @Override
    public RestAction<Entitlement> retrieveEntitlementById(long entitlementId) {
        return new RestActionImpl<>(
                this,
                Route.Applications.GET_ENTITLEMENT.compile(
                        getSelfUser().getApplicationId(), Long.toUnsignedString(entitlementId)));
    }

    @NotNull
    @Override
    public TestEntitlementCreateAction createTestEntitlement(
            long skuId, long ownerId, @NotNull TestEntitlementCreateActionImpl.OwnerType ownerType) {
        Checks.notNull(ownerType, "ownerType");

        return new TestEntitlementCreateActionImpl(this, skuId, ownerId, ownerType);
    }

    @NotNull
    @Override
    public RestAction<Void> deleteTestEntitlement(long entitlementId) {
        Route.CompiledRoute route = Route.Applications.DELETE_TEST_ENTITLEMENT.compile(
                getSelfUser().getApplicationId(), Long.toUnsignedString(entitlementId));
        return new RestActionImpl<>(this, route);
    }

    @NotNull
    @Override
    public JFA setRequiredScopes(@NotNull Collection<String> scopes) {
        Checks.noneNull(scopes, "Scopes");
        this.requiredScopes = String.join("+", scopes);
        if (!requiredScopes.contains("bot")) {
            if (requiredScopes.isEmpty()) {
                requiredScopes = "bot";
            } else {
                requiredScopes += "+bot";
            }
        }
        return this;
    }

    @NotNull
    @Override
    public String getInviteUrl(Permission... permissions) {
        StringBuilder builder = buildBaseInviteUrl();
        if (permissions != null && permissions.length > 0) {
            builder.append("&permissions=").append(Permission.getRaw(permissions));
        }
        return builder.toString();
    }

    @NotNull
    @Override
    public String getInviteUrl(Collection<Permission> permissions) {
        StringBuilder builder = buildBaseInviteUrl();
        if (permissions != null && !permissions.isEmpty()) {
            builder.append("&permissions=").append(Permission.getRaw(permissions));
        }
        return builder.toString();
    }

    private StringBuilder buildBaseInviteUrl() {
        if (clientId == null) {
            if (selfUser != null) {
                clientId = selfUser.getApplicationId();
                // populated by READY event
            } else {
                retrieveApplicationInfo().complete();
            }
        }
        StringBuilder builder = new StringBuilder("https://fluxer.com/oauth2/authorize?client_id=");
        builder.append(clientId);
        builder.append("&scope=").append(requiredScopes);
        return builder;
    }

    public void setShardManager(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    @Override
    public ShardManager getShardManager() {
        return shardManager;
    }

    public EntityBuilder getEntityBuilder() {
        return entityBuilder;
    }

    public IAudioSendFactory getAudioSendFactory() {
        return audioModuleConfig.getAudioSendFactory();
    }

    public void setGatewayPing(long ping) {
        long oldPing = this.gatewayPing;
        this.gatewayPing = ping;
        handleEvent(new GatewayPingEvent(this, oldPing));
    }

    public Requester getRequester() {
        return requester;
    }

    public WebSocketFactory getWebSocketFactory() {
        return sessionConfig.getWebSocketFactory();
    }

    public WebSocketClient getClient() {
        return client;
    }

    public SnowflakeCacheViewImpl<User> getUsersView() {
        return userCache;
    }

    public SnowflakeCacheViewImpl<Guild> getGuildsView() {
        return guildCache;
    }

    public ChannelCacheViewImpl<Channel> getChannelsView() {
        return this.channelCache;
    }

    public AbstractCacheView<AudioManager> getAudioManagersView() {
        return audioManagers;
    }

    public void setSelfUser(SelfUser selfUser) {
        try (UnlockHook hook = userCache.writeLock()) {
            userCache.getMap().put(selfUser.getIdLong(), selfUser);
        }
        this.selfUser = selfUser;
    }

    public void setResponseTotal(int responseTotal) {
        this.responseTotal = responseTotal;
    }

    public String getIdentifierString() {
        if (shardInfo != null) {
            return "JFA " + shardInfo.getShardString();
        } else {
            return "JFA";
        }
    }

    public EventCache getEventCache() {
        return eventCache;
    }

    public String getGatewayUrl() {
        if (gatewayUrl == null) {
            return gatewayUrl = getGateway();
        }
        return gatewayUrl;
    }

    public void resetGatewayUrl() {
        this.gatewayUrl = null;
    }

    public ScheduledExecutorService getAudioLifeCyclePool() {
        return threadConfig.getAudioPool(this::getIdentifierString);
    }
}
