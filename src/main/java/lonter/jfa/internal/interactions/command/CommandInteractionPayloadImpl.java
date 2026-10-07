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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lonter.jfa.api.entities.ISnowflake;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.CommandInteractionPayload;
import lonter.jfa.api.interactions.commands.OptionMapping;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.MemberImpl;
import lonter.jfa.internal.entities.UserImpl;
import lonter.jfa.internal.interactions.InteractionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandInteractionPayloadImpl extends InteractionImpl implements CommandInteractionPayload {
    private final long commandId;
    private final List<OptionMapping> options = new ArrayList<>();
    private final TLongObjectMap<Object> resolved = new TLongObjectHashMap<>();
    private final String name;
    private final boolean isGuildCommand;
    private String subcommand;
    private String group;
    private final Command.Type type;

    @SuppressWarnings("fallthrough")
    public CommandInteractionPayloadImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
        DataObject commandData = data.getObject("data");
        this.commandId = commandData.getUnsignedLong("id");
        this.name = commandData.getString("name");
        this.type = Command.Type.fromId(commandData.getInt("type", 1));
        // guild_id is always either null or the owner guild
        // (same as interaction guild_id)
        this.isGuildCommand = !commandData.isNull("guild_id");

        DataArray options = commandData.optArray("options").orElseGet(DataArray::empty);
        DataObject resolveJson = commandData.optObject("resolved").orElseGet(DataObject::empty);

        if (options.length() == 1) {
            DataObject option = options.getObject(0);
            switch (OptionType.fromKey(option.getInt("type"))) {
                case SUB_COMMAND_GROUP:
                    group = option.getString("name");
                    options = option.getArray("options");
                    option = options.getObject(0);
                case SUB_COMMAND:
                    subcommand = option.getString("name");
                    options = option.optArray("options").orElseGet(DataArray::empty); // Flatten options
                    break;
            }
        }

        parseResolved(jfa, resolveJson);
        parseOptions(options);
    }

    private void parseOptions(DataArray options) {
        options.stream(DataArray::getObject)
                .map(json -> new OptionMapping(json, resolved, getJFA(), getGuild()))
                .forEach(this.options::add);
    }

    private void parseResolved(JFAImpl jfa, DataObject resolveJson) {
        EntityBuilder entityBuilder = jfa.getEntityBuilder();

        resolveJson.optObject("users").ifPresent(users -> users.keys().forEach(userId -> {
            DataObject userJson = users.getObject(userId);
            UserImpl userArg = entityBuilder.createUser(userJson);
            resolved.put(userArg.getIdLong(), userArg);
        }));

        resolveJson
                .optObject("attachments")
                .ifPresent(attachments -> attachments.keys().forEach(id -> {
                    DataObject json = attachments.getObject(id);
                    Message.Attachment file = entityBuilder.createMessageAttachment(json);
                    resolved.put(file.getIdLong(), file);
                }));

        if (this.guild != null) {
            resolveJson.optObject("members").ifPresent(members -> {
                DataObject users = resolveJson.getObject("users");
                members.keys().forEach(memberId -> {
                    DataObject memberJson = members.getObject(memberId);
                    memberJson.put("user", users.getObject(memberId)); // Add user json as well for parsing
                    Member optionMember = interactionEntityBuilder.createMember(guild, memberJson);
                    if (member instanceof MemberImpl) {
                        entityBuilder.updateMemberCache((MemberImpl) optionMember);
                    }
                    resolved.put(optionMember.getIdLong(), optionMember); // This basically upgrades user to member
                });
            });
            resolveJson.optObject("roles").ifPresent(roles -> {
                roles.keys().stream()
                        .map(roleId -> {
                            if (!guild.isDetached()) {
                                return guild.getRoleById(roleId);
                            }
                            return interactionEntityBuilder.createRole(guild, roles.getObject(roleId));
                        })
                        .filter(Objects::nonNull)
                        .forEach(role -> resolved.put(role.getIdLong(), role));
            });
            resolveJson.optObject("channels").ifPresent(channels -> channels.keys()
                    .forEach(id -> {
                        ISnowflake channelObj = jfa.getGuildChannelById(id);
                        DataObject channelJson = channels.getObject(id);
                        if (channelObj != null) {
                            resolved.put(channelObj.getIdLong(), channelObj);
                        } else if (ChannelType.fromId(channelJson.getInt("type"))
                                .isThread()) {
                            resolved.put(
                                    Long.parseUnsignedLong(id),
                                    interactionEntityBuilder.createThreadChannel(guild, channelJson));
                        } else {
                            resolved.put(
                                    Long.parseUnsignedLong(id),
                                    interactionEntityBuilder.createGuildChannel(guild, channelJson));
                        }
                    }));
        }
    }

    @Nullable
    @Override
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) super.getChannel();
    }

    @NotNull
    @Override
    public Command.Type getCommandType() {
        return type;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubcommandName() {
        return subcommand;
    }

    @Override
    public String getSubcommandGroup() {
        return group;
    }

    @Override
    public long getCommandIdLong() {
        return commandId;
    }

    @Override
    public boolean isGuildCommand() {
        return isGuildCommand;
    }

    @NotNull
    @Override
    public List<OptionMapping> getOptions() {
        return options;
    }
}
