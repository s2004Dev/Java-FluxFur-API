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

import lonter.jfa.api.utils.AttachedFile;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.messages.MessageEditBuilder;
import lonter.jfa.api.utils.messages.MessageEditData;
import lonter.jfa.api.utils.messages.MessageEditRequest;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public interface MessageEditBuilderMixin<R extends MessageEditRequest<R>>
        extends AbstractMessageBuilderMixin<R, MessageEditBuilder>, MessageEditRequest<R> {
    @NotNull
    @Override
    default R setAttachments(@Nullable Collection<? extends AttachedFile> attachments) {
        getBuilder().setAttachments(attachments);
        return (R) this;
    }

    @NotNull
    @Override
    default R setReplace(boolean isReplace) {
        getBuilder().setReplace(isReplace);
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
    default R applyData(@NotNull MessageEditData data) {
        getBuilder().applyData(data);
        return (R) this;
    }

    @Override
    default boolean isReplace() {
        return getBuilder().isReplace();
    }
}
