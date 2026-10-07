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

package lonter.jfa.api.managers.channel.concrete;

import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.managers.channel.attribute.IAgeRestrictedChannelManager;
import lonter.jfa.api.managers.channel.attribute.ISlowmodeChannelManager;
import lonter.jfa.api.managers.channel.middleman.AudioChannelManager;
import lonter.jfa.api.managers.channel.middleman.StandardGuildChannelManager;

/**
 * Manager providing methods to modify a {@link StageChannel}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setName("School Presentations")
 *        .setBitrate(96000)
 *        .queue();
 * }
 */
public interface StageChannelManager
        extends AudioChannelManager<StageChannel, StageChannelManager>,
                StandardGuildChannelManager<StageChannel, StageChannelManager>,
                IAgeRestrictedChannelManager<StageChannel, StageChannelManager>,
                ISlowmodeChannelManager<StageChannel, StageChannelManager> {}
