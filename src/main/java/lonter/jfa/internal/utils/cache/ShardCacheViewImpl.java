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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import lonter.jfa.api.JFA;
import lonter.jfa.api.utils.ClosableIterator;
import lonter.jfa.api.utils.LockIterator;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.cache.CacheView;
import lonter.jfa.api.utils.cache.ShardCacheView;
import lonter.jfa.internal.utils.ChainedClosableIterator;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.UnlockHook;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;

public class ShardCacheViewImpl extends ReadWriteLockCache<JFA> implements ShardCacheView {
    protected static final JFA[] EMPTY_ARRAY = new JFA[0];
    protected final TIntObjectMap<JFA> elements;

    public ShardCacheViewImpl() {
        this.elements = new TIntObjectHashMap<>();
    }

    public ShardCacheViewImpl(int initialCapacity) {
        this.elements = new TIntObjectHashMap<>(initialCapacity);
    }

    public void clear() {
        try (UnlockHook hook = writeLock()) {
            elements.clear();
        }
    }

    public JFA remove(int shardId) {
        try (UnlockHook hook = writeLock()) {
            return elements.remove(shardId);
        }
    }

    public TIntObjectMap<JFA> getMap() {
        if (!lock.writeLock().isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot access map without holding write lock!");
        }
        return elements;
    }

    public TIntSet keySet() {
        try (UnlockHook hook = readLock()) {
            return new TIntHashSet(elements.keySet());
        }
    }

    @Override
    public void forEach(Consumer<? super JFA> action) {
        Objects.requireNonNull(action);
        try (UnlockHook hook = readLock()) {
            for (JFA shard : elements.valueCollection()) {
                action.accept(shard);
            }
        }
    }

    @NotNull
    @Override
    public List<JFA> asList() {
        if (isEmpty()) {
            return Collections.emptyList();
        }
        try (UnlockHook hook = readLock()) {
            List<JFA> list = getCachedList();
            if (list != null) {
                return list;
            }
            return cache(new ArrayList<>(elements.valueCollection()));
        }
    }

    @NotNull
    @Override
    public Set<JFA> asSet() {
        if (isEmpty()) {
            return Collections.emptySet();
        }
        try (UnlockHook hook = readLock()) {
            Set<JFA> set = getCachedSet();
            if (set != null) {
                return set;
            }
            return cache(new HashSet<>(elements.valueCollection()));
        }
    }

    @NotNull
    @Override
    public LockIterator<JFA> lockedIterator() {
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        MiscUtil.tryLock(readLock);
        try {
            Iterator<JFA> directIterator = elements.valueCollection().iterator();
            return new LockIterator<>(directIterator, readLock);
        } catch (Throwable t) {
            readLock.unlock();
            throw t;
        }
    }

    @Override
    public long size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @NotNull
    @Override
    public List<JFA> getElementsByName(@NotNull String name, boolean ignoreCase) {
        Checks.notEmpty(name, "Name");
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        try (UnlockHook hook = readLock()) {
            List<JFA> list = new LinkedList<>();
            for (JFA elem : elements.valueCollection()) {
                String elementName = elem.getShardInfo().getShardString();
                if (elementName != null) {
                    if (ignoreCase) {
                        if (elementName.equalsIgnoreCase(name)) {
                            list.add(elem);
                        }
                    } else {
                        if (elementName.equals(name)) {
                            list.add(elem);
                        }
                    }
                }
            }

            return list;
        }
    }

    @Override
    public Spliterator<JFA> spliterator() {
        try (UnlockHook hook = readLock()) {
            return Spliterators.spliterator(iterator(), size(), Spliterator.IMMUTABLE | Spliterator.NONNULL);
        }
    }

    @NotNull
    @Override
    public Stream<JFA> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @NotNull
    @Override
    public Stream<JFA> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    @NotNull
    @Override
    public Iterator<JFA> iterator() {
        try (UnlockHook hook = readLock()) {
            JFA[] arr = elements.values(EMPTY_ARRAY);
            return new ObjectArrayIterator<>(arr);
        }
    }

    @Override
    public JFA getElementById(int id) {
        try (UnlockHook hook = readLock()) {
            return this.elements.get(id);
        }
    }

    @Override
    public int hashCode() {
        try (UnlockHook hook = readLock()) {
            return elements.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShardCacheViewImpl)) {
            return false;
        }
        ShardCacheViewImpl view = (ShardCacheViewImpl) obj;
        try (UnlockHook hook = readLock();
                UnlockHook otherHook = view.readLock()) {
            return this.elements.equals(view.elements);
        }
    }

    @Override
    public String toString() {
        return asList().toString();
    }

    public static class UnifiedShardCacheViewImpl implements ShardCacheView {
        protected final Supplier<? extends Stream<? extends ShardCacheView>> generator;

        public UnifiedShardCacheViewImpl(Supplier<? extends Stream<? extends ShardCacheView>> generator) {
            this.generator = generator;
        }

        @Override
        public long size() {
            return distinctStream().mapToLong(CacheView::size).sum();
        }

        @Override
        public boolean isEmpty() {
            return generator.get().allMatch(CacheView::isEmpty);
        }

        @NotNull
        @Override
        public List<JFA> asList() {
            List<JFA> list = new ArrayList<>();
            stream().forEach(list::add);
            return Collections.unmodifiableList(list);
        }

        @NotNull
        @Override
        public Set<JFA> asSet() {
            Set<JFA> set = new HashSet<>();
            generator.get().flatMap(CacheView::stream).forEach(set::add);
            return Collections.unmodifiableSet(set);
        }

        @NotNull
        @Override
        public ClosableIterator<JFA> lockedIterator() {
            Iterator<? extends ShardCacheView> gen = this.generator.get().iterator();
            return new ChainedClosableIterator<>(gen);
        }

        @NotNull
        @Override
        public List<JFA> getElementsByName(@NotNull String name, boolean ignoreCase) {
            return distinctStream()
                    .flatMap(view -> view.getElementsByName(name, ignoreCase).stream())
                    .collect(Helpers.toUnmodifiableList());
        }

        @Override
        public JFA getElementById(int id) {
            return generator
                    .get()
                    .map(view -> view.getElementById(id))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        @NotNull
        @Override
        public Stream<JFA> stream() {
            return generator.get().flatMap(CacheView::stream).distinct();
        }

        @NotNull
        @Override
        public Stream<JFA> parallelStream() {
            return generator.get().flatMap(CacheView::parallelStream).distinct();
        }

        @NotNull
        @Override
        public Iterator<JFA> iterator() {
            return stream().iterator();
        }

        protected Stream<? extends ShardCacheView> distinctStream() {
            return generator.get().distinct();
        }
    }
}
