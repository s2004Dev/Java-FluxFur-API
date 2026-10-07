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

package lonter.jfa.internal.interactions.modal;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.modals.ModalInteraction;
import lonter.jfa.api.interactions.modals.ModalMapping;
import lonter.jfa.api.requests.restaction.interactions.MessageEditCallbackAction;
import lonter.jfa.api.requests.restaction.interactions.ReplyCallbackAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.interactions.DeferrableInteractionImpl;
import lonter.jfa.internal.requests.restaction.interactions.MessageEditCallbackActionImpl;
import lonter.jfa.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import lonter.jfa.internal.utils.Helpers;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class ModalInteractionImpl extends DeferrableInteractionImpl implements ModalInteraction {
    private final String modalId;
    private final List<ModalMapping> mappings;
    private final Message message;

    public ModalInteractionImpl(JFAImpl api, DataObject object) {
        super(api, object);

        DataObject data = object.getObject("data");
        this.modalId = data.getString("custom_id");
        DataObject resolved = data.optObject("resolved").orElseGet(DataObject::empty);
        this.mappings = data.optArray("components").orElseGet(DataArray::empty).stream(DataArray::getObject)
                .map(component -> getMapping(component, resolved))
                .filter(Objects::nonNull)
                .collect(Helpers.toUnmodifiableList());

        this.message = object.optObject("message")
                .map(o -> api.getEntityBuilder().createMessageWithChannel(o, getMessageChannel(), false))
                .orElse(null);
    }

    private ModalMapping getMapping(DataObject component, DataObject resolved) {
        Component.Type type = Component.Type.fromKey(component.getInt("type"));

        if (type == Component.Type.LABEL) {
            return new ModalMapping(this, resolved, component.getObject("component"));
        }

        return null;
    }

    @NotNull
    @Override
    public String getModalId() {
        return modalId;
    }

    @NotNull
    @Override
    public List<ModalMapping> getValues() {
        return mappings;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @NotNull
    @Override
    public ReplyCallbackAction deferReply() {
        return new ReplyCallbackActionImpl(hook);
    }

    @NotNull
    @Override
    public MessageEditCallbackAction deferEdit() {
        return new MessageEditCallbackActionImpl(hook);
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) super.getChannel();
    }
}
