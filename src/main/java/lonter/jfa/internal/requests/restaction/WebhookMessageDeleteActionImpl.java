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
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.WebhookMessageDeleteAction;

public class WebhookMessageDeleteActionImpl
        extends AbstractWebhookMessageActionImpl<Void, WebhookMessageDeleteActionImpl>
        implements WebhookMessageDeleteAction {
    public WebhookMessageDeleteActionImpl(JFA api, Route.CompiledRoute route) {
        super(api, route);
    }

    @Override
    protected Route.CompiledRoute finalizeRoute() {
        Route.CompiledRoute route = super.finalizeRoute();
        if (threadId != null) {
            route = route.withQueryParams("thread_id", threadId);
        }
        return route;
    }
}
