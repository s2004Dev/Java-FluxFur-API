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

package lonter.jfa.internal.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.emoji.CustomEmoji;
import lonter.jfa.api.interactions.commands.SlashCommandReference;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.BagUtils;
import org.apache.commons.collections4.bag.HashBag;

import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectMenuMentions implements Mentions {
    private final DataObject resolved;
    private final JFAImpl jfa;
    private final InteractionEntityBuilder interactionEntityBuilder;
    private final Guild guild;
    private final List<String> values;

    private List<User> cachedUsers;
    private List<Member> cachedMembers;
    private List<Role> cachedRoles;
    private List<GuildChannel> cachedChannels;

    public SelectMenuMentions(
            JFAImpl jfa,
            InteractionEntityBuilder interactionEntityBuilder,
            @Nullable Guild guild,
            DataObject resolved,
            DataArray values) {
        this.jfa = jfa;
        this.interactionEntityBuilder = interactionEntityBuilder;
        this.guild = guild;
        this.resolved = resolved;
        this.values = values.stream(DataArray::getString).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return jfa;
    }

    @Override
    public boolean mentionsEveryone() {
        return false;
    }

    @NotNull
    @Override
    public List<User> getUsers() {
        if (cachedUsers != null) {
            return cachedUsers;
        }

        DataObject userMap = resolved.optObject("users").orElseGet(DataObject::empty);
        EntityBuilder builder = jfa.getEntityBuilder();

        return cachedUsers = values.stream()
                .map(id -> userMap.optObject(id).orElse(null))
                .filter(Objects::nonNull)
                .map(builder::createUser)
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public Bag<User> getUsersBag() {
        return new HashBag<>(getUsers());
    }

    @NotNull
    @Override
    public List<GuildChannel> getChannels() {
        if (guild == null) {
            return Collections.emptyList();
        }
        if (cachedChannels != null) {
            return cachedChannels;
        }

        DataObject channelMap = resolved.optObject("channels").orElseGet(DataObject::empty);

        return cachedChannels = values.stream()
                .map(id -> channelMap.optObject(id).orElse(null))
                .filter(Objects::nonNull)
                .map(json -> {
                    ChannelType channelType = ChannelType.fromId(json.getInt("type", -1));
                    if (!guild.isDetached()) {
                        return guild.getGuildChannelById(channelType, json.getUnsignedLong("id"));
                    }

                    // Unknown guilds
                    if (channelType.isThread()) {
                        return interactionEntityBuilder.createThreadChannel(guild, json);
                    }
                    // Will return null if the type isn't known
                    return interactionEntityBuilder.createGuildChannel(guild, json);
                })
                .filter(Objects::nonNull)
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public Bag<GuildChannel> getChannelsBag() {
        return new HashBag<>(getChannels());
    }

    @NotNull
    @Override
    public <T extends GuildChannel> List<T> getChannels(@NotNull Class<T> clazz) {
        return getChannels().stream().filter(clazz::isInstance).map(clazz::cast).collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public <T extends GuildChannel> Bag<T> getChannelsBag(@NotNull Class<T> clazz) {
        return new HashBag<>(getChannels(clazz));
    }

    @NotNull
    @Override
    public List<Role> getRoles() {
        if (guild == null) {
            return Collections.emptyList();
        }
        if (cachedRoles != null) {
            return cachedRoles;
        }

        DataObject roleMap = resolved.optObject("roles").orElseGet(DataObject::empty);

        return cachedRoles = values.stream()
                .filter(roleMap::hasKey)
                .map(roleMap::getObject)
                .map(json -> {
                    if (!guild.isDetached()) {
                        return guild.getRoleById(json.getUnsignedLong("id"));
                    }
                    return interactionEntityBuilder.createRole(guild, json);
                })
                .filter(Objects::nonNull)
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public Bag<Role> getRolesBag() {
        return new HashBag<>(getRoles());
    }

    @NotNull
    @Override
    public List<CustomEmoji> getCustomEmojis() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Bag<CustomEmoji> getCustomEmojisBag() {
        return BagUtils.emptyBag();
    }

    @NotNull
    @Override
    public List<SlashCommandReference> getSlashCommands() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Bag<SlashCommandReference> getSlashCommandsBag() {
        return BagUtils.emptyBag();
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        if (guild == null) {
            return Collections.emptyList();
        }
        if (cachedMembers != null) {
            return cachedMembers;
        }

        DataObject memberMap = resolved.optObject("members").orElseGet(DataObject::empty);
        DataObject userMap = resolved.optObject("users").orElseGet(DataObject::empty);

        return cachedMembers = values.stream()
                .map(id -> memberMap.optObject(id).map(m -> m.put("id", id)).orElse(null))
                .filter(Objects::nonNull)
                .map(json -> json.put("user", userMap.getObject(json.getString("id"))))
                .map(json -> interactionEntityBuilder.createMember(guild, json))
                .filter(Objects::nonNull)
                .filter(member -> {
                    if (!member.isDetached()) {
                        jfa.getEntityBuilder().updateMemberCache((MemberImpl) member);
                    }
                    return true;
                })
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public Bag<Member> getMembersBag() {
        return new HashBag<>(getMembers());
    }

    @NotNull
    @Override
    public List<IMentionable> getMentions(@NotNull Message.MentionType... types) {
        if (types.length == 0) {
            return getMentions(Message.MentionType.values());
        }
        List<IMentionable> mentions = new ArrayList<>();
        // Convert to set to avoid duplicates
        EnumSet<Message.MentionType> set = EnumSet.of(types[0], types);
        for (Message.MentionType type : set) {
            switch (type) {
                case USER:
                    List<Member> members = getMembers();
                    List<User> users = getUsers();
                    mentions.addAll(members);
                    users.stream()
                            .filter(u -> members.stream().noneMatch(m -> m.getIdLong() == u.getIdLong()))
                            .forEach(mentions::add);
                    break;
                case ROLE:
                    mentions.addAll(getRoles());
                    break;
                case CHANNEL:
                    mentions.addAll(getChannels());
                    break;
            }
        }

        mentions.sort(Comparator.comparingInt(it -> values.indexOf(it.getId())));
        return Collections.unmodifiableList(mentions);
    }

    @Override
    public boolean isMentioned(@NotNull IMentionable mentionable, @NotNull Message.MentionType... types) {
        Checks.notNull(types, "Mention Types");
        if (types.length == 0) {
            return isMentioned(mentionable, Message.MentionType.values());
        }

        String id = mentionable.getId();
        for (Message.MentionType type : types) {
            switch (type) {
                case USER:
                    if (mentionable instanceof UserSnowflake) {
                        boolean mentioned = resolved.optObject("users")
                                .map(obj -> obj.hasKey(id))
                                .orElse(false);
                        if (mentioned) {
                            return true;
                        }
                    }
                    break;
                case ROLE:
                    if (mentionable instanceof Member) {
                        boolean mentioned = ((Member) mentionable)
                                .getUnsortedRoles().stream()
                                        .anyMatch(role -> isMentioned(role, Message.MentionType.ROLE));
                        if (mentioned) {
                            return true;
                        }
                    } else if (mentionable instanceof User) {
                        boolean mentioned = getMembers().stream()
                                .filter(it -> it.getIdLong() == mentionable.getIdLong())
                                .findFirst()
                                .map(member -> isMentioned(member, Message.MentionType.ROLE))
                                .orElse(false);
                        if (mentioned) {
                            return true;
                        }
                    } else if (mentionable instanceof Role) {
                        boolean mentioned = resolved.optObject("roles")
                                .map(obj -> obj.hasKey(id))
                                .orElse(false);
                        if (mentioned) {
                            return true;
                        }
                    }
                    break;
                case CHANNEL:
                    if (mentionable instanceof GuildChannel && getChannels().contains(mentionable)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
}
