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

package lonter.jfa.api.hooks;

import lonter.jfa.annotations.ReplaceWith;
import lonter.jfa.api.events.*;
import lonter.jfa.api.events.automod.*;
import lonter.jfa.api.events.channel.ChannelCreateEvent;
import lonter.jfa.api.events.channel.ChannelDeleteEvent;
import lonter.jfa.api.events.channel.GenericChannelEvent;
import lonter.jfa.api.events.channel.forum.ForumTagAddEvent;
import lonter.jfa.api.events.channel.forum.ForumTagRemoveEvent;
import lonter.jfa.api.events.channel.forum.GenericForumTagEvent;
import lonter.jfa.api.events.channel.forum.update.ForumTagUpdateEmojiEvent;
import lonter.jfa.api.events.channel.forum.update.ForumTagUpdateModeratedEvent;
import lonter.jfa.api.events.channel.forum.update.ForumTagUpdateNameEvent;
import lonter.jfa.api.events.channel.forum.update.GenericForumTagUpdateEvent;
import lonter.jfa.api.events.channel.update.*;
import lonter.jfa.api.events.emoji.EmojiAddedEvent;
import lonter.jfa.api.events.emoji.EmojiRemovedEvent;
import lonter.jfa.api.events.emoji.GenericEmojiEvent;
import lonter.jfa.api.events.emoji.update.EmojiUpdateNameEvent;
import lonter.jfa.api.events.emoji.update.EmojiUpdateRolesEvent;
import lonter.jfa.api.events.emoji.update.GenericEmojiUpdateEvent;
import lonter.jfa.api.events.entitlement.EntitlementCreateEvent;
import lonter.jfa.api.events.entitlement.EntitlementDeleteEvent;
import lonter.jfa.api.events.entitlement.EntitlementUpdateEvent;
import lonter.jfa.api.events.entitlement.GenericEntitlementEvent;
import lonter.jfa.api.events.guild.*;
import lonter.jfa.api.events.guild.invite.GenericGuildInviteEvent;
import lonter.jfa.api.events.guild.invite.GuildInviteCreateEvent;
import lonter.jfa.api.events.guild.invite.GuildInviteDeleteEvent;
import lonter.jfa.api.events.guild.member.*;
import lonter.jfa.api.events.guild.member.update.*;
import lonter.jfa.api.events.guild.override.GenericPermissionOverrideEvent;
import lonter.jfa.api.events.guild.override.PermissionOverrideCreateEvent;
import lonter.jfa.api.events.guild.override.PermissionOverrideDeleteEvent;
import lonter.jfa.api.events.guild.override.PermissionOverrideUpdateEvent;
import lonter.jfa.api.events.guild.scheduledevent.*;
import lonter.jfa.api.events.guild.scheduledevent.update.*;
import lonter.jfa.api.events.guild.update.*;
import lonter.jfa.api.events.guild.voice.*;
import lonter.jfa.api.events.http.HttpRequestEvent;
import lonter.jfa.api.events.interaction.GenericAutoCompleteInteractionEvent;
import lonter.jfa.api.events.interaction.GenericInteractionCreateEvent;
import lonter.jfa.api.events.interaction.ModalInteractionEvent;
import lonter.jfa.api.events.interaction.command.*;
import lonter.jfa.api.events.interaction.component.*;
import lonter.jfa.api.events.message.*;
import lonter.jfa.api.events.message.poll.GenericMessagePollVoteEvent;
import lonter.jfa.api.events.message.poll.MessagePollVoteAddEvent;
import lonter.jfa.api.events.message.poll.MessagePollVoteRemoveEvent;
import lonter.jfa.api.events.message.react.*;
import lonter.jfa.api.events.role.GenericRoleEvent;
import lonter.jfa.api.events.role.RoleCreateEvent;
import lonter.jfa.api.events.role.RoleDeleteEvent;
import lonter.jfa.api.events.role.update.*;
import lonter.jfa.api.events.self.*;
import lonter.jfa.api.events.session.*;
import lonter.jfa.api.events.stage.GenericStageInstanceEvent;
import lonter.jfa.api.events.stage.StageInstanceCreateEvent;
import lonter.jfa.api.events.stage.StageInstanceDeleteEvent;
import lonter.jfa.api.events.stage.update.GenericStageInstanceUpdateEvent;
import lonter.jfa.api.events.stage.update.StageInstanceUpdatePrivacyLevelEvent;
import lonter.jfa.api.events.stage.update.StageInstanceUpdateTopicEvent;
import lonter.jfa.api.events.sticker.GenericGuildStickerEvent;
import lonter.jfa.api.events.sticker.GuildStickerAddedEvent;
import lonter.jfa.api.events.sticker.GuildStickerRemovedEvent;
import lonter.jfa.api.events.sticker.update.*;
import lonter.jfa.api.events.thread.GenericThreadEvent;
import lonter.jfa.api.events.thread.ThreadHiddenEvent;
import lonter.jfa.api.events.thread.ThreadRevealedEvent;
import lonter.jfa.api.events.thread.member.GenericThreadMemberEvent;
import lonter.jfa.api.events.thread.member.ThreadMemberJoinEvent;
import lonter.jfa.api.events.thread.member.ThreadMemberLeaveEvent;
import lonter.jfa.api.events.user.GenericUserEvent;
import lonter.jfa.api.events.user.UserActivityEndEvent;
import lonter.jfa.api.events.user.UserActivityStartEvent;
import lonter.jfa.api.events.user.UserTypingEvent;
import lonter.jfa.api.events.user.update.*;
import lonter.jfa.internal.utils.ClassWalker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;

/**
 * An abstract implementation of {@link lonter.jfa.api.hooks.EventListener EventListener} which divides {@link lonter.jfa.api.events.Event Events}
 * for you. You should <b><u>override</u></b> the methods provided by this class for your event listener implementation.
 *
 * <p><b>Example:</b><br>
 * {@snippet lang="java":
 * public class MyReadyListener extends ListenerAdapter {
 *     @Override
 *     public void onReady(ReadyEvent event) {
 *         System.out.println("I am ready to go!");
 *     }
 *
 *     @Override
 *     public void onMessageReceived(MessageReceivedEvent event) {
 *         System.out.printf("[%s]: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());
 *     }
 * }
 * }
 *
 * @see lonter.jfa.api.hooks.EventListener EventListener
 * @see lonter.jfa.api.hooks.InterfacedEventManager InterfacedEventManager
 */
public abstract class ListenerAdapter implements EventListener {
    public void onGenericEvent(@NotNull GenericEvent event) {}

    public void onGenericUpdate(@NotNull UpdateEvent<?, ?> event) {}

    public void onRawGateway(@NotNull RawGatewayEvent event) {}

    public void onGatewayPing(@NotNull GatewayPingEvent event) {}

    // Session Events
    public void onReady(@NotNull ReadyEvent event) {}

    public void onSessionInvalidate(@NotNull SessionInvalidateEvent event) {}

    public void onSessionDisconnect(@NotNull SessionDisconnectEvent event) {}

    public void onSessionResume(@NotNull SessionResumeEvent event) {}

    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {}

    public void onShutdown(@NotNull ShutdownEvent event) {}

    // Status Events
    public void onStatusChange(@NotNull StatusChangeEvent event) {}

    public void onException(@NotNull ExceptionEvent event) {}

    // Interaction Events
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {}

    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {}

    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {}

    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {}

    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {}

    public void onModalInteraction(@NotNull ModalInteractionEvent event) {}

    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {}

    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {}

    // User Events
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {}

    public void onUserUpdateGlobalName(@NotNull UserUpdateGlobalNameEvent event) {}

    public void onUserUpdateDiscriminator(@NotNull UserUpdateDiscriminatorEvent event) {}

    public void onUserUpdateAvatar(@NotNull UserUpdateAvatarEvent event) {}

    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {}

    public void onUserUpdateActivityOrder(@NotNull UserUpdateActivityOrderEvent event) {}

    public void onUserUpdateFlags(@NotNull UserUpdateFlagsEvent event) {}

    public void onUserUpdatePrimaryGuild(@NotNull UserUpdatePrimaryGuildEvent event) {}

    public void onUserTyping(@NotNull UserTypingEvent event) {}

    public void onUserActivityStart(@NotNull UserActivityStartEvent event) {}

    public void onUserActivityEnd(@NotNull UserActivityEndEvent event) {}

    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {}

    // Self Events. Fires only in relation to the currently logged in account.
    public void onSelfUpdateAvatar(@NotNull SelfUpdateAvatarEvent event) {}

    public void onSelfUpdateMFA(@NotNull SelfUpdateMFAEvent event) {}

    public void onSelfUpdateName(@NotNull SelfUpdateNameEvent event) {}

    public void onSelfUpdateDiscriminator(@NotNull SelfUpdateDiscriminatorEvent event) {}

    public void onSelfUpdateGlobalName(@NotNull SelfUpdateGlobalNameEvent event) {}

    public void onSelfUpdateVerified(@NotNull SelfUpdateVerifiedEvent event) {}

    // Message Events
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {}

    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {}

    public void onMessageDelete(@NotNull MessageDeleteEvent event) {}

    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {}

    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {}

    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {}

    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {}

    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {}

    public void onMessagePollVoteAdd(@NotNull MessagePollVoteAddEvent event) {}

    public void onMessagePollVoteRemove(@NotNull MessagePollVoteRemoveEvent event) {}

    // PermissionOverride Events
    public void onPermissionOverrideDelete(@NotNull PermissionOverrideDeleteEvent event) {}

    public void onPermissionOverrideUpdate(@NotNull PermissionOverrideUpdateEvent event) {}

    public void onPermissionOverrideCreate(@NotNull PermissionOverrideCreateEvent event) {}

    // StageInstance Event
    public void onStageInstanceDelete(@NotNull StageInstanceDeleteEvent event) {}

    public void onStageInstanceUpdateTopic(@NotNull StageInstanceUpdateTopicEvent event) {}

    public void onStageInstanceUpdatePrivacyLevel(@NotNull StageInstanceUpdatePrivacyLevelEvent event) {}

    public void onStageInstanceCreate(@NotNull StageInstanceCreateEvent event) {}

    // Channel Events
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {}

    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {}

    // Channel Update Events
    public void onChannelUpdateBitrate(@NotNull ChannelUpdateBitrateEvent event) {}

    public void onChannelUpdateName(@NotNull ChannelUpdateNameEvent event) {}

    public void onChannelUpdateFlags(@NotNull ChannelUpdateFlagsEvent event) {}

    public void onChannelUpdateNSFW(@NotNull ChannelUpdateNSFWEvent event) {}

    public void onChannelUpdateParent(@NotNull ChannelUpdateParentEvent event) {}

    public void onChannelUpdatePosition(@NotNull ChannelUpdatePositionEvent event) {}

    public void onChannelUpdateRegion(@NotNull ChannelUpdateRegionEvent event) {}

    public void onChannelUpdateSlowmode(@NotNull ChannelUpdateSlowmodeEvent event) {}

    public void onChannelUpdateDefaultThreadSlowmode(@NotNull ChannelUpdateDefaultThreadSlowmodeEvent event) {}

    public void onChannelUpdateDefaultReaction(@NotNull ChannelUpdateDefaultReactionEvent event) {}

    public void onChannelUpdateDefaultSortOrder(@NotNull ChannelUpdateDefaultSortOrderEvent event) {}

    public void onChannelUpdateDefaultLayout(@NotNull ChannelUpdateDefaultLayoutEvent event) {}

    public void onChannelUpdateTopic(@NotNull ChannelUpdateTopicEvent event) {}

    public void onChannelUpdateVoiceStatus(@NotNull ChannelUpdateVoiceStatusEvent event) {}

    public void onChannelUpdateType(@NotNull ChannelUpdateTypeEvent event) {}

    public void onChannelUpdateUserLimit(@NotNull ChannelUpdateUserLimitEvent event) {}

    public void onChannelUpdateArchived(@NotNull ChannelUpdateArchivedEvent event) {}

    public void onChannelUpdateArchiveTimestamp(@NotNull ChannelUpdateArchiveTimestampEvent event) {}

    public void onChannelUpdateAutoArchiveDuration(@NotNull ChannelUpdateAutoArchiveDurationEvent event) {}

    public void onChannelUpdateLocked(@NotNull ChannelUpdateLockedEvent event) {}

    public void onChannelUpdateInvitable(@NotNull ChannelUpdateInvitableEvent event) {}

    public void onChannelUpdateAppliedTags(@NotNull ChannelUpdateAppliedTagsEvent event) {}

    // Forum Tag Events
    public void onForumTagAdd(@NotNull ForumTagAddEvent event) {}

    public void onForumTagRemove(@NotNull ForumTagRemoveEvent event) {}

    public void onForumTagUpdateName(@NotNull ForumTagUpdateNameEvent event) {}

    public void onForumTagUpdateEmoji(@NotNull ForumTagUpdateEmojiEvent event) {}

    public void onForumTagUpdateModerated(@NotNull ForumTagUpdateModeratedEvent event) {}

    // Thread Events
    public void onThreadRevealed(@NotNull ThreadRevealedEvent event) {}

    public void onThreadHidden(@NotNull ThreadHiddenEvent event) {}

    // Thread Member Events
    public void onThreadMemberJoin(@NotNull ThreadMemberJoinEvent event) {}

    public void onThreadMemberLeave(@NotNull ThreadMemberLeaveEvent event) {}

    // Guild Events
    public void onGuildReady(@NotNull GuildReadyEvent event) {}

    public void onGuildTimeout(@NotNull GuildTimeoutEvent event) {}

    public void onGuildJoin(@NotNull GuildJoinEvent event) {}

    public void onGuildLeave(@NotNull GuildLeaveEvent event) {}

    public void onGuildAvailable(@NotNull GuildAvailableEvent event) {}

    public void onGuildUnavailable(@NotNull GuildUnavailableEvent event) {}

    public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event) {}

    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {}

    public void onGuildBan(@NotNull GuildBanEvent event) {}

    public void onGuildUnban(@NotNull GuildUnbanEvent event) {}

    public void onGuildAuditLogEntryCreate(@NotNull GuildAuditLogEntryCreateEvent event) {}

    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {}

    // Guild Update Events
    public void onGuildUpdateAfkChannel(@NotNull GuildUpdateAfkChannelEvent event) {}

    public void onGuildUpdateSystemChannel(@NotNull GuildUpdateSystemChannelEvent event) {}

    public void onGuildUpdateRulesChannel(@NotNull GuildUpdateRulesChannelEvent event) {}

    public void onGuildUpdateCommunityUpdatesChannel(@NotNull GuildUpdateCommunityUpdatesChannelEvent event) {}

    public void onGuildUpdateSafetyAlertsChannel(@NotNull GuildUpdateSafetyAlertsChannelEvent event) {}

    public void onGuildUpdateAfkTimeout(@NotNull GuildUpdateAfkTimeoutEvent event) {}

    public void onGuildUpdateSecurityIncidentActions(@NotNull GuildUpdateSecurityIncidentActionsEvent event) {}

    public void onGuildUpdateSecurityIncidentDetections(@NotNull GuildUpdateSecurityIncidentDetectionsEvent event) {}

    public void onGuildUpdateExplicitContentLevel(@NotNull GuildUpdateExplicitContentLevelEvent event) {}

    public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent event) {}

    public void onGuildUpdateMFALevel(@NotNull GuildUpdateMFALevelEvent event) {}

    public void onGuildUpdateName(@NotNull GuildUpdateNameEvent event) {}

    public void onGuildUpdateNotificationLevel(@NotNull GuildUpdateNotificationLevelEvent event) {}

    public void onGuildUpdateOwner(@NotNull GuildUpdateOwnerEvent event) {}

    public void onGuildUpdateSplash(@NotNull GuildUpdateSplashEvent event) {}

    public void onGuildUpdateVerificationLevel(@NotNull GuildUpdateVerificationLevelEvent event) {}

    public void onGuildUpdateLocale(@NotNull GuildUpdateLocaleEvent event) {}

    public void onGuildUpdateFeatures(@NotNull GuildUpdateFeaturesEvent event) {}

    public void onGuildUpdateVanityCode(@NotNull GuildUpdateVanityCodeEvent event) {}

    public void onGuildUpdateBanner(@NotNull GuildUpdateBannerEvent event) {}

    public void onGuildUpdateDescription(@NotNull GuildUpdateDescriptionEvent event) {}

    public void onGuildUpdateSystemChannelFlags(@NotNull GuildUpdateSystemChannelFlagsEvent event) {}

    public void onGuildUpdateBoostTier(@NotNull GuildUpdateBoostTierEvent event) {}

    public void onGuildUpdateBoostCount(@NotNull GuildUpdateBoostCountEvent event) {}

    public void onGuildUpdateMaxMembers(@NotNull GuildUpdateMaxMembersEvent event) {}

    public void onGuildUpdateMaxPresences(@NotNull GuildUpdateMaxPresencesEvent event) {}

    public void onGuildUpdateNSFWLevel(@NotNull GuildUpdateNSFWLevelEvent event) {}

    // Scheduled Event Events
    public void onScheduledEventUpdateDescription(@NotNull ScheduledEventUpdateDescriptionEvent event) {}

    public void onScheduledEventUpdateEndTime(@NotNull ScheduledEventUpdateEndTimeEvent event) {}

    public void onScheduledEventUpdateLocation(@NotNull ScheduledEventUpdateLocationEvent event) {}

    public void onScheduledEventUpdateName(@NotNull ScheduledEventUpdateNameEvent event) {}

    public void onScheduledEventUpdateStartTime(@NotNull ScheduledEventUpdateStartTimeEvent event) {}

    public void onScheduledEventUpdateStatus(@NotNull ScheduledEventUpdateStatusEvent event) {}

    /**
     * Deprecated.
     *
     * @deprecated Replaced by {@link ScheduledEventUpdateCoverImageEvent},
     *             note that the values previously were {@linkplain lonter.jfa.api.entities.ScheduledEvent#getImageUrl() asset URLs}
     *             and now are {@linkplain lonter.jfa.api.entities.ScheduledEvent#getCoverImageId() asset hashes}.
     *             <br>Additionally, they were previously marked as non-null, when they are actually both nullable.
     */
    @Deprecated
    @ReplaceWith("onScheduledEventUpdateCoverImage(ScheduledEventUpdateCoverImageEvent)")
    public void onScheduledEventUpdateImage(@NotNull ScheduledEventUpdateImageEvent event) {}

    public void onScheduledEventUpdateCoverImage(@NotNull ScheduledEventUpdateCoverImageEvent event) {}

    public void onScheduledEventCreate(@NotNull ScheduledEventCreateEvent event) {}

    public void onScheduledEventDelete(@NotNull ScheduledEventDeleteEvent event) {}

    public void onScheduledEventUserAdd(@NotNull ScheduledEventUserAddEvent event) {}

    public void onScheduledEventUserRemove(@NotNull ScheduledEventUserRemoveEvent event) {}

    // Guild Invite Events
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {}

    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {}

    // Guild Member Events
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {}

    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {}

    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {}

    // Guild Member Update Events
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {}

    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {}

    public void onGuildMemberUpdateAvatar(@NotNull GuildMemberUpdateAvatarEvent event) {}

    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {}

    public void onGuildMemberUpdatePending(@NotNull GuildMemberUpdatePendingEvent event) {}

    public void onGuildMemberUpdateFlags(@NotNull GuildMemberUpdateFlagsEvent event) {}

    public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {}

    // Guild Voice Events
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {}

    public void onGuildVoiceMute(@NotNull GuildVoiceMuteEvent event) {}

    public void onGuildVoiceDeafen(@NotNull GuildVoiceDeafenEvent event) {}

    public void onGuildVoiceGuildMute(@NotNull GuildVoiceGuildMuteEvent event) {}

    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {}

    public void onGuildVoiceSelfMute(@NotNull GuildVoiceSelfMuteEvent event) {}

    public void onGuildVoiceSelfDeafen(@NotNull GuildVoiceSelfDeafenEvent event) {}

    public void onGuildVoiceSuppress(@NotNull GuildVoiceSuppressEvent event) {}

    public void onGuildVoiceStream(@NotNull GuildVoiceStreamEvent event) {}

    public void onGuildVoiceVideo(@NotNull GuildVoiceVideoEvent event) {}

    public void onGuildVoiceRequestToSpeak(@NotNull GuildVoiceRequestToSpeakEvent event) {}

    // Guild AutoMod Events
    public void onAutoModExecution(@NotNull AutoModExecutionEvent event) {}

    public void onAutoModRuleCreate(@NotNull AutoModRuleCreateEvent event) {}

    public void onAutoModRuleUpdate(@NotNull AutoModRuleUpdateEvent event) {}

    public void onAutoModRuleDelete(@NotNull AutoModRuleDeleteEvent event) {}

    // Role events
    public void onRoleCreate(@NotNull RoleCreateEvent event) {}

    public void onRoleDelete(@NotNull RoleDeleteEvent event) {}

    // Role Update Events
    @Deprecated
    @ReplaceWith("onRoleUpdateColors(event)")
    public void onRoleUpdateColor(@NotNull RoleUpdateColorEvent event) {}

    public void onRoleUpdateColors(@NotNull RoleUpdateColorsEvent event) {}

    public void onRoleUpdateHoisted(@NotNull RoleUpdateHoistedEvent event) {}

    public void onRoleUpdateIcon(@NotNull RoleUpdateIconEvent event) {}

    public void onRoleUpdateMentionable(@NotNull RoleUpdateMentionableEvent event) {}

    public void onRoleUpdateName(@NotNull RoleUpdateNameEvent event) {}

    public void onRoleUpdatePermissions(@NotNull RoleUpdatePermissionsEvent event) {}

    public void onRoleUpdatePosition(@NotNull RoleUpdatePositionEvent event) {}

    // Emoji Events
    public void onEmojiAdded(@NotNull EmojiAddedEvent event) {}

    public void onEmojiRemoved(@NotNull EmojiRemovedEvent event) {}

    // Emoji Update Events
    public void onEmojiUpdateName(@NotNull EmojiUpdateNameEvent event) {}

    public void onEmojiUpdateRoles(@NotNull EmojiUpdateRolesEvent event) {}

    // Application command permission update events
    public void onGenericPrivilegeUpdate(@NotNull GenericPrivilegeUpdateEvent event) {}

    public void onApplicationCommandUpdatePrivileges(@NotNull ApplicationCommandUpdatePrivilegesEvent event) {}

    public void onApplicationUpdatePrivileges(@NotNull ApplicationUpdatePrivilegesEvent event) {}

    // Sticker Events
    public void onGuildStickerAdded(@NotNull GuildStickerAddedEvent event) {}

    public void onGuildStickerRemoved(@NotNull GuildStickerRemovedEvent event) {}

    // Sticker Update Events
    public void onGuildStickerUpdateName(@NotNull GuildStickerUpdateNameEvent event) {}

    public void onGuildStickerUpdateTags(@NotNull GuildStickerUpdateTagsEvent event) {}

    public void onGuildStickerUpdateDescription(@NotNull GuildStickerUpdateDescriptionEvent event) {}

    public void onGuildStickerUpdateAvailable(@NotNull GuildStickerUpdateAvailableEvent event) {}

    // Entitlement events
    public void onEntitlementCreate(@NotNull EntitlementCreateEvent event) {}

    public void onEntitlementUpdate(@NotNull EntitlementUpdateEvent event) {}

    public void onEntitlementDelete(@NotNull EntitlementDeleteEvent event) {}

    // Debug Events
    public void onHttpRequest(@NotNull HttpRequestEvent event) {}

    // Generic Events
    public void onGenericSession(@NotNull GenericSessionEvent event) {}

    public void onGenericInteractionCreate(@NotNull GenericInteractionCreateEvent event) {}

    public void onGenericAutoCompleteInteraction(@NotNull GenericAutoCompleteInteractionEvent event) {}

    public void onGenericComponentInteractionCreate(@NotNull GenericComponentInteractionCreateEvent event) {}

    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {}

    public void onGenericContextInteraction(@NotNull GenericContextInteractionEvent<?> event) {}

    public void onGenericSelectMenuInteraction(@NotNull GenericSelectMenuInteractionEvent<?, ?> event) {}

    public void onGenericMessage(@NotNull GenericMessageEvent event) {}

    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {}

    public void onGenericMessagePollVote(@NotNull GenericMessagePollVoteEvent event) {}

    public void onGenericUser(@NotNull GenericUserEvent event) {}

    public void onGenericUserPresence(@NotNull GenericUserPresenceEvent event) {}

    public void onGenericUserUpdate(@NotNull GenericUserUpdateEvent<?> event) {}

    public void onGenericSelfUpdate(@NotNull GenericSelfUpdateEvent<?> event) {}

    public void onGenericStageInstance(@NotNull GenericStageInstanceEvent event) {}

    public void onGenericStageInstanceUpdate(@NotNull GenericStageInstanceUpdateEvent<?> event) {}

    public void onGenericChannel(@NotNull GenericChannelEvent event) {}

    public void onGenericChannelUpdate(@NotNull GenericChannelUpdateEvent<?> event) {}

    public void onGenericThread(@NotNull GenericThreadEvent event) {}

    public void onGenericThreadMember(@NotNull GenericThreadMemberEvent event) {}

    public void onGenericGuild(@NotNull GenericGuildEvent event) {}

    public void onGenericGuildUpdate(@NotNull GenericGuildUpdateEvent<?> event) {}

    public void onGenericGuildInvite(@NotNull GenericGuildInviteEvent event) {}

    public void onGenericGuildMember(@NotNull GenericGuildMemberEvent event) {}

    public void onGenericGuildMemberUpdate(@NotNull GenericGuildMemberUpdateEvent<?> event) {}

    public void onGenericGuildVoice(@NotNull GenericGuildVoiceEvent event) {}

    public void onGenericAutoModRule(@NotNull GenericAutoModRuleEvent event) {}

    public void onGenericRole(@NotNull GenericRoleEvent event) {}

    public void onGenericRoleUpdate(@NotNull GenericRoleUpdateEvent<?> event) {}

    public void onGenericEmoji(@NotNull GenericEmojiEvent event) {}

    public void onGenericEmojiUpdate(@NotNull GenericEmojiUpdateEvent<?> event) {}

    public void onGenericGuildSticker(@NotNull GenericGuildStickerEvent event) {}

    public void onGenericGuildStickerUpdate(@NotNull GenericGuildStickerUpdateEvent<?> event) {}

    public void onGenericEntitlement(@NotNull GenericEntitlementEvent event) {}

    public void onGenericPermissionOverride(@NotNull GenericPermissionOverrideEvent event) {}

    public void onGenericScheduledEventUpdate(@NotNull GenericScheduledEventUpdateEvent<?> event) {}

    public void onGenericScheduledEventGateway(@NotNull GenericScheduledEventGatewayEvent event) {}

    public void onGenericScheduledEventUser(@NotNull GenericScheduledEventUserEvent event) {}

    public void onGenericForumTag(@NotNull GenericForumTagEvent event) {}

    public void onGenericForumTagUpdate(@NotNull GenericForumTagUpdateEvent<?> event) {}

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final ConcurrentMap<Class<?>, MethodHandle> methods = new ConcurrentHashMap<>();
    private static final Set<Class<?>> unresolved;

    static {
        unresolved = ConcurrentHashMap.newKeySet();
        Collections.addAll(
                unresolved,
                Object.class, // Objects aren't events
                Event.class, // onEvent is final and would never be found
                UpdateEvent.class, // onGenericUpdate has already been called
                GenericEvent.class // onGenericEvent has already been called
                );
    }

    @Override
    public final void onEvent(@NotNull GenericEvent event) {
        onGenericEvent(event);
        if (event instanceof UpdateEvent) {
            onGenericUpdate((UpdateEvent<?, ?>) event);
        }

        for (Class<?> clazz : ClassWalker.range(event.getClass(), GenericEvent.class)) {
            if (unresolved.contains(clazz)) {
                continue;
            }
            MethodHandle mh = methods.computeIfAbsent(clazz, ListenerAdapter::findMethod);
            if (mh == null) {
                unresolved.add(clazz);
                continue;
            }

            try {
                mh.invoke(this, event);
            } catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                }
                throw new IllegalStateException(throwable);
            }
        }
    }

    private static MethodHandle findMethod(Class<?> clazz) {
        String name = clazz.getSimpleName();
        MethodType type = MethodType.methodType(Void.TYPE, clazz);
        try {
            name = "on" + name.substring(0, name.length() - "Event".length());
            return lookup.findVirtual(ListenerAdapter.class, name, type);
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        } // this means this is probably a custom event!
        return null;
    }
}
