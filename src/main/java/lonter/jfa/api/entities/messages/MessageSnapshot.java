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

package lonter.jfa.api.entities.messages;

import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.components.tree.MessageComponentTree;
import lonter.jfa.api.entities.Mentions;
import lonter.jfa.api.entities.Message.Attachment;
import lonter.jfa.api.entities.Message.MessageFlag;
import lonter.jfa.api.entities.MessageEmbed;
import lonter.jfa.api.entities.MessageType;
import lonter.jfa.api.entities.sticker.StickerItem;
import org.jetbrains.annotations.Unmodifiable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lonter.jfa.api.entities.Message.INVITE_PATTERN;

/**
 * Snapshot of a forwarded message.
 */
public class MessageSnapshot {
    private final Object mutex = new Object();

    private final MessageType type;
    private final Mentions mentions;
    private final OffsetDateTime editTime;
    private final String content;
    private final List<Attachment> attachments;
    private final List<MessageEmbed> embeds;
    private final List<MessageTopLevelComponentUnion> components;
    private final List<StickerItem> stickers;
    private final long flags;

    private List<String> invites;

    public MessageSnapshot(
            MessageType type,
            Mentions mentions,
            OffsetDateTime editTime,
            String content,
            List<Attachment> attachments,
            List<MessageEmbed> embeds,
            List<MessageTopLevelComponentUnion> components,
            List<StickerItem> stickers,
            long flags) {
        this.type = type;
        this.mentions = mentions;
        this.editTime = editTime;
        this.content = content;
        this.attachments = Collections.unmodifiableList(attachments);
        this.embeds = Collections.unmodifiableList(embeds);
        this.components = Collections.unmodifiableList(components);
        this.stickers = Collections.unmodifiableList(stickers);
        this.flags = flags;
    }

    /**
     * The {@link MessageType} of the forwarded message.
     *
     * @return The {@link MessageType}
     */
    @NotNull
    public MessageType getType() {
        return type;
    }

    /**
     * The mentions of the forwarded message.
     *
     * <p>Mentions are only used for resolving users and roles in a forwarded message.
     * Mentions for cross-guild forwarded messages are usually not resolved.
     *
     * @return {@link Mentions}
     */
    @NotNull
    public Mentions getMentions() {
        return mentions;
    }

    /**
     * Whether the forwarded message was edited.
     *
     * <p>Since this is a snapshot, the edited timestamp is only relevant to the time it was forwarded.
     * If the message is edited after the fact, this is not updated.
     *
     * @return True, if the message was edited when it was forwarded
     */
    public boolean isEdited() {
        return editTime != null;
    }

    /**
     * The last time the forwarded message was edited before being forwarded.
     *
     * <p>Since this is a snapshot, the edited timestamp is only relevant to the time it was forwarded.
     * If the message is edited after the fact, this is not updated.
     *
     * @return {@link OffsetDateTime} when the message was edited (up to the time it was forwarded)
     */
    @Nullable
    public OffsetDateTime getTimeEdited() {
        return editTime;
    }

    /**
     * The raw content of the message, including markdown and mentions.
     *
     * @return The raw message content.
     */
    @NotNull
    public String getContentRaw() {
        return content;
    }

    /**
     * Invite codes found in the message content.
     *
     * @return The invite codes
     */
    @NotNull
    @Unmodifiable
    public List<String> getInvites() {
        if (invites != null) {
            return invites;
        }
        synchronized (mutex) {
            if (invites != null) {
                return invites;
            }
            invites = new ArrayList<>();
            Matcher m = INVITE_PATTERN.matcher(getContentRaw());
            while (m.find()) {
                invites.add(m.group(1));
            }
            return invites = Collections.unmodifiableList(invites);
        }
    }

    /**
     * Message attachments of the forwarded message.
     *
     * @return Immutable {@link List} of {@link Attachment}
     */
    @NotNull
    @Unmodifiable
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Message embeds of the forwarded message.
     *
     * @return Immutable {@link List} of {@link MessageEmbed}
     */
    @NotNull
    @Unmodifiable
    public List<MessageEmbed> getEmbeds() {
        return embeds;
    }

    /**
     * Components of the forwarded message.
     *
     * <p>Buttons and other interactive components are non-functional in forwarded messages.
     *
     * @return Immutable {@link List} of {@link MessageTopLevelComponentUnion}
     */
    @NotNull
    @Unmodifiable
    public List<MessageTopLevelComponentUnion> getComponents() {
        return components;
    }

    /**
     * A {@link MessageComponentTree} constructed from {@link #getComponents()}.
     *
     * @return {@link MessageComponentTree}
     */
    @NotNull
    public MessageComponentTree getComponentTree() {
        return MessageComponentTree.of(components);
    }

    /**
     * Stickers of the forwarded message.
     *
     * @return Immutable {@link List} of {@link StickerItem}
     */
    @NotNull
    @Unmodifiable
    public List<StickerItem> getStickers() {
        return stickers;
    }

    /**
     * The raw message flags of the forwarded message.
     *
     * @return The message flags
     */
    public long getFlagsRaw() {
        return flags;
    }

    /**
     * The message flags fo the forwarded message.
     *
     * @return {@link EnumSet} of {@link MessageFlag}
     */
    @NotNull
    public EnumSet<MessageFlag> getFlags() {
        return MessageFlag.fromBitField((int) getFlagsRaw());
    }
}
