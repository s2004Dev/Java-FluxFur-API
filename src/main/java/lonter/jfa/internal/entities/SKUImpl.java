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

package lonter.jfa.internal.entities;

import lonter.jfa.api.entities.SKU;
import lonter.jfa.api.entities.SKUFlag;
import lonter.jfa.api.entities.SKUType;
import lonter.jfa.internal.utils.Helpers;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class SKUImpl extends SkuSnowflakeImpl implements SKU {
    private final SKUType type;
    private final String name;
    private final String slug;
    private final Set<SKUFlag> flags;

    public SKUImpl(long id, SKUType type, String name, String slug, Set<SKUFlag> flags) {
        super(id);
        this.type = type;
        this.name = name;
        this.slug = slug;
        this.flags = flags;
    }

    @Override
    @NotNull
    public SKUType getType() {
        return type;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public String getSlug() {
        return slug;
    }

    @Override
    @NotNull
    public Set<SKUFlag> getFlags() {
        return Helpers.copyEnumSet(SKUFlag.class, flags);
    }
}
