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

import lonter.jfa.api.interactions.IntegrationType;
import lonter.jfa.api.interactions.InteractionContextType;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.DefaultMemberPermissions;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.api.interactions.commands.build.*;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specialized {@link RestAction} used to edit an existing command.
 */
public interface CommandEditAction extends RestAction<Command> {
    @NotNull
    @Override
    @CheckReturnValue
    CommandEditAction setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    CommandEditAction addCheck(@NotNull BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    CommandEditAction timeout(long timeout, @NotNull TimeUnit unit);

    @NotNull
    @Override
    @CheckReturnValue
    CommandEditAction deadline(long timestamp);

    /**
     * Replace the command with the provided {@link CommandData}.
     *
     * @param  commandData
     *         The data for the command
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The CommandEditAction instance, for chaining
     *
     * @see    Commands
     * @see    CommandCreateAction
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction apply(@NotNull CommandData commandData);

    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters. Use null to keep the current name.
     *
     * @throws IllegalArgumentException
     *         If the name is not alphanumeric or not between 1-32 characters
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction setName(@Nullable String name);

    /**
     * Sets the contexts in which this command can be executed (Default: Guild and Bot DMs).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  contexts
     *         The contexts in which this command can be executed
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no interaction context types were passed
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction setContexts(@NotNull InteractionContextType... contexts) {
        return setContexts(Arrays.asList(contexts));
    }

    /**
     * Sets the contexts in which this command can be executed (Default: Guild and Bot DMs).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  contexts
     *         The contexts in which this command can be executed
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no interaction context types were passed
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction setContexts(@NotNull Collection<InteractionContextType> contexts);

    /**
     * Sets the integration types on which this command can be installed on (Default: Guilds).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  integrationTypes
     *         The integration types on which this command can be installed on
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no integration types were passed
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction setIntegrationTypes(@NotNull IntegrationType... integrationTypes) {
        return setIntegrationTypes(Arrays.asList(integrationTypes));
    }

    /**
     * Sets the integration types on which this command can be installed on (Default: Guilds).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  integrationTypes
     *         The integration types on which this command can be installed on
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no integration types were passed
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction setIntegrationTypes(@NotNull Collection<IntegrationType> integrationTypes);

    /**
     * Sets whether this command should only be usable in NSFW (age-restricted) channels.
     * <br>Default: false
     *
     * <p>Note: Age-restricted commands will not show up in direct messages by default unless the user enables them in their settings.
     *
     * @param  nsfw
     *         True, to make this command nsfw
     *
     * @return The CommandEditAction instance, for chaining
     *
     * @see <a href="https://support.fluxer.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction setNSFW(boolean nsfw);

    /**
     * Sets the {@link lonter.jfa.api.Permission Permissions} that a user must have in a specific channel to be able to use this command.
     * <br>By default, everyone can use this command ({@link DefaultMemberPermissions#ENABLED}). Additionally, a command can be disabled for everyone but admins via {@link DefaultMemberPermissions#DISABLED}.
     * <p>These configurations can be overwritten by moderators in each guild. See {@link Command#retrievePrivileges(lonter.jfa.api.entities.Guild)} to get moderator defined overrides.
     *
     * @param  permission
     *         {@link DefaultMemberPermissions} representing the default permissions of this command.
     *
     * @return The CommandEditAction instance, for chaining
     *
     * @see DefaultMemberPermissions#ENABLED
     * @see DefaultMemberPermissions#DISABLED
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction setDefaultPermissions(@NotNull DefaultMemberPermissions permission);

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-100 characters. Use null to keep the current description.
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-100 characters
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction setDescription(@Nullable String description);

    /**
     * Removes all existing options/subcommands/groups from this command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction clearOptions();

    /**
     * Adds up to 25 options to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction addOptions(@NotNull OptionData... options);

    /**
     * Adds up to 25 options to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction addOptions(@NotNull Collection<? extends OptionData> options) {
        Checks.noneNull(options, "Options");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction addOption(
            @NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required) {
        return addOptions(new OptionData(type, name, description).setRequired(required));
    }

    /**
     * Adds an option to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description) {
        return addOption(type, name, description, false);
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction addSubcommands(@NotNull SubcommandData... subcommands);

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction addSubcommands(@NotNull Collection<? extends SubcommandData> subcommands) {
        Checks.noneNull(subcommands, "Subcommands");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    CommandEditAction addSubcommandGroups(@NotNull SubcommandGroupData... groups);

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @NotNull
    @CheckReturnValue
    default CommandEditAction addSubcommandGroups(@NotNull Collection<? extends SubcommandGroupData> groups) {
        Checks.noneNull(groups, "SubcommandGroups");
        return addSubcommandGroups(groups.toArray(new SubcommandGroupData[0]));
    }
}
