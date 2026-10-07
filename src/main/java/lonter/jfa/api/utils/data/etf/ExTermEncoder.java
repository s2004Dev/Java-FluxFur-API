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

package lonter.jfa.api.utils.data.etf;

import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.SerializableData;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lonter.jfa.api.utils.data.etf.ExTermTag.*;

/**
 * Encodes an object into a binary ETF representation.
 *
 * @see #pack(Object)
 */
public class ExTermEncoder {
    /**
     * Encodes the provided object into an ETF buffer.
     *
     * <p><b>The mapping is as follows:</b><br>
     * <ul>
     *     <li>{@code String -> Binary}</li>
     *     <li>{@code Map -> Map}</li>
     *     <li>{@code Collection -> List | NIL}</li>
     *     <li>{@code Byte -> Small Int}</li>
     *     <li>{@code Integer, Short -> Int | Small Int}</li>
     *     <li>{@code Long -> Small BigInt | Int | Small Int}</li>
     *     <li>{@code Float, Double -> New Float}</li>
     *     <li>{@code Boolean -> Atom(Boolean)}</li>
     *     <li>{@code null -> Atom("nil")}</li>
     * </ul>
     *
     * @param  data
     *         The object to encode
     *
     * @throws UnsupportedOperationException
     *         If there is no type mapping for the provided object
     *
     * @return {@link ByteBuffer} with the encoded ETF term
     */
    @NotNull
    public static ByteBuffer pack(@Nullable Object data) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put((byte) 131);

        ByteBuffer packed = pack(buffer, data);
        // This cast prevents issues with backwards compatibility in the ABI
        // (java 11 made breaking changes)
        ((Buffer) packed).flip();
        return packed;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static ByteBuffer pack(@NotNull ByteBuffer buffer, @Nullable Object value) {
        if (value instanceof String) {
            return packBinary(buffer, (String) value);
        }
        if (value instanceof Map) {
            return packMap(buffer, (Map<String, Object>) value);
        }
        if (value instanceof SerializableData) {
            return packMap(buffer, ((SerializableData) value).toData().toMap());
        }
        if (value instanceof Collection) {
            return packList(buffer, (Collection<Object>) value);
        }
        if (value instanceof DataArray) {
            return packList(buffer, ((DataArray) value).toList());
        }
        if (value instanceof Byte) {
            return packSmallInt(buffer, (byte) value);
        }
        if (value instanceof Integer || value instanceof Short) {
            return packInt(buffer, ((Number) value).intValue());
        }
        if (value instanceof Long) {
            return packLong(buffer, (long) value);
        }
        if (value instanceof Float || value instanceof Double) {
            return packFloat(buffer, ((Number) value).doubleValue());
        }
        if (value instanceof Boolean) {
            return packAtom(buffer, String.valueOf(value));
        }
        if (value == null) {
            return packAtom(buffer, "nil");
        }
        if (value instanceof long[]) {
            return packArray(buffer, (long[]) value);
        }
        if (value instanceof int[]) {
            return packArray(buffer, (int[]) value);
        }
        if (value instanceof short[]) {
            return packArray(buffer, (short[]) value);
        }
        if (value instanceof byte[]) {
            return packArray(buffer, (byte[]) value);
        }
        // omitting other array types because we don't use them anywhere
        if (value instanceof Object[]) {
            return packList(buffer, Arrays.asList((Object[]) value));
        }

        throw new UnsupportedOperationException(
                "Cannot pack value of type " + value.getClass().getName());
    }

    @NotNull
    private static ByteBuffer realloc(@NotNull ByteBuffer buffer, int length) {
        if (buffer.remaining() >= length) {
            return buffer;
        }

        ByteBuffer allocated = ByteBuffer.allocate((buffer.position() + length) << 1);
        // This cast prevents issues with backwards compatibility in the ABI
        // (java 11 made breaking changes)
        ((Buffer) buffer).flip();
        allocated.put(buffer);
        return allocated;
    }

    @NotNull
    private static ByteBuffer packMap(@NotNull ByteBuffer buffer, @NotNull Map<String, Object> data) {
        buffer = realloc(buffer, data.size() + 5);
        buffer.put(MAP);
        buffer.putInt(data.size());

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            buffer = packBinary(buffer, entry.getKey());
            buffer = pack(buffer, entry.getValue());
        }

        return buffer;
    }

    @NotNull
    private static ByteBuffer packList(@NotNull ByteBuffer buffer, @NotNull Collection<Object> data) {
        if (data.isEmpty()) {
            // NIL is for empty lists
            return packNil(buffer);
        }

        buffer = realloc(buffer, data.size() + 6);
        buffer.put(LIST);
        buffer.putInt(data.size());
        for (Object element : data) {
            buffer = pack(buffer, element);
        }
        return packNil(buffer);
    }

    @NotNull
    private static ByteBuffer packBinary(@NotNull ByteBuffer buffer, @NotNull String value) {
        byte[] encoded = value.getBytes(StandardCharsets.UTF_8);
        buffer = realloc(buffer, encoded.length * 4 + 5);
        buffer.put(BINARY);
        buffer.putInt(encoded.length);
        buffer.put(encoded);
        return buffer;
    }

    @NotNull
    private static ByteBuffer packSmallInt(@NotNull ByteBuffer buffer, byte value) {
        buffer = realloc(buffer, 2);
        buffer.put(SMALL_INT);
        buffer.put(value);
        return buffer;
    }

    @NotNull
    private static ByteBuffer packInt(@NotNull ByteBuffer buffer, int value) {
        if (countBytes(value) <= 1 && value >= 0) {
            return packSmallInt(buffer, (byte) value);
        }
        buffer = realloc(buffer, 5);
        buffer.put(INT);
        buffer.putInt(value);
        return buffer;
    }

    @NotNull
    private static ByteBuffer packLong(@NotNull ByteBuffer buffer, long value) {
        byte bytes = countBytes(value);
        if (bytes <= 1) { // Use optimized small int encoding
            return packSmallInt(buffer, (byte) value);
        }
        if (bytes <= 4 && value >= 0) {
            // Use int to encode it
            buffer = realloc(buffer, 5);
            buffer.put(INT);
            buffer.putInt((int) value);
            return buffer;
        }

        buffer = realloc(buffer, 3 + bytes);
        buffer.put(SMALL_BIGINT);
        buffer.put(bytes);
        // We only use "unsigned" value so the sign is always positive
        buffer.put((byte) 0);
        while (value > 0) {
            buffer.put((byte) value);
            value >>>= 8;
        }

        return buffer;
    }

    @NotNull
    private static ByteBuffer packFloat(@NotNull ByteBuffer buffer, double value) {
        buffer = realloc(buffer, 9);
        buffer.put(NEW_FLOAT);
        buffer.putDouble(value);
        return buffer;
    }

    @NotNull
    private static ByteBuffer packAtom(@NotNull ByteBuffer buffer, String value) {
        byte[] array = value.getBytes(StandardCharsets.ISO_8859_1);
        buffer = realloc(buffer, array.length + 3);
        buffer.put(ATOM);
        buffer.putShort((short) array.length);
        buffer.put(array);
        return buffer;
    }

    @NotNull
    private static ByteBuffer packArray(@NotNull ByteBuffer buffer, @NotNull long[] array) {
        if (array.length == 0) {
            return packNil(buffer);
        }

        buffer = realloc(buffer, array.length * 8 + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (long it : array) {
            buffer = packLong(buffer, it);
        }
        return packNil(buffer);
    }

    @NotNull
    private static ByteBuffer packArray(@NotNull ByteBuffer buffer, @NotNull int[] array) {
        if (array.length == 0) {
            return packNil(buffer);
        }

        buffer = realloc(buffer, array.length * 4 + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (int it : array) {
            buffer = packInt(buffer, it);
        }
        return packNil(buffer);
    }

    @NotNull
    private static ByteBuffer packArray(@NotNull ByteBuffer buffer, @NotNull short[] array) {
        if (array.length == 0) {
            return packNil(buffer);
        }

        buffer = realloc(buffer, array.length * 2 + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (short it : array) {
            buffer = packInt(buffer, it);
        }
        return packNil(buffer);
    }

    @NotNull
    private static ByteBuffer packArray(@NotNull ByteBuffer buffer, @NotNull byte[] array) {
        if (array.length == 0) {
            return packNil(buffer);
        }

        buffer = realloc(buffer, array.length + 6);
        buffer.put(LIST);
        buffer.putInt(array.length);
        for (byte it : array) {
            buffer = packSmallInt(buffer, it);
        }
        return packNil(buffer);
    }

    @NotNull
    private static ByteBuffer packNil(@NotNull ByteBuffer buffer) {
        buffer = realloc(buffer, 1);
        buffer.put(NIL);
        return buffer;
    }

    private static byte countBytes(long value) {
        int leadingZeros = Long.numberOfLeadingZeros(value);
        return (byte) Math.ceil((64 - leadingZeros) / 8.);
    }
}
