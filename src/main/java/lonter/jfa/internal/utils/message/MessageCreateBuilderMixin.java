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

package lonter.jfa.internal.utils.message;

import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.entities.MessageEmbed;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.messages.MessageCreateBuilder;
import lonter.jfa.api.utils.messages.MessageCreateRequest;
import lonter.jfa.api.utils.messages.MessagePollData;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public interface MessageCreateBuilderMixin<R extends MessageCreateRequest<R>>
        extends AbstractMessageBuilderMixin<R, MessageCreateBuilder>, MessageCreateRequest<R> {
    @NotNull
    @Override
    default R addContent(@NotNull String content) {
        getBuilder().addContent(content);
        return (R) this;
    }

    @NotNull
    @Override
    default R addEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        getBuilder().addEmbeds(embeds);
        return (R) this;
    }

    @NotNull
    @Override
    default R addComponents(@NotNull Collection<? extends MessageTopLevelComponent> components) {
        getBuilder().addComponents(components);
        return (R) this;
    }

    @NotNull
    @Override
    default R addFiles(@NotNull Collection<? extends FileUpload> files) {
        getBuilder().addFiles(files);
        return (R) this;
    }

    @Nullable
    @Override
    default MessagePollData getPoll() {
        return getBuilder().getPoll();
    }

    @NotNull
    @Override
    default R setPoll(@Nullable MessagePollData poll) {
        getBuilder().setPoll(poll);
        return (R) this;
    }

    @NotNull
    @Override
    default R setTTS(boolean tts) {
        getBuilder().setTTS(tts);
        return (R) this;
    }

    @NotNull
    @Override
    default R setFiles(@Nullable Collection<? extends FileUpload> files) {
        getBuilder().setFiles(files);
        return (R) this;
    }

    @NotNull
    @Override
    default List<FileUpload> getAttachments() {
        return getBuilder().getAttachments();
    }

    @NotNull
    @Override
    default R setSuppressedNotifications(boolean suppressed) {
        getBuilder().setSuppressedNotifications(suppressed);
        return (R) this;
    }

    @NotNull
    @Override
    default R setVoiceMessage(boolean voiceMessage) {
        getBuilder().setVoiceMessage(voiceMessage);
        return (R) this;
    }
}
