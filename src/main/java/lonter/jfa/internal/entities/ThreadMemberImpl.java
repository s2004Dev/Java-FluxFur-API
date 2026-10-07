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

package lonter.jfa.internal.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.ThreadMember;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.internal.entities.channel.concrete.ThreadChannelImpl;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

public class ThreadMemberImpl implements ThreadMember {
    private final JFA api;
    private final ThreadChannelImpl thread;

    private Member member;
    private long joinedTimestamp;

    public ThreadMemberImpl(Member member, ThreadChannelImpl thread) {
        this.api = member.getJFA();
        this.member = member;
        this.thread = thread;
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return thread.getGuild();
    }

    @NotNull
    @Override
    public ThreadChannel getThread() {
        return this.thread;
    }

    @NotNull
    @Override
    public User getUser() {
        return member.getUser();
    }

    @NotNull
    @Override
    public Member getMember() {
        return member;
    }

    @NotNull
    @Override
    public OffsetDateTime getTimeJoined() {
        return Helpers.toOffset(joinedTimestamp);
    }

    @NotNull
    @Override
    public String getAsMention() {
        return member.getAsMention();
    }

    @Override
    public long getIdLong() {
        return member.getIdLong();
    }

    // ===== Setters =======

    public ThreadMemberImpl setJoinedTimestamp(long joinedTimestamp) {
        this.joinedTimestamp = joinedTimestamp;
        return this;
    }

    @Override
    public String toString() {
        return new EntityString(this).addMetadata("member", getMember()).toString();
    }
}
