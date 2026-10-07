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

package lonter.jfa.api.entities.channel.forums;

import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.concrete.ForumChannel;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.utils.messages.MessageCreateData;

import org.jetbrains.annotations.NotNull;

/**
 * Result of creating a post in a {@link ForumChannel}.
 * <br>This is a utility class for {@link ForumChannel#createForumPost(String, MessageCreateData)},
 * the actual post itself is only the {@link #getThreadChannel() thread channel}.
 *
 * @see ForumChannel#createForumPost(String, MessageCreateData)
 * @see #getThreadChannel()
 * @see #getMessage()
 */
public class ForumPost {
    private final Message message;
    private final ThreadChannel thread;

    public ForumPost(@NotNull Message message, @NotNull ThreadChannel thread) {
        this.message = message;
        this.thread = thread;
    }

    /**
     * The starter message of the post.
     * <br>This is created from the {@link MessageCreateData} passed to {@link ForumChannel#createForumPost(String, MessageCreateData)}.
     *
     * @return {@link Message}
     */
    @NotNull
    public Message getMessage() {
        return message;
    }

    /**
     * The {@link ThreadChannel} representing the post.
     * <br>This will use the name provided to {@link ForumChannel#createForumPost(String, MessageCreateData)}.
     *
     * @return The forum post thread channel
     */
    @NotNull
    public ThreadChannel getThreadChannel() {
        return thread;
    }
}
