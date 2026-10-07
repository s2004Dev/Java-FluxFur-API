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

import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.pagination.PinnedMessagePaginationAction;
import lonter.jfa.api.utils.TimeUtil;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.ReceivedMessage;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class PinnedMessagePaginationActionImpl
        extends PaginationActionImpl<PinnedMessagePaginationAction.PinnedMessage, PinnedMessagePaginationAction>
        implements PinnedMessagePaginationAction {
    protected final MessageChannel channel;

    public PinnedMessagePaginationActionImpl(MessageChannel channel) {
        super(channel.getJFA(), Route.Messages.GET_MESSAGE_PINS.compile(channel.getId()), 1, 50, 50);
        this.channel = channel;
    }

    @NotNull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders() {
        return EnumSet.of(PaginationOrder.BACKWARD);
    }

    @Override
    protected long getKey(PinnedMessage it) {
        OffsetDateTime timestamp = it.getTimePinned();
        long epochMillis = timestamp.toInstant().toEpochMilli();
        return TimeUtil.getFluxerTimestamp(epochMillis);
    }

    @NotNull
    @Override
    protected String getPaginationLastEvaluatedKey(long lastId, PinnedMessage last) {
        if (last == null) {
            return OffsetDateTime.now(ZoneOffset.UTC).toString();
        }
        return last.getTimePinned().toString();
    }

    @Override
    protected void handleSuccess(Response response, Request<List<PinnedMessage>> request) {
        DataObject object = response.getObject();
        DataArray items = object.getArray("items");
        EntityBuilder entityBuilder = api.getEntityBuilder();
        List<PinnedMessage> messages = new ArrayList<>(items.length());

        for (int i = 0; i < items.length(); i++) {
            try {
                DataObject item = items.getObject(i);
                ReceivedMessage message =
                        entityBuilder.createMessageWithChannel(item.getObject("message"), channel, false);
                OffsetDateTime pinnedAt = item.getOffsetDateTime("pinned_at");
                PinnedMessage pinnedMessage = new PinnedMessage(pinnedAt, message);

                messages.add(pinnedMessage);
                this.last = pinnedMessage;
                this.lastKey = getKey(last);
                if (useCache) {
                    this.cached.add(pinnedMessage);
                }
            } catch (Exception e) {
                EntityBuilder.LOG.error("Failed to parse pinned message", e);
            }
        }

        request.onSuccess(messages);
    }
}
