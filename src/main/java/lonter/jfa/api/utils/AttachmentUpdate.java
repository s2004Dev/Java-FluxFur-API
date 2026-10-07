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

package lonter.jfa.api.utils;

import lonter.jfa.api.entities.ISnowflake;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;
import okhttp3.MultipartBody;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents existing message attachment.
 * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
 */
public class AttachmentUpdate implements AttachedFile, ISnowflake {
    private final long id;
    private final String name;

    protected AttachmentUpdate(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Creates an {@link AttachmentUpdate} with the given attachment id.
     * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
     *
     * @param  id
     *         The id of the attachment to retain
     *
     * @return {@link AttachmentUpdate}
     */
    @NotNull
    public static AttachmentUpdate fromAttachment(long id) {
        return new AttachmentUpdate(id, null);
    }

    /**
     * Creates an {@link AttachmentUpdate} with the given attachment id.
     * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
     *
     * @param  id
     *         The id of the attachment to retain
     *
     * @throws IllegalArgumentException
     *         If the id is not a valid snowflake
     *
     * @return {@link AttachmentUpdate}
     */
    @NotNull
    public static AttachmentUpdate fromAttachment(@NotNull String id) {
        return fromAttachment(MiscUtil.parseSnowflake(id));
    }

    /**
     * Creates an {@link AttachmentUpdate} with the given attachment.
     * <br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
     *
     * @param  attachment
     *         The attachment to retain
     *
     * @return {@link AttachmentUpdate}
     */
    @NotNull
    public static AttachmentUpdate fromAttachment(@NotNull Message.Attachment attachment) {
        Checks.notNull(attachment, "Attachment");
        return new AttachmentUpdate(attachment.getIdLong(), attachment.getFileName());
    }

    /**
     * The existing attachment filename.
     *
     * @return The filename, or {@code null} if not provided
     */
    @Nullable
    public String getName() {
        return name;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public void addPart(@NotNull MultipartBody.Builder builder, int index) {}

    @NotNull
    @Override
    public DataObject toAttachmentData(int index) {
        DataObject object = DataObject.empty().put("id", getId());
        if (name != null) {
            object.put("filename", name);
        }
        return object;
    }

    @Override
    public void close() {}

    @Override
    public void forceClose() {}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AttachmentUpdate)) {
            return false;
        }
        AttachmentUpdate that = (AttachmentUpdate) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        EntityString entityString = new EntityString("AttachedFile").setType("Attachment");
        if (name != null) {
            entityString.setName(name);
        }
        return entityString.toString();
    }
}
