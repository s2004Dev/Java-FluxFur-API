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

package lonter.jfa.api.events.channel.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.ChannelField;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the {@link IPostContainer#getDefaultSortOrder() default sort order} of a {@link IPostContainer} changed.
 *
 * <p>Can be used to retrieve the old default sort order and the new one.
 *
 * @see ChannelField#DEFAULT_SORT_ORDER
 */
@SuppressWarnings("ConstantConditions")
public class ChannelUpdateDefaultSortOrderEvent extends GenericChannelUpdateEvent<IPostContainer.SortOrder> {
    public static final ChannelField FIELD = ChannelField.DEFAULT_SORT_ORDER;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateDefaultSortOrderEvent(
            @NotNull JFA api, long responseNumber, IPostContainer channel, IPostContainer.SortOrder oldValue) {
        super(api, responseNumber, channel, ChannelField.DEFAULT_SORT_ORDER, oldValue, channel.getDefaultSortOrder());
    }

    @NotNull
    @Override
    public IPostContainer.SortOrder getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public IPostContainer.SortOrder getNewValue() {
        return super.getNewValue();
    }
}
