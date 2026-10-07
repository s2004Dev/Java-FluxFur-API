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

package lonter.jfa.api.interactions.commands.build;

import lonter.jfa.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.api.interactions.IntegrationType;
import lonter.jfa.api.interactions.InteractionContextType;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.DefaultMemberPermissions;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.api.interactions.commands.localization.LocalizationFunction;
import lonter.jfa.api.interactions.commands.localization.LocalizationMap;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.interactions.CommandDataImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.localization.LocalizationUtils;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

/**
 * Extension of {@link CommandData} which allows setting slash-command specific settings such as options and subcommands.
 */
public interface SlashCommandData extends CommandData {
    @NotNull
    @Override
    SlashCommandData setLocalizationFunction(@NotNull LocalizationFunction localizationFunction);

    @NotNull
    @Override
    SlashCommandData setName(@NotNull String name);

    @NotNull
    @Override
    SlashCommandData setNameLocalization(@NotNull FluxerLocale locale, @NotNull String name);

    @NotNull
    @Override
    SlashCommandData setNameLocalizations(@NotNull Map<FluxerLocale, String> map);

    @NotNull
    @Override
    SlashCommandData setDefaultPermissions(@NotNull DefaultMemberPermissions permission);

    @NotNull
    @Override
    default SlashCommandData setContexts(@NotNull InteractionContextType... contexts) {
        return (SlashCommandData) CommandData.super.setContexts(contexts);
    }

    @NotNull
    @Override
    SlashCommandData setContexts(@NotNull Collection<InteractionContextType> contexts);

    @NotNull
    @Override
    default SlashCommandData setIntegrationTypes(@NotNull IntegrationType... integrationTypes) {
        return (SlashCommandData) CommandData.super.setIntegrationTypes(integrationTypes);
    }

    @NotNull
    @Override
    SlashCommandData setIntegrationTypes(@NotNull Collection<IntegrationType> integrationTypes);

    @NotNull
    @Override
    SlashCommandData setNSFW(boolean nsfw);

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The builder, for chaining
     */
    @NotNull
    SlashCommandData setDescription(@NotNull String description);

    /**
     * Sets a {@link FluxerLocale language-specific} localizations of this command's description.
     *
     * @param  locale
     *         The locale to associate the translated description with
     * @param  description
     *         The translated description to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the description is null</li>
     *             <li>If the locale is {@link FluxerLocale#UNKNOWN}</li>
     *             <li>If the description does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @NotNull
    SlashCommandData setDescriptionLocalization(@NotNull FluxerLocale locale, @NotNull String description);

    /**
     * Sets multiple {@link FluxerLocale language-specific} localizations of this command's description.
     *
     * @param  map
     *         The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link FluxerLocale#UNKNOWN} key</li>
     *             <li>If the map contains a description which does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @NotNull
    SlashCommandData setDescriptionLocalizations(@NotNull Map<FluxerLocale, String> map);

    /**
     * The configured description
     *
     * @return The description
     */
    @NotNull
    String getDescription();

    /**
     * The localizations of this command's description for {@link FluxerLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link FluxerLocale} to the localized description
     */
    @NotNull
    LocalizationMap getDescriptionLocalizations();

    /**
     * Removes all options that evaluate to {@code true} under the provided {@code condition}.
     * <br>This will not affect options within subcommands.
     * Use {@link SubcommandData#removeOptions(Predicate)} instead.
     *
     * <p><b>Example: Remove all options</b>
     * {@snippet lang="java":
     * command.removeOptions(option -> true);
     * }
     * <p><b>Example: Remove all options that are required</b>
     * {@snippet lang="java":
     * command.removeOptions(option -> option.isRequired());
     * }
     *
     * @param  condition
     *         The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     *         If the condition is null
     *
     * @return True, if any options were removed
     */
    boolean removeOptions(@NotNull Predicate<? super OptionData> condition);

    /**
     * Removes options by the provided name.
     * <br>This will not affect options within subcommands.
     * Use {@link SubcommandData#removeOptionByName(String)} instead.
     *
     * @param  name
     *         The <b>case-sensitive</b> option name
     *
     * @return True, if any options were removed
     */
    default boolean removeOptionByName(@NotNull String name) {
        return removeOptions(option -> option.getName().equals(name));
    }

    /**
     * Removes all subcommands that evaluate to {@code true} under the provided {@code condition}.
     * <br>This will not apply to subcommands within subcommand groups.
     * Use {@link SubcommandGroupData#removeSubcommand(Predicate)} instead.
     *
     * <p><b>Example: Remove all subcommands</b>
     * {@snippet lang="java":
     * command.removeSubcommands(subcommand -> true);
     * }
     *
     * @param  condition
     *         The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     *         If the condition is null
     *
     * @return True, if any subcommands were removed
     */
    boolean removeSubcommands(@NotNull Predicate<? super SubcommandData> condition);

    /**
     * Removes subcommands by the provided name.
     * <br>This will not apply to subcommands within subcommand groups.
     * Use {@link SubcommandGroupData#removeSubcommandByName(String)} instead.
     *
     * @param  name
     *         The <b>case-sensitive</b> subcommand name
     *
     * @return True, if any subcommands were removed
     */
    default boolean removeSubcommandByName(@NotNull String name) {
        return removeSubcommands(subcommand -> subcommand.getName().equals(name));
    }

    /**
     * Removes all subcommand groups that evaluate to {@code true} under the provided {@code condition}.
     *
     * <p><b>Example: Remove all subcommand groups</b>
     * {@snippet lang="java":
     * command.removeSubcommandGroups(group -> true);
     * }
     *
     * @param  condition
     *         The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     *         If the condition is null
     *
     * @return True, if any subcommand groups were removed
     */
    boolean removeSubcommandGroups(@NotNull Predicate<? super SubcommandGroupData> condition);

    /**
     * Removes subcommand groups by the provided name.
     *
     * @param  name
     *         The <b>case-sensitive</b> subcommand group name
     *
     * @return True, if any subcommand groups were removed
     */
    default boolean removeSubcommandGroupByName(@NotNull String name) {
        return removeSubcommandGroups(group -> group.getName().equals(name));
    }

    /**
     * The {@link SubcommandData Subcommands} in this command.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @NotNull
    @Unmodifiable
    List<SubcommandData> getSubcommands();

    /**
     * The {@link SubcommandGroupData Subcommand Groups} in this command.
     *
     * @return Immutable list of {@link SubcommandGroupData}
     */
    @NotNull
    @Unmodifiable
    List<SubcommandGroupData> getSubcommandGroups();

    /**
     * The options for this command.
     *
     * @return Immutable list of {@link OptionData}
     */
    @NotNull
    @Unmodifiable
    List<OptionData> getOptions();

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *          The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    SlashCommandData addOptions(@NotNull OptionData... options);

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    default SlashCommandData addOptions(@NotNull Collection<? extends OptionData> options) {
        Checks.noneNull(options, "Option");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     * @param  autoComplete
     *         Whether this option supports auto-complete via {@link CommandAutoCompleteInteractionEvent},
     *         only supported for option types which {@link OptionType#canSupportChoices() support choices}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#UNKNOWN UNKNOWN}.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If the provided option type does not support auto-complete</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    default SlashCommandData addOption(
            @NotNull OptionType type,
            @NotNull String name,
            @NotNull String description,
            boolean required,
            boolean autoComplete) {
        return addOptions(
                new OptionData(type, name, description).setRequired(required).setAutoComplete(autoComplete));
    }

    /**
     * Adds an option to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#UNKNOWN UNKNOWN}.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    default SlashCommandData addOption(
            @NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required) {
        return addOption(type, name, description, required, false);
    }

    /**
     * Adds an option to this command.
     * <br>The option is set to be non-required! You can use {@link #addOption(OptionType, String, String, boolean)} to add a required option instead.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     *         The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there already is a subcommand or subcommand group on this command (See {@link #addSubcommands(SubcommandData...)} for details).</li>
     *             <li>If the option type is {@link OptionType#UNKNOWN UNKNOWN}.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than {@value CommandData#MAX_OPTIONS} options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    default SlashCommandData addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description) {
        return addOption(type, name, description, false);
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandData Subcommands} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * {@snippet lang="java":
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     *         Also throws if you try adding subcommands when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    SlashCommandData addSubcommands(@NotNull SubcommandData... subcommands);

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandData Subcommands} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * {@snippet lang="java":
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     *         Also throws if you try adding subcommands when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    default SlashCommandData addSubcommands(@NotNull Collection<? extends SubcommandData> subcommands) {
        Checks.noneNull(subcommands, "Subcommands");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * {@snippet lang="java":
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommand groups, or duplicate group names are provided.
     *         Also throws if you try adding subcommand groups when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    SlashCommandData addSubcommandGroups(@NotNull SubcommandGroupData... groups);

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using {@link #addOptions(OptionData...)} and {@link #addSubcommands(SubcommandData...)} / {@link #addSubcommandGroups(SubcommandGroupData...)}
     * for the same command, is not supported.
     *
     * <p>Valid command layouts are as follows:
     * {@snippet lang="java":
     * command
     * |-- subcommand
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |__ subcommand group
     *     |__ subcommand
     *
     * command
     * |-- option
     * |__ option
     * }
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null, more than {@value CommandData#MAX_OPTIONS} subcommand groups, or duplicate group names are provided.
     *         Also throws if you try adding subcommand groups when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @NotNull
    default SlashCommandData addSubcommandGroups(@NotNull Collection<? extends SubcommandGroupData> groups) {
        Checks.noneNull(groups, "SubcommandGroups");
        return addSubcommandGroups(groups.toArray(new SubcommandGroupData[0]));
    }

    /**
     * Converts the provided {@link Command} into a SlashCommandData instance.
     *
     * @param  command
     *         The command to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the command has illegal configuration
     *
     * @return An instance of SlashCommandData
     */
    @NotNull
    static SlashCommandData fromCommand(@NotNull Command command) {
        Checks.notNull(command, "Command");
        if (command.getType() != Command.Type.SLASH) {
            throw new IllegalArgumentException(
                    "Cannot convert command of type " + command.getType() + " to SlashCommandData!");
        }

        CommandDataImpl data = new CommandDataImpl(command.getName(), command.getDescription());
        data.setContexts(command.getContexts());
        data.setIntegrationTypes(command.getIntegrationTypes());
        data.setNSFW(command.isNSFW());
        data.setDefaultPermissions(command.getDefaultPermissions());
        // Command localizations are unmodifiable, make a copy
        data.setNameLocalizations(command.getNameLocalizations().toMap());
        data.setDescriptionLocalizations(command.getDescriptionLocalizations().toMap());
        command.getOptions().stream().map(OptionData::fromOption).forEach(data::addOptions);
        command.getSubcommands().stream().map(SubcommandData::fromSubcommand).forEach(data::addSubcommands);
        command.getSubcommandGroups().stream()
                .map(SubcommandGroupData::fromGroup)
                .forEach(data::addSubcommandGroups);
        return data;
    }

    /**
     * Parses the provided serialization back into a SlashCommandData instance.
     * <br>This is the reverse function for {@link SlashCommandData#toData()}.
     *
     * @param  object
     *         The serialized {@link DataObject} representing the command
     *
     * @throws lonter.jfa.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed SlashCommandData instance, which can be further configured through setters
     *
     * @see    CommandData#fromData(DataObject)
     * @see    Commands#fromList(Collection)
     */
    @NotNull
    static SlashCommandData fromData(@NotNull DataObject object) {
        Checks.notNull(object, "DataObject");
        String name = object.getString("name");
        Command.Type commandType = Command.Type.fromId(object.getInt("type", 1));
        if (commandType != Command.Type.SLASH) {
            throw new IllegalArgumentException(
                    "Cannot convert command of type " + commandType + " to SlashCommandData!");
        }

        String description = object.getString("description");
        DataArray options = object.optArray("options").orElseGet(DataArray::empty);
        CommandDataImpl command = new CommandDataImpl(name, description);
        if (!object.isNull("contexts")) {
            command.setContexts(object.getArray("contexts").stream(DataArray::getString)
                    .map(InteractionContextType::fromKey)
                    .collect(Helpers.toUnmodifiableEnumSet(InteractionContextType.class)));
        } else {
            command.setContexts(
                    Helpers.unmodifiableEnumSet(InteractionContextType.GUILD, InteractionContextType.BOT_DM));
        }

        if (!object.isNull("integration_types")) {
            command.setIntegrationTypes(object.getArray("integration_types").stream(DataArray::getString)
                    .map(IntegrationType::fromKey)
                    .collect(Helpers.toUnmodifiableEnumSet(IntegrationType.class)));
        } else {
            command.setIntegrationTypes(Helpers.unmodifiableEnumSet(IntegrationType.GUILD_INSTALL));
        }

        command.setNSFW(object.getBoolean("nsfw"));

        command.setDefaultPermissions(
                object.isNull("default_member_permissions")
                        ? DefaultMemberPermissions.ENABLED
                        : DefaultMemberPermissions.enabledFor(object.getLong("default_member_permissions")));

        command.setNameLocalizations(LocalizationUtils.mapFromProperty(object, "name_localizations"));
        command.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(object, "description_localizations"));
        options.stream(DataArray::getObject).forEach(opt -> {
            OptionType type = OptionType.fromKey(opt.getInt("type"));
            switch (type) {
                case SUB_COMMAND:
                    command.addSubcommands(SubcommandData.fromData(opt));
                    break;
                case SUB_COMMAND_GROUP:
                    command.addSubcommandGroups(SubcommandGroupData.fromData(opt));
                    break;
                default:
                    command.addOptions(OptionData.fromData(opt));
            }
        });
        return command;
    }
}
