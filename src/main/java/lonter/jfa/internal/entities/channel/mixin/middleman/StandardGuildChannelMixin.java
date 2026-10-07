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

package lonter.jfa.internal.entities.channel.mixin.middleman;

import lonter.jfa.api.entities.channel.middleman.StandardGuildChannel;
import lonter.jfa.internal.entities.channel.mixin.attribute.ICategorizableChannelMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInviteContainerMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.IPositionableChannelMixin;

public interface StandardGuildChannelMixin<T extends StandardGuildChannelMixin<T>>
        extends StandardGuildChannel,
                ICategorizableChannelMixin<T>,
                IPositionableChannelMixin<T>,
                IPermissionContainerMixin<T>,
                IInviteContainerMixin<T> {}
