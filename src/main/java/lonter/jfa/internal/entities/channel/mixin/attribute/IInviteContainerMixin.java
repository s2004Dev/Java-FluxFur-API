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

package lonter.jfa.internal.entities.channel.mixin.attribute;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Invite;
import lonter.jfa.api.entities.channel.attribute.IInviteContainer;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.InviteAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.requests.restaction.InviteActionImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface IInviteContainerMixin<T extends IInviteContainerMixin<T>>
        extends IInviteContainer, GuildChannelMixin<T> {
    // ---- Default implementations of interface ----
    @NotNull
    @Override
    default InviteAction createInvite() {
        checkAttached();
        checkPermission(Permission.CREATE_INSTANT_INVITE);

        return new InviteActionImpl(this.getJFA(), this.getId());
    }

    @NotNull
    @Override
    default RestAction<List<Invite>> retrieveInvites() {
        checkAttached();
        checkPermission(Permission.MANAGE_CHANNEL);

        Route.CompiledRoute route = Route.Invites.GET_CHANNEL_INVITES.compile(getId());

        JFAImpl jfa = (JFAImpl) getJFA();
        return new RestActionImpl<>(jfa, route, (response, request) -> {
            EntityBuilder entityBuilder = jfa.getEntityBuilder();
            DataArray array = response.getArray();
            List<Invite> invites = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                invites.add(entityBuilder.createInvite(array.getObject(i)));
            }
            return Collections.unmodifiableList(invites);
        });
    }
}
