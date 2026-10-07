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

package lonter.jfa.api.interactions.commands.context;

import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.unions.GuildMessageChannelUnion;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interaction with a message context menu command
 */
public interface MessageContextInteraction extends ContextInteraction<Message> {
    @NotNull
    @Override
    default ContextTarget getTargetType() {
        return ContextTarget.MESSAGE;
    }

    @Nullable
    @Override
    MessageChannelUnion getChannel();

    @NotNull
    @Override
    default GuildMessageChannelUnion getGuildChannel() {
        return (GuildMessageChannelUnion) ContextInteraction.super.getGuildChannel();
    }
}
