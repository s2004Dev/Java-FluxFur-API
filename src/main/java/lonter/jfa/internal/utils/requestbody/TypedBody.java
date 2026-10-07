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

package lonter.jfa.internal.utils.requestbody;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TypedBody<T extends TypedBody<T>> extends RequestBody {
    protected final MediaType type;

    protected TypedBody(MediaType type) {
        this.type = type;
    }

    @NotNull
    public abstract T withType(@NotNull MediaType newType);

    @Nullable
    @Override
    public MediaType contentType() {
        return type;
    }
}
