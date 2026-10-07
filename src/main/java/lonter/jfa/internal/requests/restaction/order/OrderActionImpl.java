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

package lonter.jfa.internal.requests.restaction.order;

import lonter.jfa.api.JFA;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.order.OrderAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

public abstract class OrderActionImpl<T, M extends OrderAction<T, M>> extends RestActionImpl<Void>
        implements OrderAction<T, M> {
    protected final List<T> orderList;
    protected final boolean ascendingOrder;
    protected int selectedPosition = -1;

    /**
     * Creates a new OrderAction instance
     *
     * @param api
     *        JFA instance which is associated with the entities contained
     *        in the order list
     * @param route
     *        The {@link lonter.jfa.api.requests.Route.CompiledRoute CompiledRoute}
     *        which is provided to the {@link RestActionImpl#RestActionImpl(JFA, Route.CompiledRoute, okhttp3.RequestBody) RestAction Constructor}
     */
    public OrderActionImpl(JFA api, Route.CompiledRoute route) {
        this(api, true, route);
    }

    /**
     * Creates a new OrderAction instance
     *
     * @param api
     *        JFA instance which is associated with the entities contained
     *        in the order list
     * @param ascendingOrder
     *        Whether or not the order of items should be ascending
     * @param route
     *        The {@link lonter.jfa.api.requests.Route.CompiledRoute CompiledRoute}
     *        which is provided to the {@link RestActionImpl#RestActionImpl(JFA, Route.CompiledRoute, okhttp3.RequestBody) RestAction Constructor}
     */
    public OrderActionImpl(JFA api, boolean ascendingOrder, Route.CompiledRoute route) {
        super(api, route);
        this.orderList = new ArrayList<>();
        this.ascendingOrder = ascendingOrder;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M setCheck(BooleanSupplier checks) {
        return (M) super.setCheck(checks);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M timeout(long timeout, @NotNull TimeUnit unit) {
        return (M) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M deadline(long timestamp) {
        return (M) super.deadline(timestamp);
    }

    @Override
    public boolean isAscendingOrder() {
        return ascendingOrder;
    }

    @NotNull
    @Override
    public List<T> getCurrentOrder() {
        return Collections.unmodifiableList(orderList);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M selectPosition(int selectedPosition) {
        Checks.notNegative(selectedPosition, "Provided selectedPosition");
        Checks.check(
                selectedPosition < orderList.size(),
                "Provided selectedPosition is too big and is out of bounds. selectedPosition: " + selectedPosition);

        this.selectedPosition = selectedPosition;

        return (M) this;
    }

    @NotNull
    @Override
    public M selectPosition(@NotNull T selectedEntity) {
        Checks.notNull(selectedEntity, "Channel");
        validateInput(selectedEntity);

        return selectPosition(orderList.indexOf(selectedEntity));
    }

    @Override
    public int getSelectedPosition() {
        return selectedPosition;
    }

    @NotNull
    @Override
    public T getSelectedEntity() {
        if (selectedPosition == -1) {
            throw new IllegalStateException("No position has been selected yet");
        }

        return orderList.get(selectedPosition);
    }

    @NotNull
    @Override
    public M moveUp(int amount) {
        Checks.notNegative(amount, "Provided amount");
        if (selectedPosition == -1) {
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");
        }
        if (ascendingOrder) {
            Checks.check(
                    selectedPosition - amount >= 0,
                    "Amount provided to move up is too large and would be out of bounds."
                            + "Selected position: " + selectedPosition + " Amount: " + amount
                            + " Largest Position: " + orderList.size());
        } else {
            Checks.check(
                    selectedPosition + amount < orderList.size(),
                    "Amount provided to move up is too large and would be out of bounds."
                            + "Selected position: " + selectedPosition + " Amount: " + amount
                            + " Largest Position: " + orderList.size());
        }

        if (ascendingOrder) {
            return moveTo(selectedPosition - amount);
        } else {
            return moveTo(selectedPosition + amount);
        }
    }

    @NotNull
    @Override
    public M moveDown(int amount) {
        Checks.notNegative(amount, "Provided amount");
        if (selectedPosition == -1) {
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");
        }

        if (ascendingOrder) {
            Checks.check(
                    selectedPosition + amount < orderList.size(),
                    "Amount provided to move down is too large and would be out of bounds. "
                            + "Selected position: " + selectedPosition + " Amount: " + amount
                            + " Largest Position: " + orderList.size());
        } else {
            Checks.check(
                    selectedPosition - amount >= 0,
                    "Amount provided to move down is too large and would be out of bounds. "
                            + "Selected position: " + selectedPosition + " Amount: " + amount
                            + " Largest Position: " + orderList.size());
        }

        if (ascendingOrder) {
            return moveTo(selectedPosition + amount);
        } else {
            return moveTo(selectedPosition - amount);
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M moveTo(int position) {
        Checks.notNegative(position, "Provided position");
        Checks.check(position < orderList.size(), "Provided position is too big and is out of bounds.");
        if (selectedPosition == -1) {
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");
        }

        T selectedItem = orderList.remove(selectedPosition);
        orderList.add(position, selectedItem);
        selectedPosition = position;

        return (M) this;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M moveBelow(@NotNull T other) {
        validateInput(other);
        int index = getCurrentOrder().indexOf(other);
        moveTo(index);
        if (isAscendingOrder()) {
            return moveDown(1);
        }
        return (M) this;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M moveAbove(@NotNull T other) {
        validateInput(other);
        int index = getCurrentOrder().indexOf(other);
        moveTo(index);
        if (!isAscendingOrder()) {
            return moveUp(1);
        }
        return (M) this;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M swapPosition(int swapPosition) {
        Checks.notNegative(swapPosition, "Provided swapPosition");
        Checks.check(
                swapPosition < orderList.size(),
                "Provided swapPosition is too big and is out of bounds. swapPosition: " + swapPosition);
        if (selectedPosition == -1) {
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");
        }

        T selectedItem = orderList.get(selectedPosition);
        T swapItem = orderList.get(swapPosition);
        orderList.set(swapPosition, selectedItem);
        orderList.set(selectedPosition, swapItem);
        selectedPosition = swapPosition;

        return (M) this;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M swapPosition(@NotNull T swapEntity) {
        Checks.notNull(swapEntity, "Provided swapEntity");
        validateInput(swapEntity);

        return swapPosition(orderList.indexOf(swapEntity));
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M reverseOrder() {
        Collections.reverse(this.orderList);
        return (M) this;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M shuffleOrder() {
        Collections.shuffle(this.orderList);
        return (M) this;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public M sortOrder(@NotNull Comparator<T> comparator) {
        Checks.notNull(comparator, "Provided comparator");

        this.orderList.sort(comparator);
        return (M) this;
    }

    protected abstract void validateInput(T entity);
}
