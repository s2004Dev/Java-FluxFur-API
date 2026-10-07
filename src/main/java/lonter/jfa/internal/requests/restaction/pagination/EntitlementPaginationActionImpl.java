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

package lonter.jfa.internal.requests.restaction.pagination;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Entitlement;
import lonter.jfa.api.entities.UserSnowflake;
import lonter.jfa.api.exceptions.ParsingException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.pagination.EntitlementPaginationAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.utils.Checks;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitlementPaginationActionImpl extends PaginationActionImpl<Entitlement, EntitlementPaginationAction>
        implements EntitlementPaginationAction {
    protected List<String> skuIds;
    protected long guildId;
    protected long userId;
    protected boolean excludeEnded;

    public EntitlementPaginationActionImpl(JFA api) {
        super(api, Route.Applications.GET_ENTITLEMENTS.compile(api.getSelfUser().getApplicationId()), 1, 100, 100);
        this.skuIds = new ArrayList<>();
        this.guildId = 0;
        this.userId = 0;
    }

    @NotNull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders() {
        return EnumSet.of(PaginationOrder.BACKWARD, PaginationOrder.FORWARD);
    }

    @NotNull
    @Override
    public EntitlementPaginationAction user(@Nullable UserSnowflake user) {
        if (user == null) {
            userId = 0;
        } else {
            userId = user.getIdLong();
        }
        return this;
    }

    @NotNull
    @Override
    public EntitlementPaginationAction skuIds(@NotNull long... skuIds) {
        this.skuIds.clear();
        for (long skuId : skuIds) {
            this.skuIds.add(Long.toUnsignedString(skuId));
        }
        return this;
    }

    @NotNull
    @Override
    public EntitlementPaginationAction skuIds(@NotNull String... skuIds) {
        Checks.noneNull(skuIds, "skuIds");
        for (String skuId : skuIds) {
            Checks.isSnowflake(skuId, "skuId");
        }

        this.skuIds.clear();

        Collections.addAll(this.skuIds, skuIds);
        return this;
    }

    @NotNull
    @Override
    public EntitlementPaginationAction skuIds(@NotNull Collection<String> skuIds) {
        Checks.noneNull(skuIds, "skuIds");

        this.skuIds.clear();
        for (String skuId : skuIds) {
            Checks.isSnowflake(skuId, "skuId");
            this.skuIds.add(skuId);
        }

        return this;
    }

    @NotNull
    @Override
    public EntitlementPaginationAction guild(long guildId) {
        this.guildId = guildId;
        return this;
    }

    @NotNull
    @Override
    public EntitlementPaginationAction excludeEnded(boolean excludeEnded) {
        this.excludeEnded = excludeEnded;
        return this;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute() {
        Route.CompiledRoute route = super.finalizeRoute();

        if (userId != 0) {
            route = route.withQueryParams("user_id", Long.toUnsignedString(userId));
        }

        if (!skuIds.isEmpty()) {
            route = route.withQueryParams("sku_ids", String.join(",", skuIds));
        }

        if (guildId != 0) {
            route = route.withQueryParams("guild_id", Long.toUnsignedString(guildId));
        }

        if (excludeEnded) {
            route = route.withQueryParams("exclude_ended", String.valueOf(true));
        }

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Entitlement>> request) {
        DataArray array = response.getArray();
        List<Entitlement> entitlements = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++) {
            try {
                DataObject object = array.getObject(i);
                Entitlement entitlement = builder.createEntitlement(object);
                entitlements.add(entitlement);
            } catch (ParsingException | NullPointerException e) {
                LOG.warn("Encountered an exception in EntitlementPaginationAction", e);
            }
        }

        if (!entitlements.isEmpty()) {
            if (useCache) {
                cached.addAll(entitlements);
            }
            last = entitlements.get(entitlements.size() - 1);
            lastKey = last.getIdLong();
        }

        request.onSuccess(entitlements);
    }

    @Override
    protected long getKey(Entitlement it) {
        return it.getIdLong();
    }
}
