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

import lonter.jfa.api.entities.ThreadMember;
import lonter.jfa.api.utils.cache.CacheView;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.channel.concrete.ThreadChannelImpl;
import lonter.jfa.internal.utils.UnlockHook;

public class ThreadMemberUpdateHandler extends SocketHandler {
    public ThreadMemberUpdateHandler(JFAImpl api) {
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
                    "THREAD_MEMBER_UPDATE attempted to update a thread that does not exist. JSON: {}", content);
            return null;
        }

        // Based on the docs it is expected that we will only ever receive THREAD_MEMBER_UPDATE when
        // Fluxer needs to inform us that we are a member of a ThreadChannels that we might not have in memory.
        // Currently this only happens for ThreadChannels that get unarchived.
        // Details available at:
        // https://fluxer.com/developers/docs/topics/threads#unarchiving-a-thread
        long userId = content.getLong("user_id");
        if (userId != getJFA().getSelfUser().getIdLong()) {
            JFAImpl.LOG.warn(
                    "Received a THREAD_MEMBER_UPDATE for a user that isn't the current bot user. "
                            + "This validates assumptions that THREAD_MEMBER_UPDATE would ONLY be for the current bot user. "
                            + "Skipping this dispatch for now. This should be reported as a bug."
                            + "\nDetails: {}",
                    content);
            return null;
        }

        CacheView.SimpleCacheView<ThreadMember> view = thread.getThreadMemberView();
        try (UnlockHook lock = view.writeLock()) {
            // We might have still had the ThreadChannel in memory, so our ThreadMember might still
            // exist. Do an existence check.
            ThreadMember threadMember = view.getMap().get(userId);
            if (threadMember == null) {
                threadMember = api.getEntityBuilder()
                        .createThreadMember(thread, thread.getGuild().getSelfMember(), content);
                view.getMap().put(threadMember.getIdLong(), threadMember);
            }
        }

        return null;
    }
}
