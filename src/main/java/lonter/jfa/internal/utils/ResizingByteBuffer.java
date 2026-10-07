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

package lonter.jfa.internal.utils;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.NotNull;

public class ResizingByteBuffer {
    private ByteBuffer buffer;

    public ResizingByteBuffer(@NotNull ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @NotNull
    public ByteBuffer buffer() {
        return buffer;
    }

    @NotNull
    public ResizingByteBuffer prepareWrite(int capacity) {
        if (this.buffer.capacity() < capacity) {
            this.buffer = IOUtil.allocateLike(this.buffer, (int) (1.25 * capacity));
        } else {
            this.buffer.clear();
        }

        return this;
    }

    @NotNull
    public ResizingByteBuffer ensureRemaining(int capacity) {
        if (this.buffer.remaining() < capacity) {
            ByteBuffer newBuffer = IOUtil.allocateLike(this.buffer, (int) (1.25 * (this.buffer.position() + capacity)));
            this.buffer.flip();
            newBuffer.put(this.buffer);
            this.buffer = newBuffer;
        }

        return this;
    }

    @NotNull
    public ResizingByteBuffer replace(@NotNull ByteBuffer data) {
        this.buffer = IOUtil.replace(this.buffer, data);
        return this;
    }

    @NotNull
    public ResizingByteBuffer clear() {
        this.buffer.clear();
        return this;
    }
}
