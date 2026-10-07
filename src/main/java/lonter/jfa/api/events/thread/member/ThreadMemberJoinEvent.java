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

package lonter.jfa.api.events.thread.member;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.ThreadMember;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a guild {@link Member} joined a {@link ThreadChannel}.
 *
 * @see ThreadChannel
 * @see ThreadMember
 */
public class ThreadMemberJoinEvent extends GenericThreadMemberEvent {
    public ThreadMemberJoinEvent(
            @NotNull JFA api, long responseNumber, ThreadChannel thread, ThreadMember threadMember) {
        super(api, responseNumber, thread, threadMember.getIdLong(), threadMember);
    }

    /**
     * The {@link ThreadMember} that just joined the thread.
     * This entity will be present in {@link ThreadChannel#getThreadMembers()} list.
     *
     * @return The {@link ThreadMember} that just joined the thread.
     */
    @NotNull
    @Override
    public ThreadMember getThreadMember() {
        return super.getThreadMember();
    }

    /**
     * The {@link ThreadMember} that just joined the thread as a guild {@link Member}.
     *
     * @return The {@link ThreadMember} that just joined the thread as a guild {@link Member}.
     */
    @NotNull
    @Override
    public Member getMember() {
        // Explicitly override the getter from the super class
        // to use the member return in the thread member itself,
        // because the ThreadMember will always have the Member while the Guild itself might not,
        // because of the ChunkingFilter or a lack of GUILD_MEMBERS intent.
        return getThreadMember().getMember();
    }
}
