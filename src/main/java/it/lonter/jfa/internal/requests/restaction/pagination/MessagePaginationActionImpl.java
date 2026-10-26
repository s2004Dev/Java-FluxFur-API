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

package it.lonter.jfa.internal.requests.restaction.pagination;

import it.lonter.jfa.api.Permission;
import it.lonter.jfa.api.entities.Member;
import it.lonter.jfa.api.entities.Message;
import it.lonter.jfa.api.entities.channel.middleman.GuildChannel;
import it.lonter.jfa.api.entities.channel.middleman.MessageChannel;
import it.lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import it.lonter.jfa.api.exceptions.InsufficientPermissionException;
import it.lonter.jfa.api.exceptions.ParsingException;
import it.lonter.jfa.api.requests.Request;
import it.lonter.jfa.api.requests.Response;
import it.lonter.jfa.api.requests.Route;
import it.lonter.jfa.api.requests.restaction.pagination.MessagePaginationAction;
import it.lonter.jfa.api.utils.data.DataArray;
import it.lonter.jfa.internal.entities.EntityBuilder;
import it.lonter.jfa.internal.utils.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class MessagePaginationActionImpl extends PaginationActionImpl<Message, MessagePaginationAction>
        implements MessagePaginationAction {
    private final MessageChannel channel;

    public MessagePaginationActionImpl(MessageChannel channel) {
        super(channel.getJFA(), Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()), 1, 100, 100);

        if (channel instanceof GuildChannel guildChannel) {
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
            last = messages.getLast();
            lastKey = last.getIdLong();
        }

        request.onSuccess(messages);
    }

    @Override
    protected long getKey(Message it) {
        return it.getIdLong();
    }
}
