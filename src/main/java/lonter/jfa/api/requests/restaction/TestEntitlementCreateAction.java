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

package lonter.jfa.api.requests.restaction;

import lonter.jfa.api.entities.Entitlement;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.utils.MiscUtil;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Extension of {@link lonter.jfa.api.requests.RestAction RestAction} specifically
 * designed to create a {@link Entitlement Entitlement}.
 * This extension allows setting properties before executing the action.
 *
 * @see    lonter.jfa.api.JFA
 * @see    lonter.jfa.api.JFA#createTestEntitlement(long, long, OwnerType)
 */
public interface TestEntitlementCreateAction extends RestAction<Entitlement> {

    /**
     * Set the SKU's id to create the entitlement in
     *
     * @param skuId
     *        The id of the SKU
     *
     * @throws IllegalArgumentException
     *         If the provided skuId is not a valid snowflake
     *
     * @return The current {@link TestEntitlementCreateAction} for chaining convenience
     */
    @CheckReturnValue
    @NotNull
    default TestEntitlementCreateAction setSkuId(@NotNull String skuId) {
        return setSkuId(MiscUtil.parseSnowflake(skuId));
    }

    /**
     * Set the SKU's id to create the entitlement in
     *
     * @param skuId
     *        The id of the SKU
     *
     * @return The current {@link TestEntitlementCreateAction} for chaining convenience
     */
    @CheckReturnValue
    @NotNull
    TestEntitlementCreateAction setSkuId(long skuId);

    /**
     * Set the owner's id to create the entitlement for
     *
     * @param ownerId
     *        The id of the owner - either guild id or user id
     *
     * @throws IllegalArgumentException
     *         If the provided ownerId is not a valid snowflake
     *
     * @return The current {@link TestEntitlementCreateAction} for chaining convenience
     */
    @CheckReturnValue
    @NotNull
    default TestEntitlementCreateAction setOwnerId(@NotNull String ownerId) {
        return setOwnerId(MiscUtil.parseSnowflake(ownerId));
    }

    /**
     * Set the owner's id to create the entitlement for
     *
     * @param ownerId
     *        The id of the owner - either guild id or user id
     *
     * @return The current {@link TestEntitlementCreateAction} for chaining convenience
     */
    @CheckReturnValue
    @NotNull
    TestEntitlementCreateAction setOwnerId(long ownerId);

    /**
     * Set the owner type to create the entitlement for
     *
     * @param type
     *        The type of the owner
     *
     * @throws IllegalArgumentException
     *         If the provided type is null
     *
     * @return The current {@link TestEntitlementCreateAction} for chaining convenience
     */
    @CheckReturnValue
    @NotNull
    TestEntitlementCreateAction setOwnerType(@NotNull OwnerType type);

    /**
     * The type of the owner for the entitlement
     */
    enum OwnerType {
        GUILD_SUBSCRIPTION(1),
        USER_SUBSCRIPTION(2);

        private final int key;

        OwnerType(int key) {
            this.key = key;
        }

        /**
         * The Fluxer defined id key for this OwnerType.
         *
         * @return the id key.
         */
        public int getKey() {
            return key;
        }
    }
}
