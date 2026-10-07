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

import lonter.jfa.api.interactions.callbacks.IModalCallback;
import lonter.jfa.api.modals.Modal;
import lonter.jfa.api.requests.restaction.interactions.InteractionCallbackAction;
import lonter.jfa.api.requests.restaction.interactions.ModalCallbackAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.interactions.InteractionImpl;
import okhttp3.RequestBody;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

public class ModalCallbackActionImpl extends InteractionCallbackImpl<Void> implements ModalCallbackAction {
    private final Modal modal;

    public ModalCallbackActionImpl(IModalCallback interaction, Modal modal) {
        super((InteractionImpl) interaction);
        this.modal = modal;
    }

    @Override
    protected RequestBody finalizeData() {
        return getRequestBody(DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.MODAL.getRaw())
                .put("data", modal));
    }

    @NotNull
    @Override
    public ModalCallbackAction setCheck(BooleanSupplier checks) {
        return (ModalCallbackAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public ModalCallbackAction deadline(long timestamp) {
        return (ModalCallbackAction) super.deadline(timestamp);
    }
}
