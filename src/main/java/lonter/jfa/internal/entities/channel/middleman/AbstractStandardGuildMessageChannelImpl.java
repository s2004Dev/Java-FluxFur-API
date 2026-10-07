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

package lonter.jfa.internal.entities.channel.middleman;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.internal.entities.channel.mixin.middleman.StandardGuildMessageChannelMixin;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractStandardGuildMessageChannelImpl<T extends AbstractStandardGuildMessageChannelImpl<T>>
        extends AbstractStandardGuildChannelImpl<T> implements StandardGuildMessageChannelMixin<T> {
    protected String topic;
    protected boolean nsfw;
    protected long latestMessageId;
    protected int defaultThreadSlowmode;

    public AbstractStandardGuildMessageChannelImpl(long id, Guild guild) {
        super(id, guild);
    }

    @Nullable
    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public boolean isNSFW() {
        return nsfw;
    }

    @Override
    public long getLatestMessageIdLong() {
        return latestMessageId;
    }

    @Override
    public int getDefaultThreadSlowmode() {
        return defaultThreadSlowmode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setTopic(String topic) {
        this.topic = topic;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setNSFW(boolean nsfw) {
        this.nsfw = nsfw;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setLatestMessageIdLong(long latestMessageId) {
        this.latestMessageId = latestMessageId;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setDefaultThreadSlowmode(int defaultThreadSlowmode) {
        this.defaultThreadSlowmode = defaultThreadSlowmode;
        return (T) this;
    }
}
