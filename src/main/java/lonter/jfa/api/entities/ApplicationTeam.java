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

package lonter.jfa.api.entities;

import lonter.jfa.api.utils.FluxerAssets;
import lonter.jfa.api.utils.ImageFormat;
import lonter.jfa.api.utils.ImageProxy;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Meta-data for the team of an application.
 *
 * @see ApplicationInfo#getTeam()
 */
public interface ApplicationTeam extends ISnowflake {
    /**
     * Template for {@link #getIconUrl()}
     *
     * @deprecated Replaced by {@link FluxerAssets#applicationTeamIcon(ImageFormat, String, String)}
     */
    @Deprecated
    String ICON_URL = "https://cdn.fluxerapp.com/team-icons/%s/%s.png";

    /**
     * Searches for the {@link lonter.jfa.api.entities.TeamMember TeamMember}
     * in {@link #getMembers()} that has the same user id as {@link #getOwnerIdLong()}.
     * <br>Its possible although unlikely that the owner of the team is not a member, in that case this will be null.
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.TeamMember TeamMember} who owns the team
     */
    @Nullable
    default TeamMember getOwner() {
        return getMemberById(getOwnerIdLong());
    }

    /**
     * The id for the user who owns this team.
     *
     * @return The owner id
     */
    @NotNull
    default String getOwnerId() {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    /**
     * The id for the user who owns this team.
     *
     * @return The owner id
     */
    long getOwnerIdLong();

    /**
     * The id hash for the icon of this team.
     *
     * @return The icon id, or null if no icon is applied
     *
     * @see    #getIconUrl(ImageFormat)
     */
    @Nullable
    String getIconId();

    /**
     * The url for the icon of this team.
     *
     * @return The icon url, or null if no icon is applied
     */
    @Nullable
    default String getIconUrl() {
        return getIconUrl(ImageFormat.PNG);
    }

    /**
     * The url for the icon of this team.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The icon url, or null if no icon is applied
     *
     * @see    FluxerAssets#applicationTeamIcon(ImageFormat, String, String)
     */
    @Nullable
    default String getIconUrl(@NotNull ImageFormat format) {
        ImageProxy icon = getIcon(format);
        return icon == null ? null : icon.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this application team's icon.
     *
     * @return The {@link ImageProxy} of this application team's icon, or null if no icon is applied
     *
     * @see    #getIconUrl()
     */
    @Nullable
    default ImageProxy getIcon() {
        String iconUrl = getIconUrl();
        return iconUrl == null ? null : new ImageProxy(iconUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this application team's icon.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The {@link ImageProxy} of this application team's icon, or null if no icon is applied
     *
     * @see    #getIconUrl(ImageFormat)
     * @see    FluxerAssets#applicationTeamIcon(ImageFormat, String, String)
     */
    @Nullable
    default ImageProxy getIcon(@NotNull ImageFormat format) {
        return FluxerAssets.applicationTeamIcon(format, getId(), getIconId());
    }

    /**
     * The {@link lonter.jfa.api.entities.TeamMember Team Members}.
     *
     * @return Immutable list of team members
     */
    @NotNull
    @Unmodifiable
    List<TeamMember> getMembers();

    /**
     * Check whether {@link #getMember(User)} returns null for the provided user.
     *
     * @param  user
     *         The user to check
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return True, if the provided user is a member of this team
     */
    default boolean isMember(@NotNull User user) {
        return getMember(user) != null;
    }

    /**
     * Retrieves the {@link lonter.jfa.api.entities.TeamMember TeamMember} instance
     * for the provided user. If the user is not a member of this team, null is returned.
     *
     * @param  user
     *         The user for the team member
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The {@link lonter.jfa.api.entities.TeamMember TeamMember} for the user or null
     */
    @Nullable
    default TeamMember getMember(@NotNull User user) {
        Checks.notNull(user, "User");
        return getMemberById(user.getIdLong());
    }

    /**
     * Retrieves the {@link lonter.jfa.api.entities.TeamMember TeamMember} instance
     * for the provided user id. If the user is not a member of this team, null is returned.
     *
     * @param  userId
     *         The user id for the team member
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The {@link lonter.jfa.api.entities.TeamMember TeamMember} for the user or null
     */
    @Nullable
    default TeamMember getMemberById(@NotNull String userId) {
        return getMemberById(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Retrieves the {@link lonter.jfa.api.entities.TeamMember TeamMember} instance
     * for the provided user id. If the user is not a member of this team, null is returned.
     *
     * @param  userId
     *         The user id for the team member
     *
     * @return The {@link lonter.jfa.api.entities.TeamMember TeamMember} for the user or null
     */
    @Nullable
    default TeamMember getMemberById(long userId) {
        for (TeamMember member : getMembers()) {
            if (member.getUser().getIdLong() == userId) {
                return member;
            }
        }
        return null;
    }
}
