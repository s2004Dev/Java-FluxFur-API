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

package lonter.jfa.internal.requests.restaction;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.api.interactions.IntegrationType;
import lonter.jfa.api.interactions.InteractionContextType;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.DefaultMemberPermissions;
import lonter.jfa.api.interactions.commands.build.OptionData;
import lonter.jfa.api.interactions.commands.build.SubcommandData;
import lonter.jfa.api.interactions.commands.build.SubcommandGroupData;
import lonter.jfa.api.interactions.commands.localization.LocalizationFunction;
import lonter.jfa.api.interactions.commands.localization.LocalizationMap;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.CommandCreateAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.interactions.CommandDataImpl;
import lonter.jfa.internal.interactions.command.CommandImpl;
import lonter.jfa.internal.requests.RestActionImpl;
import okhttp3.RequestBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

public class CommandCreateActionImpl extends RestActionImpl<Command> implements CommandCreateAction {
    private final Guild guild;
    private final CommandDataImpl data;

    public CommandCreateActionImpl(JFAImpl api, CommandDataImpl command) {
        super(api, Route.Interactions.CREATE_COMMAND.compile(api.getSelfUser().getApplicationId()));
        this.guild = null;
        this.data = command;
    }

    public CommandCreateActionImpl(Guild guild, CommandDataImpl command) {
        super(
                guild.getJFA(),
                Route.Interactions.CREATE_GUILD_COMMAND.compile(
                        guild.getJFA().getSelfUser().getApplicationId(), guild.getId()));
        this.guild = guild;
        this.data = command;
    }

    @NotNull
    @Override
    public CommandCreateAction addCheck(@NotNull BooleanSupplier checks) {
        return (CommandCreateAction) super.addCheck(checks);
    }

    @NotNull
    @Override
    public CommandCreateAction setCheck(BooleanSupplier checks) {
        return (CommandCreateAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public CommandCreateAction deadline(long timestamp) {
        return (CommandCreateAction) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public CommandCreateAction setDefaultPermissions(@NotNull DefaultMemberPermissions permission) {
        data.setDefaultPermissions(permission);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setContexts(@NotNull Collection<InteractionContextType> contexts) {
        data.setContexts(contexts);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setIntegrationTypes(@NotNull Collection<IntegrationType> integrationTypes) {
        data.setIntegrationTypes(integrationTypes);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setNSFW(boolean nsfw) {
        data.setNSFW(nsfw);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setLocalizationFunction(@NotNull LocalizationFunction localizationFunction) {
        data.setLocalizationFunction(localizationFunction);
        return this;
    }

    @NotNull
    @Override
    public String getName() {
        return data.getName();
    }

    @NotNull
    @Override
    public LocalizationMap getNameLocalizations() {
        return data.getNameLocalizations();
    }

    @NotNull
    @Override
    public Command.Type getType() {
        return data.getType();
    }

    @NotNull
    @Override
    public DefaultMemberPermissions getDefaultPermissions() {
        return data.getDefaultPermissions();
    }

    @NotNull
    @Override
    public Set<InteractionContextType> getContexts() {
        return data.getContexts();
    }

    @NotNull
    @Override
    public Set<IntegrationType> getIntegrationTypes() {
        return data.getIntegrationTypes();
    }

    @Override
    public boolean isNSFW() {
        return data.isNSFW();
    }

    @NotNull
    @Override
    public CommandCreateAction timeout(long timeout, @NotNull TimeUnit unit) {
        return (CommandCreateAction) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public CommandCreateAction setName(@NotNull String name) {
        data.setName(name);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setNameLocalization(@NotNull FluxerLocale locale, @NotNull String name) {
        data.setNameLocalization(locale, name);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setNameLocalizations(@NotNull Map<FluxerLocale, String> map) {
        data.setNameLocalizations(map);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setDescription(@NotNull String description) {
        data.setDescription(description);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setDescriptionLocalization(@NotNull FluxerLocale locale, @NotNull String description) {
        data.setDescriptionLocalization(locale, description);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setDescriptionLocalizations(@NotNull Map<FluxerLocale, String> map) {
        data.setDescriptionLocalizations(map);
        return this;
    }

    @NotNull
    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @NotNull
    @Override
    public LocalizationMap getDescriptionLocalizations() {
        return data.getDescriptionLocalizations();
    }

    @Override
    public boolean removeOptions(@NotNull Predicate<? super OptionData> condition) {
        return data.removeOptions(condition);
    }

    @Override
    public boolean removeSubcommands(@NotNull Predicate<? super SubcommandData> condition) {
        return data.removeSubcommands(condition);
    }

    @Override
    public boolean removeSubcommandGroups(@NotNull Predicate<? super SubcommandGroupData> condition) {
        return data.removeSubcommandGroups(condition);
    }

    @NotNull
    @Override
    public List<SubcommandData> getSubcommands() {
        return data.getSubcommands();
    }

    @NotNull
    @Override
    public List<SubcommandGroupData> getSubcommandGroups() {
        return data.getSubcommandGroups();
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return data.getOptions();
    }

    @NotNull
    @Override
    public CommandCreateAction addOptions(@NotNull OptionData... options) {
        data.addOptions(options);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction addSubcommands(@NotNull SubcommandData... subcommand) {
        data.addSubcommands(subcommand);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction addSubcommandGroups(@NotNull SubcommandGroupData... group) {
        data.addSubcommandGroups(group);
        return this;
    }

    @Override
    public RequestBody finalizeData() {
        return getRequestBody(data.toData());
    }

    @Override
    protected void handleSuccess(Response response, Request<Command> request) {
        DataObject json = response.getObject();
        request.onSuccess(new CommandImpl(api, guild, json));
    }

    @NotNull
    @Override
    public DataObject toData() {
        return data.toData();
    }
}
