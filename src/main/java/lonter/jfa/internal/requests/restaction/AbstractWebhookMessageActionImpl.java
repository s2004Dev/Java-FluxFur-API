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

import lonter.jfa.api.JFA;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.internal.utils.Checks;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWebhookMessageActionImpl<T, R extends AbstractWebhookMessageActionImpl<T, R>>
        extends TriggerRestAction<T> {
    protected String threadId;

    public AbstractWebhookMessageActionImpl(JFA api, Route.CompiledRoute route) {
        super(api, route);
    }

    public AbstractWebhookMessageActionImpl(
            JFA api, Route.CompiledRoute route, BiFunction<Response, Request<T>, T> handler) {
        super(api, route, handler);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public R setThreadId(@Nullable String threadId) {
        if (threadId != null) {
            Checks.isSnowflake(threadId, "Thread ID");
        }
        this.threadId = threadId;
        return (R) this;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public R setCheck(BooleanSupplier checks) {
        return (R) super.setCheck(checks);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public R deadline(long timestamp) {
        return (R) super.deadline(timestamp);
    }
}
