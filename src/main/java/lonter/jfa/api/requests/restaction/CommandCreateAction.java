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

import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.api.interactions.IntegrationType;
import lonter.jfa.api.interactions.InteractionContextType;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.DefaultMemberPermissions;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.api.interactions.commands.build.OptionData;
import lonter.jfa.api.interactions.commands.build.SlashCommandData;
import lonter.jfa.api.interactions.commands.build.SubcommandData;
import lonter.jfa.api.interactions.commands.build.SubcommandGroupData;
import lonter.jfa.api.interactions.commands.localization.LocalizationFunction;
import lonter.jfa.api.requests.RestAction;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specialized {@link RestAction} used to create or update commands.
 * <br>If a command with the specified name already exists, it will be replaced!
 *
 * <p>This operation is <b>not</b> idempotent!
 * Commands will persist between restarts of your bot, you only have to create a command once.
 */
public interface CommandCreateAction extends RestAction<Command>, SlashCommandData {
    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction addCheck(@NotNull BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction timeout(long timeout, @NotNull TimeUnit unit);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction deadline(long timestamp);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setLocalizationFunction(@NotNull LocalizationFunction localizationFunction);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setName(@NotNull String name);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setNameLocalization(@NotNull FluxerLocale locale, @NotNull String name);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setNameLocalizations(@NotNull Map<FluxerLocale, String> map);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setDescription(@NotNull String description);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setDescriptionLocalization(@NotNull FluxerLocale locale, @NotNull String description);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setDescriptionLocalizations(@NotNull Map<FluxerLocale, String> map);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction addOptions(@NotNull OptionData... options);

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOptions(@NotNull Collection<? extends OptionData> options) {
        return (CommandCreateAction) SlashCommandData.super.addOptions(options);
    }

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOption(
            @NotNull OptionType type,
            @NotNull String name,
            @NotNull String description,
            boolean required,
            boolean autoComplete) {
        return (CommandCreateAction) SlashCommandData.super.addOption(type, name, description, required, autoComplete);
    }

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOption(
            @NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required) {
        return (CommandCreateAction) SlashCommandData.super.addOption(type, name, description, required);
    }

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description) {
        return (CommandCreateAction) SlashCommandData.super.addOption(type, name, description, false);
    }

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction addSubcommands(@NotNull SubcommandData... subcommands);

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction addSubcommands(@NotNull Collection<? extends SubcommandData> subcommands) {
        return (CommandCreateAction) SlashCommandData.super.addSubcommands(subcommands);
    }

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction addSubcommandGroups(@NotNull SubcommandGroupData... groups);

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction addSubcommandGroups(@NotNull Collection<? extends SubcommandGroupData> groups) {
        return (CommandCreateAction) SlashCommandData.super.addSubcommandGroups(groups);
    }

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setDefaultPermissions(@NotNull DefaultMemberPermissions permission);

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction setContexts(@NotNull InteractionContextType... contexts) {
        return (CommandCreateAction) SlashCommandData.super.setContexts(contexts);
    }

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setContexts(@NotNull Collection<InteractionContextType> contexts);

    @NotNull
    @Override
    @CheckReturnValue
    default CommandCreateAction setIntegrationTypes(@NotNull IntegrationType... integrationTypes) {
        return (CommandCreateAction) SlashCommandData.super.setIntegrationTypes(integrationTypes);
    }

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setIntegrationTypes(@NotNull Collection<IntegrationType> integrationTypes);

    @NotNull
    @Override
    @CheckReturnValue
    CommandCreateAction setNSFW(boolean nsfw);
}
