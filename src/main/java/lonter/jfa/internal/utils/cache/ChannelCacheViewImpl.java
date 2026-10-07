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

package lonter.jfa.internal.utils.cache;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.utils.ClosableIterator;
import lonter.jfa.api.utils.LockIterator;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.cache.ChannelCacheView;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.UnlockHook;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChannelCacheViewImpl<T extends Channel> extends ReadWriteLockCache<T> implements ChannelCacheView<T> {
    protected final EnumMap<ChannelType, TLongObjectMap<T>> caches = new EnumMap<>(ChannelType.class);

    public ChannelCacheViewImpl(Class<T> type) {
        for (ChannelType channelType : ChannelType.values()) {
            channelType = normalizeKey(channelType);
            Class<? extends Channel> clazz = channelType.getInterface();
            if (channelType != ChannelType.UNKNOWN && type.isAssignableFrom(clazz)) {
                caches.put(channelType, new TLongObjectHashMap<>());
            }
        }
    }

    // Store all threads under the same channel type,
    // makes it easier because the interface is shared
    protected ChannelType normalizeKey(ChannelType type) {
        return type.isThread() ? ChannelType.GUILD_PUBLIC_THREAD : type;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <C extends T> TLongObjectMap<C> getMap(@NotNull ChannelType type) {
        return (TLongObjectMap<C>) caches.get(normalizeKey(type));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <C extends T> C put(C element) {
        try (UnlockHook hook = writeLock()) {
            return (C) getMap(element.getType()).put(element.getIdLong(), element);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <C extends T> C remove(ChannelType type, long id) {
        try (UnlockHook hook = writeLock()) {
            T removed = getMap(type).remove(id);
            return (C) removed;
        }
    }

    public <C extends T> C remove(C channel) {
        return remove(channel.getType(), channel.getIdLong());
    }

    public <C extends T> void removeIf(Class<C> typeFilter, Predicate<? super C> predicate) {
        try (UnlockHook hook = writeLock()) {
            ofType(typeFilter).removeIf(predicate);
        }
    }

    public void clear() {
        try (UnlockHook hook = writeLock()) {
            caches.values().forEach(TLongObjectMap::clear);
        }
    }

    @NotNull
    @Override
    public <C extends T> FilteredCacheView<C> ofType(@NotNull Class<C> type) {
        return new FilteredCacheView<>(type);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        try (UnlockHook hook = readLock()) {
            for (TLongObjectMap<T> cache : caches.values()) {
                cache.valueCollection().forEach(action);
            }
        }
    }

    @NotNull
    @Override
    public List<T> asList() {
        List<T> list = getCachedList();
        if (list == null) {
            List<T> newList = applyStream(stream -> stream.collect(Collectors.toList()));
            list = cache(newList);
        }
        return list;
    }

    @NotNull
    @Override
    public Set<T> asSet() {
        Set<T> set = getCachedSet();
        if (set == null) {
            Set<T> newSet = applyStream(stream -> stream.collect(Collectors.toSet()));
            set = cache(newSet);
        }
        return set;
    }

    @NotNull
    @Override
    public ClosableIterator<T> lockedIterator() {
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        MiscUtil.tryLock(readLock);
        try {
            Iterator<? extends T> directIterator = caches.values().stream()
                    .flatMap(map -> map.valueCollection().stream())
                    .iterator();
            return new LockIterator<>(directIterator, readLock);
        } catch (Throwable t) {
            readLock.unlock();
            throw t;
        }
    }

    @Override
    public long size() {
        try (UnlockHook hook = readLock()) {
            return caches.values().stream().mapToLong(TLongObjectMap::size).sum();
        }
    }

    @Override
    public boolean isEmpty() {
        try (UnlockHook hook = readLock()) {
            return caches.values().stream().allMatch(TLongObjectMap::isEmpty);
        }
    }

    @NotNull
    @Override
    public List<T> getElementsByName(@NotNull String name, boolean ignoreCase) {
        Checks.notEmpty(name, "Name");
        return applyStream(stream -> stream.filter((channel) -> Helpers.equals(channel.getName(), name, ignoreCase))
                .collect(Helpers.toUnmodifiableList()));
    }

    @NotNull
    @Override
    public Stream<T> stream() {
        return this.asList().stream();
    }

    @NotNull
    @Override
    public Stream<T> parallelStream() {
        return this.asList().parallelStream();
    }

    @Nullable
    @Override
    public T getElementById(long id) {
        try (UnlockHook hook = readLock()) {
            for (TLongObjectMap<? extends T> cache : caches.values()) {
                T element = cache.get(id);
                if (element != null) {
                    return element;
                }
            }
            return null;
        }
    }

    public T getElementById(@NotNull ChannelType type, long id) {
        Checks.notNull(type, "ChannelType");
        try (UnlockHook hook = readLock()) {
            TLongObjectMap<T> map = getMap(type);
            return map == null ? null : map.get(id);
        }
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public class FilteredCacheView<C extends T> implements ChannelCacheView<C> {
        protected final Class<C> type;
        protected final List<TLongObjectMap<C>> filteredMaps;

        @SuppressWarnings("unchecked")
        protected FilteredCacheView(Class<C> type) {
            Checks.notNull(type, "Type");
            checkChannelInterface(type);

            this.type = type;

            this.filteredMaps = caches.entrySet().stream()
                    .filter(entry -> entry.getKey() != null
                            && type.isAssignableFrom(entry.getKey().getInterface()))
                    .map(entry -> (TLongObjectMap<C>) entry.getValue())
                    .collect(Collectors.toList());
        }

        private void checkChannelInterface(Class<C> type) {
            boolean isValidInterfaceType = false;
            for (ChannelType channelType : ChannelType.values()) {
                isValidInterfaceType |= type.isAssignableFrom(channelType.getInterface());
            }
            Checks.check(isValidInterfaceType, "Type %s is not a valid channel interface", type.getSimpleName());
        }

        protected void removeIf(Predicate<? super C> filter) {
            this.filteredMaps.forEach(map -> map.valueCollection().removeIf(filter));
        }

        @NotNull
        @Override
        public List<C> asList() {
            return applyStream(stream -> stream.collect(Helpers.toUnmodifiableList()));
        }

        @NotNull
        @Override
        public Set<C> asSet() {
            return applyStream(stream ->
                    stream.collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet)));
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public ClosableIterator<C> lockedIterator() {
            ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
            MiscUtil.tryLock(readLock);
            try {
                Iterator<? extends C> directIterator = filteredMaps.stream()
                        .flatMap(map -> map.valueCollection().stream())
                        .iterator();
                return new LockIterator<>(directIterator, readLock);
            } catch (Throwable t) {
                readLock.unlock();
                throw t;
            }
        }

        @Override
        public long size() {
            try (UnlockHook hook = readLock()) {
                return filteredMaps.stream().mapToLong(TLongObjectMap::size).sum();
            }
        }

        @Override
        public boolean isEmpty() {
            try (UnlockHook hook = readLock()) {
                return filteredMaps.stream().allMatch(TLongObjectMap::isEmpty);
            }
        }

        @NotNull
        @Override
        public List<C> getElementsByName(@NotNull String name, boolean ignoreCase) {
            Checks.notEmpty(name, "Name");
            return applyStream(stream -> stream.filter(channel -> Helpers.equals(channel.getName(), name, ignoreCase))
                    .collect(Helpers.toUnmodifiableList()));
        }

        @NotNull
        @Override
        public Stream<C> stream() {
            return asList().stream();
        }

        @NotNull
        @Override
        public Stream<C> parallelStream() {
            return asList().parallelStream();
        }

        @NotNull
        @Override
        public <C1 extends C> ChannelCacheView<C1> ofType(@NotNull Class<C1> type) {
            return ChannelCacheViewImpl.this.ofType(type);
        }

        @Nullable
        @Override
        public C getElementById(@NotNull ChannelType type, long id) {
            T channel = ChannelCacheViewImpl.this.getElementById(type, id);
            return this.type.isInstance(channel) ? this.type.cast(channel) : null;
        }

        @Nullable
        @Override
        public C getElementById(long id) {
            try (UnlockHook hook = readLock()) {
                return filteredMaps.stream()
                        .map(it -> it.get(id))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }
        }

        @NotNull
        @Override
        public Iterator<C> iterator() {
            return asList().iterator();
        }
    }
}
