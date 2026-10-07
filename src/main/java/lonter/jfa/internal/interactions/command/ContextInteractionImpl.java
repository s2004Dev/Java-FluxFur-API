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

package lonter.jfa.internal.interactions.command;

import lonter.jfa.api.interactions.commands.CommandInteractionPayload;
import lonter.jfa.api.interactions.commands.context.ContextInteraction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;

import org.jetbrains.annotations.NotNull;

public abstract class ContextInteractionImpl<T> extends CommandInteractionImpl
        implements ContextInteraction<T>, CommandInteractionPayloadMixin {
    private final T target;
    private final CommandInteractionPayloadImpl payload;

    public ContextInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
        this.payload = new CommandInteractionPayloadImpl(jfa, data);
        this.target = parse(data, data.getObject("data").getObject("resolved"));
    }

    protected abstract T parse(DataObject interactionData, DataObject resolved);

    @Override
    public CommandInteractionPayload getCommandPayload() {
        return payload;
    }

    @NotNull
    @Override
    public T getTarget() {
        return target;
    }
}
