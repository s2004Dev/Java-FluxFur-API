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

package lonter.jfa.api.entities;

import lonter.jfa.internal.utils.EntityString;

import org.jetbrains.annotations.NotNull;

/**
 * Metadata for the vanity invite of a guild
 */
public record VanityInvite(String code, int uses) {
    public VanityInvite(@NotNull String code, int uses) {
        this.code = code;
        this.uses = uses;
    }

    /**
     * The invite code used for the invite url.
     *
     * @return The code
     */
    @Override
    @NotNull
    public String code() {
        return code;
    }

    /**
     * How many times this invite has been used.
     * <br>This is reset after the invite is changed or removed.
     *
     * @return The invite uses
     */
    @Override
    public int uses() {
        return uses;
    }

    /**
     * The invite url.
     *
     * @return The invite url
     */
    @NotNull
    public String getUrl() {
        return "https://fluxer.gg/" + code();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VanityInvite(String code1, int uses1))) {
            return false;
        }
      return uses == uses1 && code.equals(code1);
    }

    @Override
    public @NotNull String toString() {
        return new EntityString(this).addMetadata("code", code).toString();
    }
}
