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
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.data.SerializableData;
import lonter.jfa.internal.utils.IOUtil;
import lonter.jfa.internal.utils.JFALogger;
import lonter.jfa.internal.utils.message.MessageUtil;
import org.slf4j.Logger;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Output of a {@link MessageCreateBuilder} and used for sending messages to channels/webhooks/interactions.
 *
 * @see MessageCreateBuilder
 * @see MessageChannel#sendMessage(MessageCreateData)
 * @see lonter.jfa.api.interactions.callbacks.IReplyCallback#reply(MessageCreateData) IReplyCallback.reply(MessageCreateData)
 * @see lonter.jfa.api.entities.WebhookClient#sendMessage(MessageCreateData) WebhookClient.sendMessage(MessageCreateData)
 */
public class MessageCreateData implements MessageData, AutoCloseable, SerializableData {
    private static final Logger LOG = JFALogger.getLog(MessageCreateData.class);

    private final String content;
    private final List<MessageEmbed> embeds;
    private final List<FileUpload> files;
    private final Set<FileUpload> allDistinctFiles;
    private final List<MessageTopLevelComponentUnion> components;
    private final AllowedMentionsData mentions;
    private final MessagePollData poll;
    private final boolean tts;
    private final int flags;

    protected MessageCreateData(
            String content,
            List<MessageEmbed> embeds,
            List<FileUpload> files,
            List<MessageTopLevelComponentUnion> components,
            AllowedMentionsData mentions,
            MessagePollData poll,
            boolean tts,
            int flags) {
        this.content = content;
        this.embeds = Collections.unmodifiableList(embeds);
        this.files = Collections.unmodifiableList(files);
        this.allDistinctFiles = createAllDistinctFiles(files, components);
        this.components = Collections.unmodifiableList(components);
        this.mentions = mentions;
        this.poll = poll;
        this.tts = tts;
        this.flags = flags;
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setContent(content).build()}.
     *
     * @param  content
     *         The message content (up to {@value Message#MAX_CONTENT_LENGTH})
     *
     * @throws IllegalArgumentException
     *         If the content is null, empty, or longer than {@value Message#MAX_CONTENT_LENGTH}
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setContent(String)
     */
    @NotNull
    public static MessageCreateData fromContent(@NotNull String content) {
        return new MessageCreateBuilder().setContent(content).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setEmbeds(embeds).build()}.
     *
     * @param  embeds
     *         The message embeds (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setEmbeds(Collection)
     */
    @NotNull
    public static MessageCreateData fromEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        return new MessageCreateBuilder().setEmbeds(embeds).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setEmbeds(embeds).build()}.
     *
     * @param  embeds
     *         The message embeds (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setEmbeds(Collection)
     */
    @NotNull
    public static MessageCreateData fromEmbeds(@NotNull MessageEmbed... embeds) {
        return new MessageCreateBuilder().setEmbeds(embeds).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setFiles(embeds).build()}.
     *
     * @param  files
     *         The file uploads
     *
     * @throws IllegalArgumentException
     *         If the null is provided or the list is empty
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setFiles(Collection)
     */
    @NotNull
    public static MessageCreateData fromFiles(@NotNull Collection<? extends FileUpload> files) {
        return new MessageCreateBuilder().setFiles(files).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setFiles(embeds).build()}.
     *
     * @param  files
     *         The file uploads
     *
     * @throws IllegalArgumentException
     *         If the null is provided or the list is empty
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setFiles(Collection)
     */
    @NotNull
    public static MessageCreateData fromFiles(@NotNull FileUpload... files) {
        return new MessageCreateBuilder().setFiles(files).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().applyMessage(message).build()}.
     *
     * @param  message
     *         The message to apply
     *
     * @throws IllegalArgumentException
     *         If the message is null or a system message
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#applyMessage(Message)
     */
    @NotNull
    public static MessageCreateData fromMessage(@NotNull Message message) {
        return new MessageCreateBuilder().applyMessage(message).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().applyEditData(data).build()}.
     *
     * @param  data
     *         The message edit data to apply
     *
     * @throws IllegalArgumentException
     *         If the data is null or empty
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#applyEditData(MessageEditData)
     */
    @NotNull
    public static MessageCreateData fromEditData(@NotNull MessageEditData data) {
        return new MessageCreateBuilder().applyEditData(data).build();
    }

    /**
     * The content of the message.
     *
     * @return The content or an empty string if none was provided
     */
    @NotNull
    @Override
    public String getContent() {
        return content;
    }

    /**
     * The embeds of the message.
     *
     * @return The embeds or an empty list if none were provided
     */
    @NotNull
    @Override
    public List<MessageEmbed> getEmbeds() {
        return embeds;
    }

    /**
     * The components of the message.
     *
     * @return The components or an empty list if none were provided
     */
    @NotNull
    @Override
    public List<MessageTopLevelComponentUnion> getComponents() {
        return components;
    }

    @Override
    public boolean isUsingComponentsV2() {
        return (flags & Message.MessageFlag.IS_COMPONENTS_V2.getValue()) != 0;
    }

    @NotNull
    @Override
    public List<? extends FileUpload> getAttachments() {
        return getFiles();
    }

    /**
     * The poll to send with the message
     *
     * @return The poll, or null if no poll is sent
     */
    @Nullable
    public MessagePollData getPoll() {
        return poll;
    }

    @Override
    public boolean isSuppressEmbeds() {
        return (flags & Message.MessageFlag.EMBEDS_SUPPRESSED.getValue()) != 0;
    }

    /**
     * Whether this message uses <em>Text-to-Speech</em> (TTS).
     *
     * @return True, if text to speech will be used when this is sent
     */
    public boolean isTTS() {
        return tts;
    }

    /**
     * Whether this message is silent.
     *
     * @return True, if the message will not trigger push and desktop notifications.
     */
    public boolean isSuppressedNotifications() {
        return (flags & Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.getValue()) != 0;
    }

    /**
     * Whether this message is intended as a voice message.
     *
     * @return True, if this message is intended as a voice message.
     */
    public boolean isVoiceMessage() {
        return (flags & Message.MessageFlag.IS_VOICE_MESSAGE.getValue()) != 0;
    }

    /**
     * The IDs for users which are allowed to be mentioned, or an empty list.
     *
     * @return The user IDs which are mention whitelisted
     */
    @NotNull
    @Override
    public Set<String> getMentionedUsers() {
        return mentions.getMentionedUsers();
    }

    /**
     * The IDs for roles which are allowed to be mentioned, or an empty list.
     *
     * @return The role IDs which are mention whitelisted
     */
    @NotNull
    @Override
    public Set<String> getMentionedRoles() {
        return mentions.getMentionedRoles();
    }

    /**
     * The mention types which are whitelisted.
     *
     * @return The mention types which can be mentioned by this message
     */
    @NotNull
    @Override
    public EnumSet<Message.MentionType> getAllowedMentions() {
        return mentions.getAllowedMentions();
    }

    /**
     * Whether this message would mention a user, if it is sent as a reply.
     *
     * @return True, if this would mention with the reply
     */
    @Override
    public boolean isMentionRepliedUser() {
        return mentions.isMentionRepliedUser();
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject json = DataObject.empty();
        if (!isUsingComponentsV2()) {
            json.put("content", content);
            json.put("poll", poll);
            json.put("embeds", DataArray.fromCollection(embeds));
        }
        json.put("components", DataArray.fromCollection(components));
        json.put("tts", tts);
        json.put("flags", flags);
        json.put("allowed_mentions", mentions);
        if (files != null && !allDistinctFiles.isEmpty()) {
            json.put("attachments", MessageUtil.getAttachmentsData(getAllDistinctFiles()));
        }

        return json;
    }

    /**
     * The {@link FileUpload FileUploads} attached to this message.
     *
     * @return The list of file uploads
     */
    @NotNull
    public List<FileUpload> getFiles() {
        return files;
    }

    /**
     * Returns both the {@link FileUpload FileUploads} attached to that message,
     * and those added indirectly to this message, such as from V2 components and embeds,
     * references to the same uploads are deduplicated.
     *
     * @return The set of all file uploads
     */
    @NotNull
    public Set<? extends FileUpload> getAllDistinctFiles() {
        return allDistinctFiles;
    }

    @NotNull
    private static Set<FileUpload> createAllDistinctFiles(
            @NotNull Collection<FileUpload> files, @NotNull Collection<MessageTopLevelComponentUnion> components) {
        List<FileUpload> indirectFiles = MessageUtil.getIndirectFiles(components);
        Set<FileUpload> distinctFiles = new LinkedHashSet<>(files.size() + indirectFiles.size());
        distinctFiles.addAll(files);
        distinctFiles.addAll(indirectFiles);
        return Collections.unmodifiableSet(distinctFiles);
    }

    @Override
    public void close() {
        files.forEach(IOUtil::silentClose);
    }
}
