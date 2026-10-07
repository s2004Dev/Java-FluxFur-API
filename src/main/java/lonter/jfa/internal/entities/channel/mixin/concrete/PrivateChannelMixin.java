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

package lonter.jfa.internal.entities.channel.mixin.concrete;

import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.concrete.PrivateChannel;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.channel.mixin.middleman.MessageChannelMixin;
import lonter.jfa.internal.requests.CompletedRestAction;
import lonter.jfa.internal.requests.RestActionImpl;

import org.jetbrains.annotations.NotNull;

public interface PrivateChannelMixin<T extends PrivateChannelMixin<T>> extends PrivateChannel, MessageChannelMixin<T> {

    @NotNull
    @Override
    default String getName() {
        User user = getUser();
        if (user == null) {
            // don't break or override the contract of @NotNull
            return "";
        }
        return user.getName();
    }

    @NotNull
    @Override
    default RestAction<User> retrieveUser() {
        User user = getUser();
        if (user != null) {
            return new CompletedRestAction<>(getJFA(), user);
        }
        // even if the user blocks the bot, this does not fail.
        return retrievePrivateChannel().map(PrivateChannel::getUser);
    }

    @NotNull
    default RestAction<PrivateChannel> retrievePrivateChannel() {
        Route.CompiledRoute route = Route.Channels.GET_CHANNEL.compile(getId());
        return new RestActionImpl<>(getJFA(), route, (response, request) -> ((JFAImpl) getJFA())
                .getEntityBuilder()
                .createPrivateChannel(response.getObject()));
    }
}
