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
import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.entities.IMentionable;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.MessageEmbed;
import lonter.jfa.api.utils.AttachedFile;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.messages.AbstractMessageBuilder;
import lonter.jfa.api.utils.messages.MessageRequest;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
public interface AbstractMessageBuilderMixin<R extends MessageRequest<R>, B extends AbstractMessageBuilder<?, B>>
        extends MessageRequest<R> {
    B getBuilder();

    @NotNull
    @Override
    default R setContent(@Nullable String content) {
        getBuilder().setContent(content);
        return (R) this;
    }

    @NotNull
    @Override
    default String getContent() {
        return getBuilder().getContent();
    }

    @NotNull
    @Override
    default R setEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        getBuilder().setEmbeds(embeds);
        return (R) this;
    }

    @NotNull
    @Override
    default List<MessageEmbed> getEmbeds() {
        return getBuilder().getEmbeds();
    }

    @NotNull
    @Override
    default R setComponents(@NotNull Collection<? extends MessageTopLevelComponent> components) {
        getBuilder().setComponents(components);
        return (R) this;
    }

    @NotNull
    @Override
    default R useComponentsV2(boolean use) {
        getBuilder().useComponentsV2(use);
        return (R) this;
    }

    @NotNull
    @Override
    default List<MessageTopLevelComponentUnion> getComponents() {
        return getBuilder().getComponents();
    }

    @Override
    default boolean isUsingComponentsV2() {
        return getBuilder().isUsingComponentsV2();
    }

    @NotNull
    @Override
    default R setSuppressEmbeds(boolean suppress) {
        getBuilder().setSuppressEmbeds(suppress);
        return (R) this;
    }

    @Override
    default boolean isSuppressEmbeds() {
        return getBuilder().isSuppressEmbeds();
    }

    @NotNull
    @Override
    default R setFiles(@Nullable Collection<? extends FileUpload> files) {
        getBuilder().setFiles(files);
        return (R) this;
    }

    @NotNull
    @Override
    default List<? extends AttachedFile> getAttachments() {
        return getBuilder().getAttachments();
    }

    @NotNull
    @Override
    default R mentionRepliedUser(boolean mention) {
        getBuilder().mentionRepliedUser(mention);
        return (R) this;
    }

    @NotNull
    @Override
    default R setAllowedMentions(@Nullable Collection<Message.MentionType> allowedMentions) {
        getBuilder().setAllowedMentions(allowedMentions);
        return (R) this;
    }

    @NotNull
    @Override
    default R mention(@NotNull Collection<? extends IMentionable> mentions) {
        getBuilder().mention(mentions);
        return (R) this;
    }

    @NotNull
    @Override
    default R mentionUsers(@NotNull Collection<String> userIds) {
        getBuilder().mentionUsers(userIds);
        return (R) this;
    }

    @NotNull
    @Override
    default R mentionRoles(@NotNull Collection<String> roleIds) {
        getBuilder().mentionRoles(roleIds);
        return (R) this;
    }

    @NotNull
    @Override
    default Set<String> getMentionedUsers() {
        return getBuilder().getMentionedUsers();
    }

    @NotNull
    @Override
    default Set<String> getMentionedRoles() {
        return getBuilder().getMentionedRoles();
    }

    @NotNull
    @Override
    default EnumSet<Message.MentionType> getAllowedMentions() {
        return getBuilder().getAllowedMentions();
    }

    @Override
    default boolean isMentionRepliedUser() {
        return getBuilder().isMentionRepliedUser();
    }
}
