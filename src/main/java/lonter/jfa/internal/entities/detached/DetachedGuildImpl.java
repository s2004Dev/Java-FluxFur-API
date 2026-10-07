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

package lonter.jfa.internal.entities.detached;

import lonter.jfa.api.Region;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.automod.AutoModRule;
import lonter.jfa.api.entities.automod.build.AutoModRuleData;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.*;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.DefaultGuildChannelUnion;
import lonter.jfa.api.entities.emoji.CustomEmoji;
import lonter.jfa.api.entities.emoji.RichCustomEmoji;
import lonter.jfa.api.entities.guild.SecurityIncidentActions;
import lonter.jfa.api.entities.guild.SecurityIncidentDetections;
import lonter.jfa.api.entities.guild.SystemChannelFlag;
import lonter.jfa.api.entities.sticker.GuildSticker;
import lonter.jfa.api.entities.sticker.StickerSnowflake;
import lonter.jfa.api.entities.templates.Template;
import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.PrivilegeConfig;
import lonter.jfa.api.interactions.commands.build.CommandData;
import lonter.jfa.api.interactions.commands.privileges.IntegrationPrivilege;
import lonter.jfa.api.managers.*;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.*;
import lonter.jfa.api.requests.restaction.order.CategoryOrderAction;
import lonter.jfa.api.requests.restaction.order.ChannelOrderAction;
import lonter.jfa.api.requests.restaction.order.RoleOrderAction;
import lonter.jfa.api.requests.restaction.pagination.AuditLogPaginationAction;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.cache.MemberCacheView;
import lonter.jfa.api.utils.cache.SnowflakeCacheView;
import lonter.jfa.api.utils.cache.SortedSnowflakeCacheView;
import lonter.jfa.api.utils.concurrent.Task;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.detached.mixin.IDetachableEntityMixin;
import lonter.jfa.internal.requests.restaction.pagination.BanPaginationActionImpl;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.cache.SortedChannelCacheViewImpl;

import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DetachedGuildImpl implements Guild, IDetachableEntityMixin {
    private final long id;
    private final JFAImpl api;

    private Set<String> features;
    private FluxerLocale preferredLocale = FluxerLocale.ENGLISH_US;

    public DetachedGuildImpl(JFAImpl api, long id) {
        this.id = id;
        this.api = api;
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @NotNull
    @Override
    public RestAction<List<Command>> retrieveCommands(boolean withLocalizations) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Command> retrieveCommandById(@NotNull String id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public CommandCreateAction upsertCommand(@NotNull CommandData command) {
        throw detachedException();
    }

    @NotNull
    @Override
    public CommandListUpdateAction updateCommands() {
        throw detachedException();
    }

    @NotNull
    @Override
    public CommandEditAction editCommandById(@NotNull Command.Type type, @NotNull String id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> deleteCommandById(@NotNull String commandId) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<List<IntegrationPrivilege>> retrieveIntegrationPrivilegesById(@NotNull String targetId) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<PrivilegeConfig> retrieveCommandPrivileges() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<List<AutoModRule>> retrieveAutoModRules() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<AutoModRule> retrieveAutoModRuleById(@NotNull String id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<AutoModRule> createAutoModRule(@NotNull AutoModRuleData rule) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AutoModRuleManager modifyAutoModRuleById(@NotNull String id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> deleteAutoModRuleById(@NotNull String id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public MemberAction addMember(@NotNull String accessToken, @NotNull UserSnowflake user) {
        throw detachedException();
    }

    @Override
    public boolean isLoaded() {
        throw detachedException();
    }

    @Override
    public void pruneMemberCache() {
        throw detachedException();
    }

    @Override
    public boolean unloadMember(long userId) {
        throw detachedException();
    }

    @Override
    public int getMemberCount() {
        throw detachedException();
    }

    @NotNull
    @Override
    public String getName() {
        throw detachedException();
    }

    @Override
    public String getIconId() {
        throw detachedException();
    }

    @NotNull
    @Override
    public Set<String> getFeatures() {
        return features;
    }

    @Override
    public String getSplashId() {
        throw detachedException();
    }

    @Nullable
    @Override
    public String getVanityCode() {
        throw detachedException();
    }

    @Override
    @NotNull
    public RestAction<VanityInvite> retrieveVanityInvite() {
        throw detachedException();
    }

    @Nullable
    @Override
    public String getDescription() {
        throw detachedException();
    }

    @NotNull
    @Override
    public FluxerLocale getLocale() {
        return preferredLocale;
    }

    @Nullable
    @Override
    public String getBannerId() {
        throw detachedException();
    }

    @NotNull
    @Override
    public BoostTier getBoostTier() {
        throw detachedException();
    }

    @Override
    public int getBoostCount() {
        throw detachedException();
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions") // can't be null here
    public List<Member> getBoosters() {
        throw detachedException();
    }

    @Override
    public int getMaxMembers() {
        throw detachedException();
    }

    @Override
    public int getMaxPresences() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<MetaData> retrieveMetaData() {
        throw detachedException();
    }

    @Override
    public VoiceChannel getAfkChannel() {
        throw detachedException();
    }

    @Override
    public TextChannel getSystemChannel() {
        throw detachedException();
    }

    @Override
    public TextChannel getRulesChannel() {
        throw detachedException();
    }

    //    @NotNull
    //    @Override
    //    public CacheRestAction<ScheduledEvent> retrieveScheduledEventById(@NotNull String id) {
    //        throw detachedException();
    //    }
    //
    //    @NotNull
    //    @Override
    //    public CacheRestAction<ScheduledEvent> retrieveScheduledEventById(long id) {
    //        throw detachedException();
    //    }
    //
    //    @NotNull
    //    @Override
    //    public ScheduledEventAction createScheduledEvent(
    //            @NotNull String name,
    //            @NotNull String location,
    //            @NotNull OffsetDateTime startTime,
    //            @NotNull OffsetDateTime endTime) {
    //        throw detachedException();
    //    }
    //
    //    @NotNull
    //    @Override
    //    public ScheduledEventAction createScheduledEvent(
    //            @NotNull String name, @NotNull GuildChannel channel, @NotNull OffsetDateTime startTime) {
    //        throw detachedException();
    //    }

    @Override
    public TextChannel getCommunityUpdatesChannel() {
        throw detachedException();
    }

    @Nullable
    @Override
    public TextChannel getSafetyAlertsChannel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<List<Webhook>> retrieveWebhooks() {
        throw detachedException();
    }

    @Override
    public Member getOwner() {
        throw detachedException();
    }

    @Override
    public long getOwnerIdLong() {
        throw detachedException();
    }

    @NotNull
    @Override
    public Timeout getAfkTimeout() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SecurityIncidentActions getSecurityIncidentActions() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SecurityIncidentDetections getSecurityIncidentDetections() {
        throw detachedException();
    }

    @Override
    public boolean isMember(@NotNull UserSnowflake user) {
        throw detachedException();
    }

    @NotNull
    @Override
    public SelfMember getSelfMember() {
        throw detachedException();
    }

    @Override
    public Member getMember(@NotNull UserSnowflake user) {
        throw detachedException();
    }

    @NotNull
    @Override
    public Task<List<Member>> findMembers(@NotNull Predicate<? super Member> filter) {
        throw detachedException();
    }

    @NotNull
    @Override
    public MemberCacheView getMemberCache() {
        throw detachedException();
    }

    //    @NotNull
    //    @Override
    //    public SortedSnowflakeCacheView<ScheduledEvent> getScheduledEventCache() {
    //        throw detachedException();
    //    }
    //
    //    @NotNull
    //    @Override
    //    public RestAction<List<ScheduledEvent>> retrieveScheduledEvents(boolean includeUserCount) {
    //        throw detachedException();
    //    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<Category> getCategoryCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<TextChannel> getTextChannelCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<NewsChannel> getNewsChannelCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<ForumChannel> getForumChannelCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SnowflakeCacheView<MediaChannel> getMediaChannelCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<StageChannel> getStageChannelCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<ThreadChannel> getThreadChannelCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedChannelCacheViewImpl<GuildChannel> getChannelCache() {
        throw detachedException();
    }

    @Nullable
    @Override
    public GuildChannel getGuildChannelById(long id) {
        throw detachedException();
    }

    @Override
    public GuildChannel getGuildChannelById(@NotNull ChannelType type, long id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheView<Role> getRoleCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SnowflakeCacheView<RichCustomEmoji> getEmojiCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public SnowflakeCacheView<GuildSticker> getStickerCache() {
        throw detachedException();
    }

    @NotNull
    @Override
    public List<GuildChannel> getChannels(boolean includeHidden) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<List<RichCustomEmoji>> retrieveEmojis() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<RichCustomEmoji> retrieveEmojiById(@NotNull String id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<RichCustomEmoji> retrieveEmoji(@NotNull CustomEmoji emoji) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<List<GuildSticker>> retrieveStickers() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<GuildSticker> retrieveSticker(@NotNull StickerSnowflake sticker) {
        throw detachedException();
    }

    @NotNull
    @Override
    public GuildStickerManager editSticker(@NotNull StickerSnowflake sticker) {
        throw detachedException();
    }

    @NotNull
    @Override
    public BanPaginationActionImpl retrieveBanList() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Ban> retrieveBan(@NotNull UserSnowflake user) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Integer> retrievePrunableMemberCount(int days) {
        throw detachedException();
    }

    @NotNull
    @Override
    public Role getPublicRole() {
        throw detachedException();
    }

    @Nullable
    @Override
    public DefaultGuildChannelUnion getDefaultChannel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public GuildManager getManager() {
        throw detachedException();
    }

    @Override
    public boolean isBoostProgressBarEnabled() {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditLogPaginationAction retrieveAuditLogs() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> leave() {
        throw detachedException();
    }

    @NotNull
    @Override
    public AudioManager getAudioManager() {
        throw detachedException();
    }

    @NotNull
    @Override
    public synchronized Task<Void> requestToSpeak() {
        throw detachedException();
    }

    @NotNull
    @Override
    public synchronized Task<Void> cancelRequestToSpeak() {
        throw detachedException();
    }

    @NotNull
    @Override
    public JFAImpl getJFA() {
        return api;
    }

    @NotNull
    @Override
    public List<GuildVoiceState> getVoiceStates() {
        throw detachedException();
    }

    @NotNull
    @Override
    public CacheRestAction<GuildVoiceState> retrieveMemberVoiceStateById(long id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public VerificationLevel getVerificationLevel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public NSFWLevel getNSFWLevel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public Set<SystemChannelFlag> getSystemChannelFlags() {
        throw detachedException();
    }

    @Override
    public int getSystemChannelFlagsRaw() {
        throw detachedException();
    }

    @NotNull
    @Override
    public NotificationLevel getDefaultNotificationLevel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public MFALevel getRequiredMFALevel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ExplicitContentLevel getExplicitContentLevel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public Task<Void> loadMembers(@NotNull Consumer<Member> callback) {
        throw detachedException();
    }

    @NotNull
    @Override
    public CacheRestAction<Member> retrieveMemberById(long id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public Task<List<Member>> retrieveMembersByIds(boolean includePresence, @NotNull long... ids) {
        throw detachedException();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public Task<List<Member>> retrieveMembersByPrefix(@NotNull String prefix, int limit) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<List<ThreadChannel>> retrieveActiveThreads() {
        throw detachedException();
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @NotNull
    @Override
    public RestAction<List<Invite>> retrieveInvites() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<List<Template>> retrieveTemplates() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Template> createTemplate(@NotNull String name, @Nullable String description) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<GuildWelcomeScreen> retrieveWelcomeScreen() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> moveVoiceMember(@NotNull UserSnowflake user, @Nullable AudioChannel audioChannel) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> modifyNickname(@NotNull Member member, String nickname) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Integer> prune(int days, boolean wait, @NotNull Role... roles) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> modifySecurityIncidents(@NotNull SecurityIncidentActions incidents) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> kick(@NotNull UserSnowflake user) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> ban(@NotNull UserSnowflake user, int duration, @NotNull TimeUnit unit) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<BulkBanResponse> ban(
            @NotNull Collection<? extends UserSnowflake> users, @Nullable Duration deletionTime) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> unban(@NotNull UserSnowflake user) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> timeoutUntil(@NotNull UserSnowflake user, @NotNull TemporalAccessor temporal) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> removeTimeout(@NotNull UserSnowflake user) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> deafen(@NotNull UserSnowflake user, boolean deafen) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> mute(@NotNull UserSnowflake user, boolean mute) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> addRoleToMember(@NotNull UserSnowflake user, @NotNull Role role) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> removeRoleFromMember(@NotNull UserSnowflake user, @NotNull Role role) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> modifyMemberRoles(
            @NotNull Member member, Collection<Role> rolesToAdd, Collection<Role> rolesToRemove) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> modifyMemberRoles(@NotNull Member member, @NotNull Collection<Role> roles) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<RoleMemberCounts> retrieveRoleMemberCounts() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<TextChannel> createTextChannel(@NotNull String name, Category parent) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<NewsChannel> createNewsChannel(@NotNull String name, Category parent) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@NotNull String name, Category parent) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@NotNull String name, Category parent) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<ForumChannel> createForumChannel(@NotNull String name, Category parent) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<MediaChannel> createMediaChannel(@NotNull String name, @Nullable Category parent) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<Category> createCategory(@NotNull String name) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RoleAction createRole() {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<RichCustomEmoji> createEmoji(
            @NotNull String name, @NotNull Icon icon, @NotNull Role... roles) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<GuildSticker> createSticker(
            @NotNull String name,
            @NotNull String description,
            @NotNull FileUpload file,
            @NotNull Collection<String> tags) {
        throw detachedException();
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> deleteSticker(@NotNull StickerSnowflake id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelOrderAction modifyCategoryPositions() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelOrderAction modifyTextChannelPositions() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelOrderAction modifyVoiceChannelPositions() {
        throw detachedException();
    }

    @NotNull
    @Override
    public CategoryOrderAction modifyTextChannelPositions(@NotNull Category category) {
        throw detachedException();
    }

    @NotNull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions(@NotNull Category category) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RoleOrderAction modifyRolePositions(boolean useAscendingOrder) {
        throw detachedException();
    }

    @NotNull
    @Override
    public GuildWelcomeScreenManager modifyWelcomeScreen() {
        throw detachedException();
    }

    // ---- Setters -----

    public DetachedGuildImpl setFeatures(Set<String> features) {
        this.features = Collections.unmodifiableSet(features);
        return this;
    }

    public DetachedGuildImpl setLocale(FluxerLocale locale) {
        this.preferredLocale = locale;
        return this;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DetachedGuildImpl)) {
            return false;
        }
        DetachedGuildImpl oGuild = (DetachedGuildImpl) o;
        return this.id == oGuild.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return new EntityString(this).toString();
    }
}
