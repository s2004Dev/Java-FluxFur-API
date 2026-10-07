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

package lonter.jfa.internal.managers;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.templates.Template;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.TemplateManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TemplateManagerImpl extends ManagerBase<TemplateManager> implements TemplateManager {
    protected final Template template;
    protected final JFA api;

    protected String name;
    protected String description;

    /**
     * Creates a new TemplateManager instance
     *
     * @param template
     *        {@link Template Template} that should be modified
     */
    public TemplateManagerImpl(Template template) {
        super(
                template.getJFA(),
                Route.Templates.MODIFY_TEMPLATE.compile(template.getGuild().getId(), template.getCode()));
        this.template = template;
        this.api = template.getJFA();
        if (isPermissionChecksEnabled()) {
            checkPermissions();
        }
    }

    @NotNull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl reset(long fields) {
        super.reset(fields);
        if ((fields & NAME) == NAME) {
            this.name = null;
        }
        if ((fields & DESCRIPTION) == DESCRIPTION) {
            this.description = null;
        }
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl reset() {
        super.reset();
        this.name = null;
        this.description = null;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl setName(@NotNull String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public TemplateManagerImpl setDescription(@Nullable String description) {
        if (description != null) {
            Checks.notLonger(name, 120, "Description");
        }
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject body = DataObject.empty();
        if (shouldUpdate(NAME)) {
            body.put("name", name);
        }
        if (shouldUpdate(DESCRIPTION)) {
            body.put("description", name);
        }

        reset();
        return getRequestBody(body);
    }

    @Override
    protected boolean checkPermissions() {
        Guild guild = api.getGuildById(template.getGuild().getIdLong());

        if (guild == null) {
            return true;
        }
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            throw new InsufficientPermissionException(guild, Permission.MANAGE_SERVER);
        }
        return super.checkPermissions();
    }
}
