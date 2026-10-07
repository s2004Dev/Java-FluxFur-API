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

package lonter.jfa.api.requests.restaction;

import lonter.jfa.api.entities.Invite;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.internal.utils.Checks;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link lonter.jfa.api.entities.Invite Invite} Builder system created as an extension of {@link lonter.jfa.api.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Fluxer to create {@link lonter.jfa.api.entities.Invite Invites}.
 *
 * @see lonter.jfa.api.entities.channel.attribute.IInviteContainer#createInvite()
 */
public interface InviteAction extends AuditableRestAction<Invite> {
    @NotNull
    @Override
    @CheckReturnValue
    InviteAction setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    InviteAction timeout(long timeout, @NotNull TimeUnit unit);

    @NotNull
    @Override
    @CheckReturnValue
    InviteAction deadline(long timestamp);

    /**
     * Sets the max age in seconds for the invite. Set this to {@code 0} if the invite should never expire. Default is {@code 86400} (24 hours).
     * {@code null} will reset this to the default value.
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     *
     * @throws IllegalArgumentException
     *         If maxAge is negative.
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    InviteAction setMaxAge(@Nullable Integer maxAge);

    /**
     * Sets the max age for the invite. Set this to {@code 0} if the invite should never expire. Default is {@code 86400} (24 hours).
     * {@code null} will reset this to the default value.
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     * @param  timeUnit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} type of {@code maxAge}.
     *
     * @throws IllegalArgumentException
     *         If maxAge is negative or maxAge is positive and timeUnit is null.
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    InviteAction setMaxAge(@Nullable Long maxAge, @NotNull TimeUnit timeUnit);

    /**
     * Sets the max uses for the invite. Set this to {@code 0} if the invite should have unlimited uses. Default is {@code 0}.
     * {@code null} will reset this to the default value.
     *
     * @param  maxUses
     *         The max uses for this invite or {@code null} to use the default value.
     *
     * @throws IllegalArgumentException
     *         If maxUses is negative.
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    InviteAction setMaxUses(@Nullable Integer maxUses);

    /**
     * Sets whether the invite should only grant temporary membership. Default is {@code false}.
     *
     * @param  temporary
     *         Whether the invite should only grant temporary membership or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    InviteAction setTemporary(@Nullable Boolean temporary);

    /**
     * Sets whether fluxer should reuse a similar invite. Default is {@code false}.
     *
     * @param  unique
     *         Whether fluxer should reuse a similar invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    InviteAction setUnique(@Nullable Boolean unique);

    /**
     * Sets the id of the targeted application.
     * <br>The invite has to point to a voice channel.
     * The invite will have the {@link Invite.TargetType#EMBEDDED_APPLICATION} target.
     *
     * @param applicationId
     *        The id of the embedded application to target or {@code 0} to remove
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    InviteAction setTargetApplication(long applicationId);

    /**
     * Sets the id of the targeted application.
     * <br>The invite has to point to a voice channel.
     * The invite will have the {@link Invite.TargetType#EMBEDDED_APPLICATION} target.
     *
     * @param applicationId
     *        The id of the embedded application to target
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    default InviteAction setTargetApplication(@NotNull String applicationId) {
        return setTargetApplication(MiscUtil.parseSnowflake(applicationId));
    }

    /**
     * Sets the user whose stream to target for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param userId
     *        The id of the user whose stream to target or {@code 0} to remove.
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    InviteAction setTargetStream(long userId);

    /**
     * Sets the user whose stream to display for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param userId
     *        The id of the user whose stream to target.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    default InviteAction setTargetStream(@NotNull String userId) {
        return setTargetStream(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Sets the user whose stream to display for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param user
     *        The user whose stream to target.
     *
     * @throws IllegalArgumentException
     *         If the provided user is {@code null}
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    default InviteAction setTargetStream(@NotNull User user) {
        Checks.notNull(user, "User");
        return setTargetStream(user.getIdLong());
    }

    /**
     * Sets the user whose stream to display for this invite.
     * <br>The user must be streaming in the same channel.
     * The invite will have the {@link Invite.TargetType#STREAM} target.
     *
     * @param member
     *        The member whose stream to target.
     *
     * @throws IllegalArgumentException
     *         If the provided member is {@code null}
     *
     * @return The current InviteAction for chaining.
     */
    @NotNull
    @CheckReturnValue
    default InviteAction setTargetStream(@NotNull Member member) {
        Checks.notNull(member, "Member");
        return setTargetStream(member.getIdLong());
    }
}
