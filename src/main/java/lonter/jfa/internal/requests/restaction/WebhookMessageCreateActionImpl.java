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
import lonter.jfa.api.entities.Message.MessageFlag;
import lonter.jfa.api.entities.channel.forums.ForumTagSnowflake;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.ThreadCreateMetadata;
import lonter.jfa.api.requests.restaction.WebhookMessageCreateAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.messages.MessageCreateBuilder;
import lonter.jfa.api.utils.messages.MessageCreateData;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WebhookMessageCreateActionImpl<T>
        extends AbstractWebhookMessageActionImpl<T, WebhookMessageCreateActionImpl<T>>
        implements WebhookMessageCreateAction<T>, MessageCreateBuilderMixin<WebhookMessageCreateAction<T>> {
    private final MessageCreateBuilder builder = new MessageCreateBuilder();
    private final Function<DataObject, T> transformer;

    private boolean isInteraction = true;

    // Interactions only
    private boolean ephemeral;

    // Incoming webhooks only

    private String username;
    private String avatar;
    private ThreadCreateMetadata threadMetadata;

    public WebhookMessageCreateActionImpl(JFA api, Route.CompiledRoute route, Function<DataObject, T> transformer) {
        super(api, route);
        this.transformer = transformer;
    }

    public WebhookMessageCreateActionImpl<T> setInteraction(boolean isInteraction) {
        this.isInteraction = isInteraction;
        return this;
    }

    @Override
    public MessageCreateBuilder getBuilder() {
        return builder;
    }

    @NotNull
    @Override
    public WebhookMessageCreateActionImpl<T> setEphemeral(boolean ephemeral) {
        if (!isInteraction && ephemeral) {
            throw new IllegalStateException(
                    "Cannot create ephemeral messages with webhooks. Use InteractionHook instead!");
        }

        this.ephemeral = ephemeral;
        return this;
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> setUsername(@Nullable String name) {
        if (isInteraction && username != null) {
            throw new IllegalStateException("Cannot set username on interaction messages.");
        }

        if (name != null) {
            name = name.trim();
            Checks.inRange(name, 1, 80, "Name"); // See
            // https://fluxer.com/developers/docs/resources/webhook#create-webhook
        }

        this.username = name;
        return this;
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> setAvatarUrl(@Nullable String iconUrl) {
        if (isInteraction && iconUrl != null) {
            throw new IllegalStateException("Cannot set avatar on interaction messages.");
        }

        if (iconUrl != null) {
            Checks.noWhitespace(iconUrl, "Avatar URL");
            Checks.check(
                    iconUrl.startsWith("https://") || iconUrl.startsWith("http://"),
                    "Invalid URL format. Must start with 'https://' or 'http://'. Provided %s",
                    iconUrl);
        }

        this.avatar = iconUrl;
        return this;
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> createThread(@NotNull ThreadCreateMetadata threadMetadata) {
        if (isInteraction) {
            throw new IllegalStateException("Cannot create a thread through an interaction hook.");
        }

        Checks.notNull(threadMetadata, "Thread Metadata");
        this.threadMetadata = threadMetadata;

        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        try (MessageCreateData data = builder.build()) {
            DataObject json = data.toData();
            if (ephemeral) {
                json.put("flags", json.getInt("flags", 0) | MessageFlag.EPHEMERAL.getValue());
            }

            if (username != null) {
                json.put("username", username);
            }
            if (avatar != null) {
                json.put("avatar_url", avatar);
            }

            if (threadId == null && threadMetadata != null) {
                json.put("thread_name", threadMetadata.getName());
                List<ForumTagSnowflake> tags = threadMetadata.getAppliedTags();
                if (!tags.isEmpty()) {
                    json.put(
                            "applied_tags",
                            tags.stream().map(ForumTagSnowflake::getId).collect(Helpers.toDataArray()));
                }
            }

            return getMultipartBody(data.getAllDistinctFiles(), json);
        }
    }

    @Override
    protected Route.CompiledRoute finalizeRoute() {
        Route.CompiledRoute route = super.finalizeRoute();
        if (threadId != null) {
            route = route.withQueryParams("thread_id", threadId);
        }

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<T> request) {
        T message = transformer.apply(response.getObject());
        request.onSuccess(message);
    }
}
