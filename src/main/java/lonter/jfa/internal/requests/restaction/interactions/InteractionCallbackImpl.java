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

package lonter.jfa.internal.requests.restaction.interactions;

import lonter.jfa.api.exceptions.ErrorResponseException;
import lonter.jfa.api.requests.*;
import lonter.jfa.api.requests.restaction.interactions.InteractionCallbackAction;
import lonter.jfa.internal.interactions.InteractionImpl;
import lonter.jfa.internal.requests.RestActionImpl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

public abstract class InteractionCallbackImpl<T> extends RestActionImpl<T> implements InteractionCallbackAction<T> {
    protected final InteractionImpl interaction;

    public InteractionCallbackImpl(InteractionImpl interaction) {
        super(
                interaction.getJFA(),
                Route.Interactions.CALLBACK
                        .compile(interaction.getId(), interaction.getToken())
                        .withQueryParams("with_response", "true"));
        this.interaction = interaction;
        setErrorMapper(this::handleUnknownInteraction);
    }

    private Throwable handleUnknownInteraction(
            Response response, Request<?> request, ErrorResponseException exception) {
        // While this error response has existed since at least 2022
        // (https://github.com/fluxer/fluxer-api-docs/pull/4484),
        // Fluxer does not always report this error correctly and instead sends a
        // 'UNKNOWN_INTERACTION'.
        // That's why we also have a similar exception at the end of this method.
        if (exception.getErrorResponse() == ErrorResponse.INTERACTION_ALREADY_ACKNOWLEDGED) {
            return ErrorResponseException.create(
                    "This interaction was acknowledged by another process running for the same bot.\n"
                            + "To resolve this, try stopping all current processes for the bot that could be responsible, or resetting your bot token.\n"
                            + "You can reset your token at https://fluxer.com/developers/applications/"
                            + getJFA().getSelfUser().getApplicationId() + "/bot",
                    exception);
        }

        // Time synchronization issues prevent us from checking the exact nature of the issue,
        // and storing a local Instant would be invalid in case the WS thread is blocked,
        // as it will be created when the thread is released.
        // Send a message for both issues instead.
        if (exception.getErrorResponse() == ErrorResponse.UNKNOWN_INTERACTION) {
            return ErrorResponseException.create(
                    "Failed to acknowledge this interaction, this can be due to 2 reasons:\n"
                            + "1. This interaction took longer than 3 seconds to be acknowledged, see https://jfa.wiki/using-jfa/troubleshooting/#the-interaction-took-longer-than-3-seconds-to-be-acknowledged\n"
                            + "2. This interaction could have been acknowledged by another process running for the same bot\n"
                            + "You can confirm this by checking if your bot replied, or the three dots in a button disappeared without saying 'This interaction failed', or you see '[Bot] is thinking...' for more than 3 seconds.\n"
                            + "To resolve this, try stopping all current processes for the bot that could be responsible, or resetting your bot token.\n"
                            + "You can reset your token at https://fluxer.com/developers/applications/"
                            + getJFA().getSelfUser().getApplicationId() + "/bot",
                    exception);
        }

        return null;
    }

    @NotNull
    @Override
    public InteractionCallbackAction<T> closeResources() {
        return this;
    }

    // Here we intercept calls to queue/submit/complete to prevent double ack/reply scenarios with a
    // better error message than fluxer provides

    // This is an exception factory method that only returns an exception if we would have to throw
    // it or fail in another way.
    // note that interaction.ack() is already synchronized so this is actually thread-safe!
    protected final IllegalStateException tryAck() {
        // true => we already called this before => this will never succeed!
        return interaction.ack()
                ? new IllegalStateException(
                        "This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!")
                : null; // null indicates we were successful, no exception means we can't fail :)
    }

    @Override
    public final void queue(Consumer<? super T> success, Consumer<? super Throwable> failure) {
        IllegalStateException exception = tryAck();
        if (exception != null) {
            if (failure != null) {
                // if the failure callback throws that will just bubble up, which is acceptable
                failure.accept(exception);
            } else {
                RestAction.getDefaultFailure().accept(exception);
            }
            return;
        }

        super.queue(success, failure);
    }

    @NotNull
    @Override
    public final CompletableFuture<T> submit(boolean shouldQueue) {
        IllegalStateException exception = tryAck();
        if (exception != null) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(exception);
            return future;
        }

        return super.submit(shouldQueue);
    }

    // Here we handle the interaction hook, which awaits the signal that the interaction was
    // acknowledged before sending any requests.

    @Override
    protected void handleSuccess(Response response, Request<T> request) {
        interaction.releaseHook(true); // sends followup messages
        super.handleSuccess(response, request);
    }

    @Override
    public void handleResponse(Response response, Request<T> request) {
        if (!response.isOk()) {
            interaction.releaseHook(false); // cancels followup messages with an exception
        }
        super.handleResponse(response, request);
    }
}
