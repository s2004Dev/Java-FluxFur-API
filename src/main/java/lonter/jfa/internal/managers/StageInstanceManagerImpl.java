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

package lonter.jfa.internal.managers;

import lonter.jfa.api.entities.StageInstance;
import lonter.jfa.api.managers.StageInstanceManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StageInstanceManagerImpl extends ManagerBase<StageInstanceManager> implements StageInstanceManager {
    private final StageInstance instance;

    private String topic;

    public StageInstanceManagerImpl(StageInstance instance) {
        super(
                instance.getChannel().getJFA(),
                Route.StageInstances.UPDATE_INSTANCE.compile(
                        instance.getChannel().getId()));
        this.instance = instance;
    }

    @NotNull
    @Override
    public StageInstance getStageInstance() {
        return instance;
    }

    @NotNull
    @Override
    public StageInstanceManager setTopic(@Nullable String topic) {
        if (topic != null) {
            topic = topic.trim();
            Checks.notLonger(topic, 120, "Topic");
            if (topic.isEmpty()) {
                topic = null;
            }
        }
        this.topic = topic;
        set |= TOPIC;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject body = DataObject.empty();
        if (shouldUpdate(TOPIC) && topic != null) {
            body.put("topic", topic);
        }
        return getRequestBody(body);
    }
}
