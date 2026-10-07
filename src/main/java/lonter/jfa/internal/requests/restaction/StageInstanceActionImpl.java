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

package lonter.jfa.internal.requests.restaction;

import lonter.jfa.api.entities.StageInstance;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.StageInstanceAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

public class StageInstanceActionImpl extends RestActionImpl<StageInstance> implements StageInstanceAction {
    private final StageChannel channel;
    private String topic;

    public StageInstanceActionImpl(StageChannel channel) {
        super(channel.getJFA(), Route.StageInstances.CREATE_INSTANCE.compile());
        this.channel = channel;
    }

    @NotNull
    @Override
    public StageInstanceAction setCheck(BooleanSupplier checks) {
        return (StageInstanceAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public StageInstanceAction timeout(long timeout, @NotNull TimeUnit unit) {
        return (StageInstanceAction) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public StageInstanceAction deadline(long timestamp) {
        return (StageInstanceAction) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public StageInstanceAction setTopic(@NotNull String topic) {
        Checks.notBlank(topic, "Topic");
        Checks.notLonger(topic, 120, "Topic");
        this.topic = topic;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject body = DataObject.empty();
        body.put("channel_id", channel.getId());
        body.put("topic", topic);
        return getRequestBody(body);
    }

    @Override
    protected void handleSuccess(Response response, Request<StageInstance> request) {
        StageInstance instance =
                api.getEntityBuilder().createStageInstance((GuildImpl) channel.getGuild(), response.getObject());
        request.onSuccess(instance);
    }
}
