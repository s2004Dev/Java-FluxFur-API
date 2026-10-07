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

package lonter.jfa.api.utils.messages;

import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.MessageEmbed;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.utils.AttachedFile;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.data.SerializableData;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.IOUtil;
import lonter.jfa.internal.utils.message.MessageUtil;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lonter.jfa.api.utils.messages.MessageEditBuilder.*;

/**
 * Output of a {@link MessageEditRequest} and used for editing messages in channels/webhooks/interactions.
 *
 * @see MessageEditBuilder
 * @see MessageChannel#editMessageById(String, MessageEditData)
 * @see lonter.jfa.api.interactions.callbacks.IMessageEditCallback#editMessage(MessageEditData) IMessageEditCallback.editMessage(MessageEditData)
 * @see lonter.jfa.api.entities.WebhookClient#editMessageById(String, MessageEditData) WebhookClient.editMessageById(String, MessageEditData)
 * @see lonter.jfa.api.interactions.InteractionHook#editOriginal(MessageEditData) InteractionHook.editOriginal(MessageEditData)
 */
public class MessageEditData implements MessageData, AutoCloseable, SerializableData {
    protected final AllowedMentionsData mentions;
    private final String content;
    private final List<MessageEmbed> embeds;
    private final List<AttachedFile> files;
    private final Set<AttachedFile> allDistinctFiles;
    private final List<MessageTopLevelComponentUnion> components;
    private final int messageFlags;

    private final boolean isReplace;
    private final int configuredFields;

    protected MessageEditData(
            int configuredFields,
            int messageFlags,
            boolean isReplace,
            String content,
            List<MessageEmbed> embeds,
            List<AttachedFile> files,
            List<MessageTopLevelComponentUnion> components,
            AllowedMentionsData mentions) {
        this.content = content;
        this.embeds = Collections.unmodifiableList(embeds);
        this.files = Collections.unmodifiableList(files);
        this.allDistinctFiles = createAllDistinctFiles(files, components);
        this.components = Collections.unmodifiableList(components);
        this.mentions = mentions;
        this.messageFlags = messageFlags;
        this.isReplace = isReplace;
        this.configuredFields = configuredFields;
    }

    /**
     * Shortcut for {@code new MessageEditBuilder().setContent(content).build()}.
     *
     * @param  content
     *         The message content (up to {@value Message#MAX_CONTENT_LENGTH})
     *
     * @throws IllegalArgumentException
     *         If the content is null, empty, or longer than {@value Message#MAX_CONTENT_LENGTH}
     *
     * @return New valid instance of MessageEditData
     *
     * @see    MessageEditBuilder#setContent(String)
     */
    @NotNull
    public static MessageEditData fromContent(@NotNull String content) {
        return new MessageEditBuilder().setContent(content).build();
    }

    /**
     * Shortcut for {@code new MessageEditBuilder().setEmbeds(embeds).build()}.
     *
     * @param  embeds
     *         The message embeds (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return New valid instance of MessageEditData
     *
     * @see    MessageEditBuilder#setEmbeds(Collection)
     */
    @NotNull
    public static MessageEditData fromEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        return new MessageEditBuilder().setEmbeds(embeds).build();
    }

    /**
     * Shortcut for {@code new MessageEditBuilder().setEmbeds(embeds).build()}.
     *
     * @param  embeds
     *         The message embeds (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return New valid instance of MessageEditData
     *
     * @see    MessageEditBuilder#setEmbeds(Collection)
     */
    @NotNull
    public static MessageEditData fromEmbeds(@NotNull MessageEmbed... embeds) {
        return new MessageEditBuilder().setEmbeds(embeds).build();
    }

    /**
     * Shortcut for {@code new MessageEditBuilder().setFiles(embeds).build()}.
     *
     * @param  files
     *         The file uploads
     *
     * @throws IllegalArgumentException
     *         If the null is provided or the list is empty
     *
     * @return New valid instance of MessageEditData
     *
     * @see    MessageEditBuilder#setFiles(Collection)
     */
    @NotNull
    public static MessageEditData fromFiles(@NotNull Collection<? extends FileUpload> files) {
        return new MessageEditBuilder().setFiles(files).build();
    }

    /**
     * Shortcut for {@code new MessageEditBuilder().setFiles(embeds).build()}.
     *
     * @param  files
     *         The file uploads
     *
     * @throws IllegalArgumentException
     *         If the null is provided or the list is empty
     *
     * @return New valid instance of MessageEditData
     *
     * @see    MessageEditBuilder#setFiles(Collection)
     */
    @NotNull
    public static MessageEditData fromFiles(@NotNull FileUpload... files) {
        return new MessageEditBuilder().setFiles(files).build();
    }

    /**
     * Shortcut for {@code new MessageEditBuilder().applyMessage(message).build()}.
     *
     * @param  message
     *         The message to apply
     *
     * @throws IllegalArgumentException
     *         If the message is null or a system message
     *
     * @return New valid instance of MessageEditData
     *
     * @see    MessageEditBuilder#applyMessage(Message)
     */
    @NotNull
    public static MessageEditData fromMessage(@NotNull Message message) {
        return new MessageEditBuilder().applyMessage(message).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().applyCreateData(data).build()}.
     *
     * @param  data
     *         The message create data to apply
     *
     * @throws IllegalArgumentException
     *         If the data is null or empty
     *
     * @return New valid instance of MessageEditData
     *
     * @see    MessageEditBuilder#applyCreateData(MessageCreateData)
     */
    @NotNull
    public static MessageEditData fromCreateData(@NotNull MessageCreateData data) {
        return new MessageEditBuilder().applyCreateData(data).build();
    }

    protected boolean isReplace() {
        return isReplace;
    }

    protected int getConfiguredFields() {
        return configuredFields;
    }

    protected int getFlags() {
        return messageFlags;
    }

    /**
     * The content of the message.
     *
     * @return The content or an empty string if none was set
     */
    @NotNull
    public String getContent() {
        return content;
    }

    /**
     * The embeds of the message.
     *
     * @return The embeds or an empty list if none were set
     */
    @NotNull
    public List<MessageEmbed> getEmbeds() {
        return embeds;
    }

    /**
     * The components of the message.
     *
     * @return The components or an empty list if none were set
     */
    @NotNull
    public List<MessageTopLevelComponentUnion> getComponents() {
        return components;
    }

    @Override
    public boolean isUsingComponentsV2() {
        return (messageFlags & Message.MessageFlag.IS_COMPONENTS_V2.getValue()) != 0;
    }

    /**
     * The {@link AttachedFile AttachedFiles} attached to this message.
     *
     * @return The list of attachments, or an empty list if none were set
     */
    @NotNull
    public List<AttachedFile> getAttachments() {
        return files;
    }

    @Override
    public boolean isSuppressEmbeds() {
        return isSet(Message.MessageFlag.EMBEDS_SUPPRESSED.getValue());
    }

    /**
     * The IDs for users which are allowed to be mentioned, or an empty list.
     *
     * @return The user IDs which are mention whitelisted
     */
    @NotNull
    public Set<String> getMentionedUsers() {
        return mentions.getMentionedUsers();
    }

    /**
     * The IDs for roles which are allowed to be mentioned, or an empty list.
     *
     * @return The role IDs which are mention whitelisted
     */
    @NotNull
    public Set<String> getMentionedRoles() {
        return mentions.getMentionedRoles();
    }

    /**
     * The mention types which are whitelisted.
     *
     * @return The mention types which can be mentioned by this message
     */
    @NotNull
    public EnumSet<Message.MentionType> getAllowedMentions() {
        return mentions.getAllowedMentions();
    }

    /**
     * Whether this message would mention a user, if it is sent as a reply.
     *
     * @return True, if this would mention with the reply
     */
    public boolean isMentionRepliedUser() {
        return mentions.isMentionRepliedUser();
    }

    @NotNull
    @Override
    public synchronized DataObject toData() {
        DataObject json = DataObject.empty();
        if (isSet(CONTENT)) {
            json.put("content", content);
        }
        if (isSet(EMBEDS)) {
            json.put("embeds", DataArray.fromCollection(embeds));
        }
        if (isSet(COMPONENTS)) {
            json.put("components", DataArray.fromCollection(components));
        }
        if (isSet(MENTIONS)) {
            json.put("allowed_mentions", mentions);
        }
        if (isSet(FLAGS)) {
            json.put("flags", messageFlags);
        }

        List<FileUpload> additionalFiles = MessageUtil.getIndirectFiles(components);
        if (isSet(ATTACHMENTS) || !additionalFiles.isEmpty()) {
            json.put("attachments", MessageUtil.getAttachmentsData(allDistinctFiles));
        }

        return json;
    }

    /**
     * The {@link FileUpload FileUploads} attached to this message.
     *
     * @return The list of file uploads
     */
    @NotNull
    public synchronized List<FileUpload> getFiles() {
        return files.stream()
                .filter(FileUpload.class::isInstance)
                .map(FileUpload.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Returns both the {@link FileUpload FileUploads} attached to that message,
     * and those added indirectly to this message, such as from V2 components and embeds,
     * references to the same uploads are deduplicated.
     *
     * @return The set of all file uploads
     */
    @NotNull
    public Set<? extends AttachedFile> getAllDistinctFiles() {
        return allDistinctFiles;
    }

    @NotNull
    public static Set<AttachedFile> createAllDistinctFiles(
            @Nullable Collection<AttachedFile> files, @NotNull Collection<MessageTopLevelComponentUnion> components) {
        List<FileUpload> indirectFiles = MessageUtil.getIndirectFiles(components);
        Set<AttachedFile> distinctFiles =
                new LinkedHashSet<>((files == null ? 0 : files.size()) + indirectFiles.size());
        if (files != null) {
            distinctFiles.addAll(files);
        }
        distinctFiles.addAll(indirectFiles);
        return Collections.unmodifiableSet(distinctFiles);
    }

    @Override
    public synchronized void close() {
        files.forEach(IOUtil::silentClose);
    }

    protected boolean isSet(int flag) {
        return isReplace || (configuredFields & flag) != 0;
    }
}
