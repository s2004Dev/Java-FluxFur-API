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

import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.utils.ClosableIterator;
import lonter.jfa.api.utils.cache.ChannelCacheView;
import lonter.jfa.internal.utils.ChainedClosableIterator;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnifiedChannelCacheView<C extends Channel> implements ChannelCacheView<C> {
    private final Supplier<Stream<ChannelCacheView<C>>> supplier;

    public UnifiedChannelCacheView(Supplier<Stream<ChannelCacheView<C>>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void forEach(Consumer<? super C> action) {
        Objects.requireNonNull(action, "Consumer");
        try (ClosableIterator<C> iterator = lockedIterator()) {
            while (iterator.hasNext()) {
                action.accept(iterator.next());
            }
        }
    }

    @NotNull
    @Override
    public List<C> asList() {
        return stream().collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public Set<C> asSet() {
        return stream().collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    @NotNull
    @Override
    public ClosableIterator<C> lockedIterator() {
        return new ChainedClosableIterator<>(supplier.get().iterator());
    }

    @Override
    public long size() {
        return supplier.get().mapToLong(ChannelCacheView::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return supplier.get().allMatch(ChannelCacheView::isEmpty);
    }

    @NotNull
    @Override
    public List<C> getElementsByName(@NotNull String name, boolean ignoreCase) {
        return supplier.get()
                .flatMap(view -> view.getElementsByName(name, ignoreCase).stream())
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public Stream<C> stream() {
        return supplier.get().flatMap(ChannelCacheView::stream);
    }

    @NotNull
    @Override
    public Stream<C> parallelStream() {
        return supplier.get().parallel().flatMap(ChannelCacheView::parallelStream);
    }

    @NotNull
    @Override
    public <T extends C> ChannelCacheView<T> ofType(@NotNull Class<T> type) {
        Checks.notNull(type, "Type");
        return new UnifiedChannelCacheView<>(() -> supplier.get().map(view -> view.ofType(type)));
    }

    @Nullable
    @Override
    public C getElementById(@NotNull ChannelType type, long id) {
        return supplier.get()
                .map(view -> view.getElementById(type, id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @Override
    public C getElementById(long id) {
        return supplier.get()
                .map(view -> view.getElementById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @NotNull
    @Override
    public Iterator<C> iterator() {
        return stream().iterator();
    }
}
