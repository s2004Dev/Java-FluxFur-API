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

package lonter.jfa.internal.entities.channel.mixin;

import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.unions.ChannelUnion;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.internal.entities.detached.mixin.IDetachableEntityMixin;
import lonter.jfa.internal.requests.RestActionImpl;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public interface ChannelMixin<T extends ChannelMixin<T>> extends Channel, ChannelUnion, IDetachableEntityMixin {
    // ---- Default implementations of interface ----
    @Override
    @NotNull
    @CheckReturnValue
    default RestAction<Void> delete() {
        checkCanAccess();
        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(getId());
        return new RestActionImpl<>(getJFA(), route);
    }

    // ---- State Accessors ----
    T setName(String name);

    // ---- Hooks ----
    void checkCanAccess();
}
