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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;
import lonter.jfa.api.entities.channel.attribute.ISlowmodeChannel;
import lonter.jfa.api.entities.channel.concrete.ForumChannel;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.channel.forums.ForumPost;
import lonter.jfa.api.entities.channel.forums.ForumTagSnowflake;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.ForumPostAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.messages.MessageCreateBuilder;
import lonter.jfa.api.utils.messages.MessageCreateData;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.ChannelUtil;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import java.util.Collection;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

public class ForumPostActionImpl extends RestActionImpl<ForumPost>
        implements ForumPostAction, MessageCreateBuilderMixin<ForumPostAction> {
    private final MessageCreateBuilder builder;
    private final IPostContainer channel;
    private final TLongSet appliedTags = new TLongHashSet();
    private String name;
    private ThreadChannel.AutoArchiveDuration autoArchiveDuration;
    protected Integer slowmode = null;

    public ForumPostActionImpl(IPostContainer channel, String name, MessageCreateBuilder builder) {
        super(channel.getJFA(), Route.Channels.CREATE_THREAD.compile(channel.getId()));
        this.builder = builder;
        this.channel = channel;
        setName(name);
    }

    @NotNull
    @Override
    public ForumPostAction setCheck(BooleanSupplier checks) {
        return (ForumPostAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public ForumPostAction addCheck(@NotNull BooleanSupplier checks) {
        return (ForumPostAction) super.addCheck(checks);
    }

    @NotNull
    @Override
    public ForumPostAction deadline(long timestamp) {
        return (ForumPostAction) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return channel.getGuild();
    }

    @NotNull
    @Override
    public IPostContainer getChannel() {
        return channel;
    }

    @NotNull
    @Override
    public ForumPostAction setTags(@NotNull Collection<? extends ForumTagSnowflake> tags) {
        Checks.noneNull(tags, "Tags");
        Checks.check(
                tags.size() <= ForumChannel.MAX_POST_TAGS, "Provided more than %d tags.", ForumChannel.MAX_POST_TAGS);
        Checks.check(
                !channel.isTagRequired() || !tags.isEmpty(),
                "This forum requires at least one tag per post! See ForumChannel#isRequireTag()");
        this.appliedTags.clear();
        tags.forEach(t -> this.appliedTags.add(t.getIdLong()));
        return this;
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.GUILD_PUBLIC_THREAD;
    }

    @NotNull
    @Override
    public ForumPostAction setName(@NotNull String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, Channel.MAX_NAME_LENGTH, "Name");
        this.name = name.trim();
        return this;
    }

    @NotNull
    @Override
    public ForumPostAction setAutoArchiveDuration(@NotNull ThreadChannel.AutoArchiveDuration autoArchiveDuration) {
        Checks.notNull(autoArchiveDuration, "AutoArchiveDuration");
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    @NotNull
    @Override
    public ForumPostAction setSlowmode(int slowmode) {
        Checks.checkSupportedChannelTypes(ChannelUtil.SLOWMODE_SUPPORTED, getType(), "slowmode");
        Checks.check(
                slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0,
                "Slowmode per user must be between 0 and %d (seconds)!",
                ISlowmodeChannel.MAX_SLOWMODE);
        if (!getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_THREADS)) {
            throw new InsufficientPermissionException(
                    channel,
                    Permission.MANAGE_THREADS,
                    "You must have Permission.MANAGE_THREADS on the parent forum channel to set a slowmode!");
        }
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public MessageCreateBuilder getBuilder() {
        return builder;
    }

    @Override
    protected RequestBody finalizeData() {
        try (MessageCreateData message = builder.build()) {
            DataObject json = DataObject.empty();
            json.put("message", message);
            json.put("name", name);
            if (autoArchiveDuration != null) {
                json.put("auto_archive_duration", autoArchiveDuration.getMinutes());
            }
            if (slowmode != null) {
                json.put("rate_limit_per_user", slowmode);
            }
            if (!appliedTags.isEmpty()) {
                json.put("applied_tags", appliedTags.toArray());
            } else if (getChannel().isTagRequired()) {
                throw new IllegalStateException(
                        "Cannot create posts without a tag in this forum. Apply at least one tag!");
            }
            return getMultipartBody(message.getAllDistinctFiles(), json);
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<ForumPost> request) {
        DataObject json = response.getObject();

        EntityBuilder entityBuilder = api.getEntityBuilder();

        ThreadChannel thread =
                entityBuilder.createThreadChannel(json, getGuild().getIdLong());
        Message message = entityBuilder.createMessageWithChannel(json.getObject("message"), thread, false);

        request.onSuccess(new ForumPost(message, thread));
    }
}
