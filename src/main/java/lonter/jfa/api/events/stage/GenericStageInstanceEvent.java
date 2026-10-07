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

package lonter.jfa.api.events.stage;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.StageInstance;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.events.guild.GenericGuildEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link lonter.jfa.api.entities.StageInstance StageInstance} was created/deleted/changed.
 * <br>Every StageInstanceEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any StageInstanceEvent.
 */
public abstract class GenericStageInstanceEvent extends GenericGuildEvent {
    protected final StageInstance instance;

    public GenericStageInstanceEvent(@NotNull JFA api, long responseNumber, @NotNull StageInstance stageInstance) {
        super(api, responseNumber, stageInstance.getGuild());
        this.instance = stageInstance;
    }

    /**
     * The affected {@link StageInstance}
     *
     * @return The {@link StageInstance}
     */
    @NotNull
    public StageInstance getInstance() {
        return instance;
    }

    /**
     * The {@link StageChannel} this instance belongs to
     *
     * @return The StageChannel
     */
    @NotNull
    public StageChannel getChannel() {
        return instance.getChannel();
    }
}
