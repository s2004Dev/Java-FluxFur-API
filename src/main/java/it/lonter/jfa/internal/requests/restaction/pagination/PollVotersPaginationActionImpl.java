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

import it.lonter.jfa.api.JFA;
import it.lonter.jfa.api.entities.User;
import it.lonter.jfa.api.exceptions.ParsingException;
import it.lonter.jfa.api.requests.Request;
import it.lonter.jfa.api.requests.Response;
import it.lonter.jfa.api.requests.Route;
import it.lonter.jfa.api.requests.restaction.pagination.PollVotersPaginationAction;
import it.lonter.jfa.api.utils.data.DataArray;
import it.lonter.jfa.api.utils.data.DataObject;
import it.lonter.jfa.internal.entities.EntityBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class PollVotersPaginationActionImpl extends PaginationActionImpl<User, PollVotersPaginationAction>
        implements PollVotersPaginationAction {
    public PollVotersPaginationActionImpl(JFA jfa, String channelId, String messageId, long answerId) {
        super(
                jfa,
                Route.Messages.GET_POLL_ANSWER_VOTERS.compile(channelId, messageId, Long.toString(answerId)),
                1,
                100,
                100);
        this.order = PaginationOrder.FORWARD;
    }

    @NotNull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders() {
        return EnumSet.of(PaginationOrder.FORWARD);
    }

    @Override
    protected long getKey(User it) {
        return it.getIdLong();
    }

    @Override
    protected void handleSuccess(Response response, Request<List<User>> request) {
        DataArray array = response.getObject().getArray("users");
        List<User> users = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++) {
            try {
                DataObject object = array.getObject(i);
                users.add(builder.createUser(object));
            } catch (ParsingException | NullPointerException e) {
                LOG.warn("Encountered an exception in PollVotersPaginationAction", e);
            }
        }

        if (!users.isEmpty()) {
            if (useCache) {
                cached.addAll(users);
            }
            last = users.getLast();
            lastKey = last.getIdLong();
        }

        request.onSuccess(users);
    }
}
