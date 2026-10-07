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

package lonter.jfa.api;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the online presence of a {@link lonter.jfa.api.entities.Member Member}.
 */
public enum OnlineStatus {
    /**
     * Indicates that the user is currently online (green circle)
     */
    ONLINE("online"),
    /**
     * Indicates that the user is currently idle (orange circle)
     */
    IDLE("idle"),
    /**
     * Indicates that the user is currently on do not disturb (red circle)
     * <br>This means the user won't receive notifications for mentions.
     */
    DO_NOT_DISTURB("dnd"),
    /**
     * Indicates that the currently logged in account is set to invisible and shows
     * up as {@link #OFFLINE} for other users.
     * <br>Only available for the currently logged in account.
     * <br>Other {@link lonter.jfa.api.entities.Member Members} will show up as {@link lonter.jfa.api.OnlineStatus#OFFLINE OFFLINE} even when they really are INVISIBLE.
     */
    INVISIBLE("invisible"),
    /**
     * Indicates that a member is currently offline or invisible (grey circle)
     */
    OFFLINE("offline"),
    /**
     * Placeholder for possible future online status values that are not listed here yet.
     */
    UNKNOWN("");

    private final String key;

    OnlineStatus(String key) {
        this.key = key;
    }

    /**
     * The valid API key for this OnlineStatus
     *
     * @return String representation of the valid API key for this OnlineStatus
     *
     * @see    <a href="https://fluxer.com/developers/docs/topics/gateway#presence-update">PRESENCE_UPDATE</a>
     */
    @NotNull
    public String getKey() {
        return key;
    }

    /**
     * Will get the {@link lonter.jfa.api.OnlineStatus OnlineStatus} from the provided key.
     * <br>If the provided key does not have a matching OnlineStatus, this will return {@link lonter.jfa.api.OnlineStatus#UNKNOWN UNKONWN}
     *
     * @param  key
     *         The key relating to the {@link lonter.jfa.api.OnlineStatus OnlineStatus} we wish to retrieve.
     *
     * @return The matching {@link lonter.jfa.api.OnlineStatus OnlineStatus}. If there is no match, returns {@link lonter.jfa.api.OnlineStatus#UNKNOWN UNKNOWN}.
     */
    @NotNull
    public static OnlineStatus fromKey(@NotNull String key) {
        for (OnlineStatus onlineStatus : values()) {
            if (onlineStatus.key.equalsIgnoreCase(key)) {
                return onlineStatus;
            }
        }
        return UNKNOWN;
    }
}
