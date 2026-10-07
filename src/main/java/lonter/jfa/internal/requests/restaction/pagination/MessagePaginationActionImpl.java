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

package lonter.jfa.internal.requests.restaction.pagination;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.exceptions.ParsingException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.pagination.MessagePaginationAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.utils.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class MessagePaginationActionImpl extends PaginationActionImpl<Message, MessagePaginationAction>
        implements MessagePaginationAction {
    private final MessageChannel channel;

    public MessagePaginationActionImpl(MessageChannel channel) {
        super(channel.getJFA(), Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()), 1, 100, 100);

        if (channel instanceof GuildChannel) {
            GuildChannel guildChannel = (GuildChannel) channel;
            Member selfMember = guildChannel.getGuild().getSelfMember();
            Checks.checkAccess(selfMember, guildChannel);
            if (!selfMember.hasPermission(guildChannel, Permission.MESSAGE_HISTORY)) {
                throw new InsufficientPermissionException(guildChannel, Permission.MESSAGE_HISTORY);
            }
        }

        this.channel = channel;
    }

    @NotNull
    @Override
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) channel;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Message>> request) {
        DataArray array = response.getArray();
        List<Message> messages = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++) {
            try {
                Message msg = builder.createMessageWithChannel(array.getObject(i), channel, false);
                messages.add(msg);
            } catch (ParsingException | NullPointerException e) {
                LOG.warn("Encountered an exception in MessagePagination", e);
            } catch (IllegalArgumentException e) {
                if (EntityBuilder.UNKNOWN_MESSAGE_TYPE.equals(e.getMessage())) {
                    LOG.warn("Skipping unknown message type during pagination", e);
                } else {
                    LOG.warn("Unexpected issue trying to parse message during pagination", e);
                }
            }
        }

        if (order == PaginationOrder.FORWARD) {
            Collections.reverse(messages);
        }
        if (useCache) {
            cached.addAll(messages);
        }

        if (!messages.isEmpty()) {
            last = messages.get(messages.size() - 1);
            lastKey = last.getIdLong();
        }

        request.onSuccess(messages);
    }

    @Override
    protected long getKey(Message it) {
        return it.getIdLong();
    }
}
