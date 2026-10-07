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

package lonter.jfa.internal.requests.restaction;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Entitlement;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.TestEntitlementCreateAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import org.jetbrains.annotations.NotNull;

public class TestEntitlementCreateActionImpl extends RestActionImpl<Entitlement>
        implements TestEntitlementCreateAction {

    private long skuId;
    private long ownerId;
    private OwnerType type;

    public TestEntitlementCreateActionImpl(JFA api, long skuId, long ownerId, OwnerType type) {
        super(
                api,
                Route.Applications.CREATE_TEST_ENTITLEMENT.compile(
                        api.getSelfUser().getApplicationId()));

        this.skuId = skuId;
        this.ownerId = ownerId;
        this.type = type;
    }

    @NotNull
    @Override
    public TestEntitlementCreateAction setSkuId(long skuId) {
        this.skuId = skuId;
        return this;
    }

    @NotNull
    @Override
    public TestEntitlementCreateAction setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @NotNull
    @Override
    public TestEntitlementCreateAction setOwnerType(@NotNull OwnerType type) {
        Checks.notNull(type, "type");

        this.type = type;
        return this;
    }

    @Override
    protected void handleSuccess(Response response, Request<Entitlement> request) {
        DataObject object = response.getObject();
        request.onSuccess(api.getEntityBuilder().createEntitlement(object));
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();
        object.put("sku_id", skuId);
        object.put("owner_id", ownerId);
        object.put("owner_type", type.getKey());

        return getRequestBody(object);
    }
}
