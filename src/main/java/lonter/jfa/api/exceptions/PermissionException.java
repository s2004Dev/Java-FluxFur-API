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

package lonter.jfa.api.exceptions;

import lonter.jfa.api.Permission;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the currently logged in account does not meet the specified {@link lonter.jfa.api.Permission Permission}
 * from {@link #getPermission()}
 */
public class PermissionException extends RuntimeException {
    private final Permission permission;

    /**
     * Creates a new PermissionException instance
     *
     * @param reason
     *        The reason for this Exception
     */
    public PermissionException(String reason) {
        this(Permission.UNKNOWN, reason);
    }

    /**
     * Creates a new PermissionException instance
     *
     * @param permission
     *        The required {@link lonter.jfa.api.Permission Permission}
     */
    protected PermissionException(@NotNull Permission permission) {
        this(
                permission,
                "Cannot perform action due to a lack of Permission. Missing permission: " + permission.toString());
    }

    /**
     * Creates a new PermissionException
     *
     * @param permission
     *        The required {@link lonter.jfa.api.Permission Permission}
     * @param reason
     *        The reason for this Exception
     */
    protected PermissionException(@NotNull Permission permission, String reason) {
        super(reason);
        Checks.notNull(permission, "permission");
        this.permission = permission;
    }

    /**
     * The {@link lonter.jfa.api.Permission Permission} that is required for the operation
     *
     * <p><b>If this is a {@link lonter.jfa.api.exceptions.HierarchyException HierarchyException}
     * this will always be {@link lonter.jfa.api.Permission#UNKNOWN Permission.UNKNOWN}!</b>
     *
     * @return The required {@link lonter.jfa.api.Permission Permission}
     */
    @NotNull
    public Permission getPermission() {
        return permission;
    }
}
