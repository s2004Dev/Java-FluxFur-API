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

package lonter.jfa.api.managers.channel.middleman;

import lonter.jfa.api.entities.channel.middleman.StandardGuildChannel;
import lonter.jfa.api.managers.channel.attribute.ICategorizableChannelManager;
import lonter.jfa.api.managers.channel.attribute.IPermissionContainerManager;
import lonter.jfa.api.managers.channel.attribute.IPositionableChannelManager;

/**
 * Manager providing functionality common for all {@link lonter.jfa.api.entities.channel.middleman.StandardGuildChannel StandardGuildChannels}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setName("help")
 *        .setParent(categoryChannel)
 *        .queue();
 * manager.reset(ChannelManager.PARENT | ChannelManager.NAME)
 *        .putPermissionOverride(member, 0, Permission.ALL_PERMISSIONS)
 *        .queue();
 * }
 *
 * @see StandardGuildChannel#getManager()
 */
public interface StandardGuildChannelManager<
                T extends StandardGuildChannel, M extends StandardGuildChannelManager<T, M>>
        extends IPermissionContainerManager<T, M>,
                IPositionableChannelManager<T, M>,
                ICategorizableChannelManager<T, M> {}
