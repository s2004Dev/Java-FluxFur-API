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

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.ISlowmodeChannel;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.ThreadChannelAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.ChannelUtil;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public class ThreadChannelActionImpl extends AuditableRestActionImpl<ThreadChannel> implements ThreadChannelAction {
    protected final Guild guild;
    protected final GuildChannel channel;
    protected final ChannelType type;
    protected final String parentMessageId;

    protected String name;
    protected ThreadChannel.AutoArchiveDuration autoArchiveDuration = null;
    protected Integer slowmode = null;
    protected Boolean invitable = null;

    public ThreadChannelActionImpl(GuildChannel channel, String name, ChannelType type) {
        super(channel.getJFA(), Route.Channels.CREATE_THREAD.compile(channel.getId()));
        this.guild = channel.getGuild();
        this.channel = channel;
        this.type = type;
        this.parentMessageId = null;

        this.name = name;
    }

    public ThreadChannelActionImpl(GuildChannel channel, String name, String parentMessageId) {
        super(channel.getJFA(), Route.Channels.CREATE_THREAD_FROM_MESSAGE.compile(channel.getId(), parentMessageId));
        this.guild = channel.getGuild();
        this.channel = channel;
        this.type =
                channel.getType() == ChannelType.TEXT ? ChannelType.GUILD_PUBLIC_THREAD : ChannelType.GUILD_NEWS_THREAD;
        this.parentMessageId = parentMessageId;

        this.name = name;
    }

    @NotNull
    @Override
    public ThreadChannelActionImpl reason(String reason) {
        return (ThreadChannelActionImpl) super.reason(reason);
    }

    @NotNull
    @Override
    public ThreadChannelActionImpl setCheck(BooleanSupplier checks) {
        return (ThreadChannelActionImpl) super.setCheck(checks);
    }

    @NotNull
    @Override
    public ThreadChannelActionImpl timeout(long timeout, @NotNull TimeUnit unit) {
        return (ThreadChannelActionImpl) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public ThreadChannelActionImpl deadline(long timestamp) {
        return (ThreadChannelActionImpl) super.deadline(timestamp);
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
    public ThreadChannelActionImpl setName(@NotNull String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, Channel.MAX_NAME_LENGTH, "Name");
        this.name = name;
        return this;
    }

    @NotNull
    @Override
    public ThreadChannelAction setAutoArchiveDuration(@NotNull ThreadChannel.AutoArchiveDuration autoArchiveDuration) {
        Checks.notNull(autoArchiveDuration, "autoArchiveDuration");
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    @NotNull
    @Override
    public ThreadChannelAction setSlowmode(int slowmode) {
        Checks.checkSupportedChannelTypes(ChannelUtil.SLOWMODE_SUPPORTED, type, "slowmode");
        Checks.check(
                slowmode <= ISlowmodeChannel.MAX_SLOWMODE && slowmode >= 0,
                "Slowmode per user must be between 0 and %d (seconds)!",
                ISlowmodeChannel.MAX_SLOWMODE);
        if (!getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_THREADS)) {
            throw new InsufficientPermissionException(
                    channel,
                    Permission.MANAGE_THREADS,
                    "You must have Permission.MANAGE_THREADS on the parent channel to set a slowmode!");
        }
        this.slowmode = slowmode;
        return this;
    }

    @NotNull
    @Override
    public ThreadChannelAction setInvitable(boolean invitable) {
        if (type != ChannelType.GUILD_PRIVATE_THREAD) {
            throw new UnsupportedOperationException("Can only set invitable on private threads");
        }

        this.invitable = invitable;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();

        object.put("name", name);

        // The type is selected by fluxer itself if we are using a parent message,
        // so don't send it.
        if (parentMessageId == null) {
            object.put("type", type.getId());
        }

        if (autoArchiveDuration != null) {
            object.put("auto_archive_duration", autoArchiveDuration.getMinutes());
        }
        if (slowmode != null) {
            object.put("rate_limit_per_user", slowmode);
        }
        if (invitable != null) {
            object.put("invitable", invitable);
        }

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<ThreadChannel> request) {
        ThreadChannel channel = api.getEntityBuilder().createThreadChannel(response.getObject(), guild.getIdLong());
        request.onSuccess(channel);
    }
}
