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

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.StageInstance;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.StageInstanceManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.internal.managers.StageInstanceManagerImpl;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.EntityString;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;

public class StageInstanceImpl implements StageInstance {
    private final long id;
    private StageChannel channel;

    private String topic;
    private PrivacyLevel privacyLevel;

    public StageInstanceImpl(long id, StageChannel channel) {
        this.id = id;
        this.channel = channel;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return getChannel().getGuild();
    }

    @NotNull
    @Override
    public StageChannel getChannel() {
        StageChannel real = channel.getJFA().getStageChannelById(channel.getIdLong());
        if (real != null) {
            channel = real;
        }
        return channel;
    }

    @NotNull
    @Override
    public String getTopic() {
        return topic;
    }

    @NotNull
    @Override
    public PrivacyLevel getPrivacyLevel() {
        return privacyLevel;
    }

    @NotNull
    @Override
    public RestAction<Void> delete() {
        checkPermissions();
        Route.CompiledRoute route = Route.StageInstances.DELETE_INSTANCE.compile(channel.getId());
        return new RestActionImpl<>(channel.getJFA(), route);
    }

    @NotNull
    @Override
    public StageInstanceManager getManager() {
        checkPermissions();
        return new StageInstanceManagerImpl(this);
    }

    public StageInstanceImpl setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public StageInstanceImpl setPrivacyLevel(PrivacyLevel privacyLevel) {
        this.privacyLevel = privacyLevel;
        return this;
    }

    @Override
    public String toString() {
        return new EntityString(this).addMetadata("channel", getChannel()).toString();
    }

    private void checkPermissions() {
        EnumSet<Permission> permissions = getGuild().getSelfMember().getPermissions(getChannel());
        EnumSet<Permission> required =
                EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS);
        for (Permission perm : required) {
            if (!permissions.contains(perm)) {
                throw new InsufficientPermissionException(
                        getChannel(),
                        perm,
                        "You must be a stage moderator to manage a stage instance! Missing Permission: " + perm);
            }
        }
    }
}
