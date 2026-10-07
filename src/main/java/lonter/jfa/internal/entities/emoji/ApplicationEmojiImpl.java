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

package lonter.jfa.internal.entities.emoji;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.emoji.*;
import lonter.jfa.api.managers.ApplicationEmojiManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.managers.ApplicationEmojiManagerImpl;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.EntityString;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ApplicationEmojiImpl implements ApplicationEmoji, EmojiUnion {
    private final long id;
    private final JFAImpl api;
    private final User owner;

    boolean animated = false;
    private String name;

    public ApplicationEmojiImpl(long id, JFAImpl api, User owner) {
        this.id = id;
        this.api = api;
        this.owner = owner;
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.CUSTOM;
    }

    @NotNull
    @Override
    public String getAsReactionCode() {
        return name + ":" + id;
    }

    @NotNull
    @Override
    public DataObject toData() {
        return DataObject.empty().put("name", name).put("animated", animated).put("id", id);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @Nullable
    @Override
    public User getOwner() {
        return owner;
    }

    @NotNull
    @Override
    public ApplicationEmojiManager getManager() {
        return new ApplicationEmojiManagerImpl(this);
    }

    @Override
    public boolean isAnimated() {
        return animated;
    }

    @NotNull
    @Override
    public RestAction<Void> delete() {
        Route.CompiledRoute route = Route.Applications.DELETE_APPLICATION_EMOJI.compile(
                getJFA().getSelfUser().getApplicationId(), getId());
        return new RestActionImpl<>(getJFA(), route);
    }

    // -- Setters --

    public ApplicationEmojiImpl setName(String name) {
        this.name = name;
        return this;
    }

    public ApplicationEmojiImpl setAnimated(boolean animated) {
        this.animated = animated;
        return this;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ApplicationEmojiImpl)) {
            return false;
        }

        ApplicationEmojiImpl other = (ApplicationEmojiImpl) obj;
        return this.id == other.getIdLong();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return new EntityString(this).setName(name).toString();
    }

    @NotNull
    @Override
    public UnicodeEmoji asUnicode() {
        throw new IllegalStateException("Cannot convert ApplicationEmoji to UnicodeEmoji!");
    }

    @NotNull
    @Override
    public CustomEmoji asCustom() {
        return this;
    }

    @NotNull
    @Override
    public RichCustomEmoji asRich() {
        throw new IllegalStateException("Cannot convert ApplicationEmoji to RichCustomEmoji!");
    }

    @NotNull
    @Override
    public ApplicationEmoji asApplication() {
        return this;
    }
}
