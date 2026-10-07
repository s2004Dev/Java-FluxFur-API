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

package lonter.jfa.internal.interactions.command;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.interactions.IntegrationType;
import lonter.jfa.api.interactions.InteractionContextType;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.DefaultMemberPermissions;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.api.interactions.commands.localization.LocalizationMap;
import lonter.jfa.api.interactions.commands.privileges.IntegrationPrivilege;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.CommandEditAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.requests.restaction.CommandEditActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.localization.LocalizationUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class CommandImpl implements Command {
    public static final EnumSet<OptionType> OPTIONS =
            EnumSet.complementOf(EnumSet.of(OptionType.SUB_COMMAND, OptionType.SUB_COMMAND_GROUP));
    public static final Predicate<DataObject> OPTION_TEST =
            it -> OPTIONS.contains(OptionType.fromKey(it.getInt("type")));
    public static final Predicate<DataObject> SUBCOMMAND_TEST =
            it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND;
    public static final Predicate<DataObject> GROUP_TEST =
            it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND_GROUP;

    private final JFAImpl api;
    private final Guild guild;
    private final String name, description;
    private final LocalizationMap nameLocalizations;
    private final LocalizationMap descriptionLocalizations;
    private final List<Command.Option> options;
    private final List<Command.SubcommandGroup> groups;
    private final List<Command.Subcommand> subcommands;
    private final long id, guildId, applicationId, version;
    private final boolean nsfw;
    private final Set<InteractionContextType> contexts;
    private final Set<IntegrationType> integrationTypes;
    private final Command.Type type;
    private final DefaultMemberPermissions defaultMemberPermissions;

    public CommandImpl(JFAImpl api, Guild guild, DataObject json) {
        this.api = api;
        this.guild = guild;
        this.name = json.getString("name");
        this.nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations");
        this.description = json.getString("description", "");
        this.descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations");
        this.type = Command.Type.fromId(json.getInt("type", 1));
        this.id = json.getUnsignedLong("id");
        this.guildId = guild != null ? guild.getIdLong() : 0L;
        this.applicationId =
                json.getUnsignedLong("application_id", api.getSelfUser().getApplicationIdLong());
        this.options = parseOptions(json, OPTION_TEST, Command.Option::new);
        this.groups = parseOptions(json, GROUP_TEST, (DataObject o) -> new SubcommandGroup(this, o));
        this.subcommands = parseOptions(json, SUBCOMMAND_TEST, (DataObject o) -> new Subcommand(this, o));
        this.version = json.getUnsignedLong("version", id);

        this.defaultMemberPermissions = json.isNull("default_member_permissions")
                ? DefaultMemberPermissions.ENABLED
                : DefaultMemberPermissions.enabledFor(json.getLong("default_member_permissions"));

        if (!json.isNull("contexts")) {
            this.contexts = json.getArray("contexts").stream(DataArray::getString)
                    .map(InteractionContextType::fromKey)
                    .collect(Helpers.toUnmodifiableEnumSet(InteractionContextType.class));
        }
        // If the command is in a guild, it can only be guild,
        // otherwise up to the dm_permission flag
        else if (guildId != 0L) {
            this.contexts = Helpers.unmodifiableEnumSet(InteractionContextType.GUILD);
        } else {
            boolean dmPermission = json.getBoolean("dm_permission", true);
            this.contexts = dmPermission
                    ? Helpers.unmodifiableEnumSet(InteractionContextType.GUILD, InteractionContextType.BOT_DM)
                    : Helpers.unmodifiableEnumSet(InteractionContextType.GUILD);
        }

        if (!json.isNull("integration_types")) {
            this.integrationTypes = json.getArray("integration_types").stream(DataArray::getString)
                    .map(IntegrationType::fromKey)
                    .collect(Helpers.toUnmodifiableEnumSet(IntegrationType.class));
        } else {
            this.integrationTypes = Helpers.unmodifiableEnumSet(IntegrationType.GUILD_INSTALL);
        }

        this.nsfw = json.getBoolean("nsfw");
    }

    public static <T> List<T> parseOptions(
            DataObject json, Predicate<DataObject> test, Function<DataObject, T> transform) {
        return json.optArray("options")
                .map(arr -> arr.stream(DataArray::getObject)
                        .filter(test)
                        .map(transform)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @NotNull
    @Override
    public RestAction<Void> delete() {
        checkSelfUser("Cannot delete a command from another bot!");
        Route.CompiledRoute route;
        String appId = getJFA().getSelfUser().getApplicationId();
        if (guildId != 0L) {
            route = Route.Interactions.DELETE_GUILD_COMMAND.compile(appId, Long.toUnsignedString(guildId), getId());
        } else {
            route = Route.Interactions.DELETE_COMMAND.compile(appId, getId());
        }
        return new RestActionImpl<>(api, route);
    }

    @NotNull
    @Override
    public CommandEditAction editCommand() {
        checkSelfUser("Cannot edit a command from another bot!");
        return guild == null
                ? new CommandEditActionImpl(api, type, getId())
                : new CommandEditActionImpl(guild, type, getId());
    }

    @NotNull
    @Override
    public RestAction<List<IntegrationPrivilege>> retrievePrivileges(@NotNull Guild guild) {
        checkSelfUser("Cannot retrieve privileges for a command from another bot!");
        Checks.notNull(guild, "Guild");
        return guild.retrieveIntegrationPrivilegesById(id);
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @NotNull
    @Override
    public Command.Type getType() {
        return type;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public LocalizationMap getNameLocalizations() {
        return nameLocalizations;
    }

    @NotNull
    @Override
    public String getFullCommandName() {
        return name;
    }

    @NotNull
    @Override
    public String getDescription() {
        return description;
    }

    @NotNull
    @Override
    public LocalizationMap getDescriptionLocalizations() {
        return descriptionLocalizations;
    }

    @NotNull
    @Override
    public List<Command.Option> getOptions() {
        return options;
    }

    @NotNull
    @Override
    public List<Command.Subcommand> getSubcommands() {
        return subcommands;
    }

    @NotNull
    @Override
    public List<Command.SubcommandGroup> getSubcommandGroups() {
        return groups;
    }

    @Override
    public long getApplicationIdLong() {
        return applicationId;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @NotNull
    @Override
    public DefaultMemberPermissions getDefaultPermissions() {
        return defaultMemberPermissions;
    }

    @NotNull
    @Override
    public EnumSet<InteractionContextType> getContexts() {
        return Helpers.copyEnumSet(InteractionContextType.class, contexts);
    }

    @NotNull
    @Override
    public EnumSet<IntegrationType> getIntegrationTypes() {
        return Helpers.copyEnumSet(IntegrationType.class, integrationTypes);
    }

    @Override
    public boolean isNSFW() {
        return nsfw;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @NotNull
    @Override
    public String getAsMention() {
        if (getType() != Type.SLASH) {
            throw new IllegalStateException("Only slash commands can be mentioned");
        }
        return Command.super.getAsMention();
    }

    @Override
    public String toString() {
        return new EntityString(this).setType(getType()).setName(getName()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Command)) {
            return false;
        }
        return id == ((Command) obj).getIdLong();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    private void checkSelfUser(String s) {
        if (applicationId != api.getSelfUser().getApplicationIdLong()) {
            throw new IllegalStateException(s);
        }
    }
}
