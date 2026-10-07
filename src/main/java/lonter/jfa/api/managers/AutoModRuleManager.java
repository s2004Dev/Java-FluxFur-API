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

package lonter.jfa.api.managers;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.automod.AutoModResponse;
import lonter.jfa.api.entities.automod.AutoModRule;
import lonter.jfa.api.entities.automod.build.TriggerConfig;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Manager providing functionality to update one or more fields for an {@link AutoModRule}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setName("Fluxer Invites")
 *        .setEnables(false)
 *        .queue();
 * manager.reset(AutoModRuleManager.NAME | AutoModRuleManager.ENABLED)
 *        .setName("Invites")
 *        .setEnabled(true)
 *        .queue();
 * }
 *
 * @see Guild#modifyAutoModRuleById(long)
 * @see Guild#modifyAutoModRuleById(String)
 * @see AutoModRule#getManager()
 */
public interface AutoModRuleManager extends Manager<AutoModRuleManager> {
    /** Used to reset the name field. */
    long NAME = 1;
    /** Used to reset the enabled field. */
    long ENABLED = 1 << 1;
    /** Used to reset the response field. */
    long RESPONSE = 1 << 2;
    /** Used to reset the exempt roles field. */
    long EXEMPT_ROLES = 1 << 3;
    /** Used to reset the exempt channels field. */
    long EXEMPT_CHANNELS = 1 << 4;
    /** Used to reset the trigger metadata field. */
    long TRIGGER_METADATA = 1 << 5;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(AutoModRuleManager.NAME | AutoModRuleManager.RESPONSE);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ENABLED}</li>
     *     <li>{@link #RESPONSE}</li>
     *     <li>{@link #EXEMPT_ROLES}</li>
     *     <li>{@link #EXEMPT_CHANNELS}</li>
     *     <li>{@link #TRIGGER_METADATA}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    AutoModRuleManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(AutoModRuleManager.NAME, AutoModRuleManager.RESPONSE);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ENABLED}</li>
     *     <li>{@link #RESPONSE}</li>
     *     <li>{@link #EXEMPT_ROLES}</li>
     *     <li>{@link #EXEMPT_CHANNELS}</li>
     *     <li>{@link #TRIGGER_METADATA}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    AutoModRuleManager reset(@NotNull long... fields);

    /**
     * Sets the <b><u>name</u></b> of the selected {@link AutoModRule}.
     *
     * <p>A rule name <b>must</b> be between 1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters long!
     *
     * @param  name
     *         The new name for the selected {@link AutoModRule}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters long
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    AutoModRuleManager setName(@NotNull String name);

    /**
     * Sets the <b><u>enabled</u></b> state of the selected {@link AutoModRule}.
     *
     * <p>When a rule is disabled, it will not be applied to any messages.
     *
     * @param  enabled
     *         True, if the selected {@link AutoModRule} should be enabled
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    AutoModRuleManager setEnabled(boolean enabled);

    //    @NotNull
    //    @CheckReturnValue
    //    AutoModRuleManager setEventType(@NotNull AutoModEventType type);

    /**
     * Sets what the rule should do upon triggering.
     *
     * <p>Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     *         The responses to configure
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} or {@link AutoModResponse.Type#UNKNOWN} is provided</li>
     *             <li>If the collection is empty</li>
     *         </ul>
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    AutoModRuleManager setResponses(@NotNull Collection<? extends AutoModResponse> responses);

    /**
     * Sets what the rule should do upon triggering.
     *
     * <p>Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     *         The responses to configure
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} or {@link AutoModResponse.Type#UNKNOWN} is provided</li>
     *             <li>If the collection is empty</li>
     *         </ul>
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default AutoModRuleManager setResponses(@NotNull AutoModResponse... responses) {
        Checks.noneNull(responses, "Responses");
        return setResponses(Arrays.asList(responses));
    }

    /**
     * Set which roles can bypass this rule.
     *
     * <p>Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     *         The roles to exempt (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    AutoModRuleManager setExemptRoles(@NotNull Collection<Role> roles);

    /**
     * Set which roles can bypass this rule.
     *
     * <p>Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     *         The roles to exempt (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default AutoModRuleManager setExemptRoles(@NotNull Role... roles) {
        Checks.noneNull(roles, "Roles");
        return setExemptRoles(Arrays.asList(roles));
    }

    /**
     * Set which channels can bypass this rule.
     *
     * <p>No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     *         The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    AutoModRuleManager setExemptChannels(@NotNull Collection<? extends GuildChannel> channels);

    /**
     * Set which channels can bypass this rule.
     *
     * <p>No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     *         The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default AutoModRuleManager setExemptChannels(@NotNull GuildChannel... channels) {
        Checks.noneNull(channels, "Channels");
        return setExemptChannels(Arrays.asList(channels));
    }

    /**
     * Change the {@link TriggerConfig} for this rule.
     *
     * @param  config
     *         The new config
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    AutoModRuleManager setTriggerConfig(@NotNull TriggerConfig config);
}
