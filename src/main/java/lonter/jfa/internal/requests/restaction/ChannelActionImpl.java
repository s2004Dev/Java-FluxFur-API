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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lonter.jfa.api.Permission;
import lonter.jfa.api.Region;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;
import lonter.jfa.api.entities.channel.attribute.ISlowmodeChannel;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.concrete.ForumChannel;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.entities.channel.forums.BaseForumTag;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.middleman.StandardGuildMessageChannel;
import lonter.jfa.api.entities.emoji.CustomEmoji;
import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.entities.emoji.UnicodeEmoji;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.ChannelAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.utils.ChannelUtil;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChannelActionImpl<T extends GuildChannel> extends AuditableRestActionImpl<T> implements ChannelAction<T> {
    protected final TLongObjectMap<PermOverrideData> overrides = new TLongObjectHashMap<>();
    protected final Guild guild;
    protected final Class<T> clazz;
    protected final ChannelType type;

    // --all channels--
    protected String name;
    protected Category parent;
    protected Integer position;

    // --forum only--
    protected List<? extends BaseForumTag> availableTags;
    protected Emoji defaultReactionEmoji;

    // --text/forum/voice only--
    protected Integer slowmode = null;
    protected Integer defaultThreadSlowmode = null;

    // --text/forum/voice/news--
    protected String topic = null;
    protected Boolean nsfw = null;

    // --voice only--
    protected Integer userlimit = null;

    // --audio only--
    protected Integer bitrate = null;
    protected Region region = null;

    // --forum only--
    protected Integer defaultLayout = null;
    protected Integer defaultSortOrder = null;

    public ChannelActionImpl(Class<T> clazz, String name, Guild guild, ChannelType type) {
        super(guild.getJFA(), Route.Guilds.CREATE_CHANNEL.compile(guild.getId()));
        this.clazz = clazz;
        this.guild = guild;
        this.type = type;
        this.name = name;
    }

    @NotNull
    @Override
    public ChannelActionImpl<T> reason(@Nullable String reason) {
        return (ChannelActionImpl<T>) super.reason(reason);
    }

    @NotNull
    @Override
    public ChannelActionImpl<T> setCheck(BooleanSupplier checks) {
        return (ChannelActionImpl<T>) super.setCheck(checks);
    }

    @NotNull
    @Override
    public ChannelActionImpl<T> timeout(long timeout, @NotNull TimeUnit unit) {
        return (ChannelActionImpl<T>) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public ChannelActionImpl<T> deadline(long timestamp) {
        return (ChannelActionImpl<T>) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return type;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setName(@NotNull String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, Channel.MAX_NAME_LENGTH, "Name");
        this.name = name;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setParent(Category category) {
        if (category != null) {
            Checks.check(category.getGuild().equals(guild), "Category is not from same guild!");
            if (type == ChannelType.CATEGORY) {
                throw new UnsupportedOperationException("Cannot set a parent Category on a Category");
            }
        }

        this.parent = category;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setPosition(Integer position) {
        Checks.check(position == null || position >= 0, "Position must be >= 0!");
        this.position = position;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setTopic(String topic) {
        Checks.checkSupportedChannelTypes(ChannelUtil.TOPIC_SUPPORTED, type, "Topic");
        if (topic != null) {
            if (ChannelUtil.POST_CONTAINERS.contains(type)) {
                Checks.notLonger(topic, IPostContainer.MAX_POST_CONTAINER_TOPIC_LENGTH, "Topic");
            } else {
                Checks.notLonger(topic, StandardGuildMessageChannel.MAX_TOPIC_LENGTH, "Topic");
            }
        }
        this.topic = topic;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setNSFW(boolean nsfw) {
        Checks.checkSupportedChannelTypes(ChannelUtil.NSFW_SUPPORTED, type, "NSFW (age-restricted)");
        this.nsfw = nsfw;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setSlowmode(int slowmode) {
        Checks.checkSupportedChannelTypes(ChannelUtil.SLOWMODE_SUPPORTED, type, "Slowmode");
        Checks.check(
                slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0,
                "Slowmode must be between 0 and %d (seconds)!",
                ISlowmodeChannel.MAX_SLOWMODE);
        this.slowmode = slowmode;
        return this;
    }

    @NotNull
    @Override
    public ChannelAction<T> setDefaultThreadSlowmode(int slowmode) {
        Checks.checkSupportedChannelTypes(ChannelUtil.THREAD_CONTAINERS, type, "Default Thread Slowmode");
        Checks.check(
                slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0,
                "Slowmode must be between 0 and %d (seconds)!",
                ISlowmodeChannel.MAX_SLOWMODE);
        this.defaultThreadSlowmode = slowmode;
        return this;
    }

    @NotNull
    @Override
    public ChannelAction<T> setDefaultReaction(@Nullable Emoji emoji) {
        Checks.checkSupportedChannelTypes(ChannelUtil.POST_CONTAINERS, type, "Default Reaction");
        this.defaultReactionEmoji = emoji;
        return this;
    }

    @NotNull
    @Override
    public ChannelAction<T> setDefaultLayout(@NotNull ForumChannel.Layout layout) {
        Checks.checkSupportedChannelTypes(EnumSet.of(ChannelType.FORUM), type, "Default Layout");
        Checks.notNull(layout, "layout");
        Checks.check(layout != ForumChannel.Layout.UNKNOWN, "Layout type cannot be UNKNOWN.");
        this.defaultLayout = layout.getKey();
        return this;
    }

    @NotNull
    @Override
    public ChannelAction<T> setDefaultSortOrder(@NotNull IPostContainer.SortOrder sortOrder) {
        Checks.checkSupportedChannelTypes(ChannelUtil.POST_CONTAINERS, type, "Default Sort Order");
        Checks.notNull(sortOrder, "SortOrder");
        Checks.check(sortOrder != IPostContainer.SortOrder.UNKNOWN, "Sort Order cannot be UNKNOWN.");
        this.defaultSortOrder = sortOrder.getKey();
        return this;
    }

    @NotNull
    @Override
    public ChannelAction<T> setAvailableTags(@NotNull List<? extends BaseForumTag> tags) {
        Checks.checkSupportedChannelTypes(ChannelUtil.POST_CONTAINERS, type, "Available Tags");
        Checks.noneNull(tags, "Tags");
        this.availableTags = new ArrayList<>(tags);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> addMemberPermissionOverride(long userId, long allow, long deny) {
        return addOverride(userId, PermOverrideData.MEMBER_TYPE, allow, deny);
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> addRolePermissionOverride(long roleId, long allow, long deny) {
        return addOverride(roleId, PermOverrideData.ROLE_TYPE, allow, deny);
    }

    @NotNull
    @Override
    public ChannelAction<T> removePermissionOverride(long id) {
        overrides.remove(id);
        return this;
    }

    @NotNull
    @Override
    public ChannelAction<T> clearPermissionOverrides() {
        overrides.clear();
        return this;
    }

    @NotNull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ChannelAction<T> syncPermissionOverrides() {
        if (parent == null) {
            throw new IllegalStateException(
                    "Cannot sync overrides without parent category! Use setParent(category) first!");
        }
        clearPermissionOverrides();
        Member selfMember = getGuild().getSelfMember();
        boolean canSetRoles = selfMember.hasPermission(parent, Permission.MANAGE_ROLES);
        // You can only set MANAGE_ROLES if you have ADMINISTRATOR or MANAGE_PERMISSIONS as an
        // override on the channel
        // That is why we explicitly exclude it here!
        // This is by far the most complex and weird permission logic in the entire API...
        long botPerms =
                PermissionUtil.getEffectivePermission(selfMember) & ~Permission.MANAGE_PERMISSIONS.getRawValue();

        parent.getRolePermissionOverrides().forEach(override -> {
            long allow = override.getAllowedRaw();
            long deny = override.getDeniedRaw();
            if (!canSetRoles) {
                allow &= botPerms;
                deny &= botPerms;
            }
            addRolePermissionOverride(override.getIdLong(), allow, deny);
        });

        parent.getMemberPermissionOverrides().forEach(override -> {
            long allow = override.getAllowedRaw();
            long deny = override.getDeniedRaw();
            if (!canSetRoles) {
                allow &= botPerms;
                deny &= botPerms;
            }
            addMemberPermissionOverride(override.getIdLong(), allow, deny);
        });
        return this;
    }

    private ChannelActionImpl<T> addOverride(long targetId, int type, long allow, long deny) {
        Member selfMember = getGuild().getSelfMember();
        boolean canSetRoles = selfMember.hasPermission(Permission.ADMINISTRATOR);
        if (!canSetRoles && parent != null) {
            // You can also set MANAGE_ROLES if you have it on the category (apparently?)
            canSetRoles = selfMember.hasPermission(parent, Permission.MANAGE_ROLES);
        }
        if (!canSetRoles) {
            // Prevent permission escalation
            // You can only set MANAGE_ROLES if you have ADMINISTRATOR or MANAGE_PERMISSIONS as an
            // override on the channel
            // That is why we explicitly exclude it here!
            // This is by far the most complex and weird permission logic in the entire API...
            long botPerms =
                    PermissionUtil.getEffectivePermission(selfMember) & ~Permission.MANAGE_PERMISSIONS.getRawValue();

            EnumSet<Permission> missingPerms = Permission.getPermissions((allow | deny) & ~botPerms);
            if (!missingPerms.isEmpty()) {
                throw new InsufficientPermissionException(
                        guild,
                        Permission.MANAGE_PERMISSIONS,
                        "You must have Permission.MANAGE_PERMISSIONS on the channel explicitly in order to set permissions you don't already have!");
            }
        }

        overrides.put(targetId, new PermOverrideData(type, targetId, allow, deny));
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setBitrate(Integer bitrate) {
        if (!type.isAudio()) {
            throw new UnsupportedOperationException("Can only set the bitrate for an Audio Channel!");
        }
        if (bitrate != null) {
            int maxBitrate = getGuild().getMaxBitrate();
            if (bitrate < 8000) {
                throw new IllegalArgumentException("Bitrate must be greater than 8000.");
            } else if (bitrate > maxBitrate) {
                throw new IllegalArgumentException("Bitrate must be less than " + maxBitrate);
            }
        }

        this.bitrate = bitrate;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setUserlimit(Integer userlimit) {
        if (userlimit != null) {
            Checks.notNegative(userlimit, "Userlimit");
            if (type == ChannelType.VOICE) {
                Checks.check(
                        userlimit <= VoiceChannel.MAX_USERLIMIT,
                        "Userlimit may not be greater than %d for voice channels",
                        VoiceChannel.MAX_USERLIMIT);
            } else if (type == ChannelType.STAGE) {
                Checks.check(
                        userlimit <= StageChannel.MAX_USERLIMIT,
                        "Userlimit may not be greater than %d for stage channels",
                        StageChannel.MAX_USERLIMIT);
            } else {
                throw new IllegalStateException("Can only set userlimit on audio channels");
            }
        }
        this.userlimit = userlimit;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ChannelActionImpl<T> setRegion(@Nullable Region region) {
        if (!type.isAudio()) {
            throw new UnsupportedOperationException("Can only set the region for AudioChannels!");
        }
        this.region = region;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();

        // All channel types
        object.put("name", name);
        object.put("type", type.getId());
        object.put("permission_overwrites", DataArray.fromCollection(overrides.valueCollection()));
        if (position != null) {
            object.put("position", position);
        }
        if (parent != null) {
            object.put("parent_id", parent.getId());
        }

        // Text and Forum
        if (slowmode != null) {
            object.put("rate_limit_per_user", slowmode);
        }
        if (defaultThreadSlowmode != null) {
            object.put("default_thread_rate_limit_per_user", defaultThreadSlowmode);
        }

        // Text, Forum, and News
        if (topic != null && !topic.isEmpty()) {
            object.put("topic", topic);
        }
        if (nsfw != null) {
            object.put("nsfw", nsfw);
        }

        // Forum/Media only
        if (defaultReactionEmoji instanceof CustomEmoji) {
            object.put(
                    "default_reaction_emoji",
                    DataObject.empty().put("emoji_id", ((CustomEmoji) defaultReactionEmoji).getId()));
        } else if (defaultReactionEmoji instanceof UnicodeEmoji) {
            object.put("default_reaction_emoji", DataObject.empty().put("emoji_name", defaultReactionEmoji.getName()));
        }
        if (availableTags != null) {
            object.put("available_tags", DataArray.fromCollection(availableTags));
        }
        if (defaultSortOrder != null) {
            object.put("default_sort_order", defaultSortOrder);
        }

        // Forum only
        if (defaultLayout != null) {
            object.put("default_forum_layout", defaultLayout);
        }

        // Voice only
        if (userlimit != null) {
            object.put("user_limit", userlimit);
        }

        // Voice and Stage
        if (bitrate != null) {
            object.put("bitrate", bitrate);
        }
        if (region != null) {
            object.put("rtc_region", region.getKey());
        }

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<T> request) {
        EntityBuilder builder = api.getEntityBuilder();
        GuildChannel channel = builder.createGuildChannel((GuildImpl) guild, response.getObject());
        if (channel == null) {
            request.onFailure(new IllegalStateException("Created channel of unknown type!"));
        } else {
            request.onSuccess(clazz.cast(channel));
        }
    }
}
