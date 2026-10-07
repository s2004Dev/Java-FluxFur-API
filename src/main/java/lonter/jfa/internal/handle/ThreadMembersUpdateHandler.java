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

package lonter.jfa.internal.handle;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lonter.jfa.api.entities.ThreadMember;
import lonter.jfa.api.events.thread.member.ThreadMemberJoinEvent;
import lonter.jfa.api.events.thread.member.ThreadMemberLeaveEvent;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.cache.CacheView;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.channel.concrete.ThreadChannelImpl;
import lonter.jfa.internal.utils.UnlockHook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ThreadMembersUpdateHandler extends SocketHandler {
    public ThreadMembersUpdateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        long threadId = content.getLong("id");
        ThreadChannelImpl thread = (ThreadChannelImpl) getJFA().getThreadChannelById(threadId);
        if (thread == null) {
            getJFA().getEventCache().cache(EventCache.Type.CHANNEL, threadId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug(
                    "THREAD_MEMBERS_UPDATE attempted to update a thread that does not exist. JSON: {}", content);
            return null;
        }

        if (!content.isNull("added_members")) {
            DataArray addedMembersJson = content.getArray("added_members");
            handleAddedThreadMembers(thread, addedMembersJson);
        }

        if (!content.isNull("removed_member_ids")) {
            List<Long> removedMemberIds = content.getArray("removed_member_ids").stream(DataArray::getString)
                    .map(MiscUtil::parseSnowflake)
                    .collect(Collectors.toList());
            handleRemovedThreadMembers(thread, removedMemberIds);
        }

        return null;
    }

    private void handleAddedThreadMembers(ThreadChannelImpl thread, DataArray addedMembersJson) {
        EntityBuilder entityBuilder = api.getEntityBuilder();
        CacheView.SimpleCacheView<ThreadMember> view = thread.getThreadMemberView();

        List<ThreadMember> addedThreadMembers = new ArrayList<>();
        for (int i = 0; i < addedMembersJson.length(); i++) {
            DataObject threadMemberJson = addedMembersJson.getObject(i);
            ThreadMember threadMember = entityBuilder.createThreadMember(thread.getGuild(), thread, threadMemberJson);
            addedThreadMembers.add(threadMember);
        }

        // TODO-Threads: We assume here that we are allowed to cache these, however, we probably
        // need to check the ChunkFilter first as the
        // underlying Member object might have been created when creating the ThreadMember and it
        // might not be being updated. We don't
        // want to cache ThreadMembers if the Members they're based on aren't being cached.
        try (UnlockHook lock = view.writeLock()) {
            for (ThreadMember threadMember : addedThreadMembers) {
                view.getMap().put(threadMember.getIdLong(), threadMember);
            }
        }

        // Emit the events from outside the writeLock
        for (ThreadMember threadMember : addedThreadMembers) {
            api.handleEvent(new ThreadMemberJoinEvent(
                    api, responseNumber,
                    thread, threadMember));
        }
    }

    private void handleRemovedThreadMembers(ThreadChannelImpl thread, List<Long> removedMemberIds) {
        CacheView.SimpleCacheView<ThreadMember> view = thread.getThreadMemberView();

        // Store the removed threads into a map so that we can provide them in the events later.
        // We don't want to dispatch the events from inside the writeLock
        TLongObjectMap<ThreadMember> removedThreadMembers = new TLongObjectHashMap<>();
        try (UnlockHook lock = view.writeLock()) {
            for (long threadMemberId : removedMemberIds) {
                ThreadMember threadMember = view.getMap().remove(threadMemberId);
                removedThreadMembers.put(threadMemberId, threadMember);
            }
        }

        for (long threadMemberId : removedMemberIds) {
            api.handleEvent(new ThreadMemberLeaveEvent(
                    api, responseNumber, thread, threadMemberId, removedThreadMembers.remove(threadMemberId)));
        }
    }
}
