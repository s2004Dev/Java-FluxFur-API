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

import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.SelfUser;
import lonter.jfa.api.entities.channel.concrete.PrivateChannel;
import lonter.jfa.api.managers.AccountManager;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.managers.AccountManagerImpl;

import org.jetbrains.annotations.NotNull;

public class SelfUserImpl extends UserImpl implements SelfUser {
    private boolean verified;
    private boolean mfaEnabled;
    private long applicationId;

    public SelfUserImpl(long id, JFAImpl api) {
        super(id, api);
        // configured later by EntityBuilder#createSelfUser
        // when handling the ready event payload
        this.applicationId = id;
    }

    @Override
    public boolean hasPrivateChannel() {
        return false;
    }

    @Override
    public PrivateChannel getPrivateChannel() {
        throw new UnsupportedOperationException("You cannot get a PrivateChannel with yourself (SelfUser)");
    }

    @NotNull
    @Override
    public CacheRestAction<PrivateChannel> openPrivateChannel() {
        throw new UnsupportedOperationException("You cannot open a PrivateChannel with yourself (SelfUser)");
    }

    @Override
    public long getApplicationIdLong() {
        return applicationId;
    }

    @Override
    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    @Override
    public long getAllowedFileSize() {
        return Message.MAX_FILE_SIZE;
    }

    @NotNull
    @Override
    public AccountManager getManager() {
        return new AccountManagerImpl(this);
    }

    public SelfUserImpl setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public SelfUserImpl setMfaEnabled(boolean enabled) {
        this.mfaEnabled = enabled;
        return this;
    }

    public SelfUserImpl setApplicationId(long id) {
        this.applicationId = id;
        return this;
    }

    public static SelfUserImpl copyOf(SelfUserImpl other, JFAImpl jfa) {
        SelfUserImpl selfUser = new SelfUserImpl(other.id, jfa);
        selfUser.setName(other.name)
                .setGlobalName(other.globalName)
                .setAvatarId(other.avatarId)
                .setDiscriminator(other.getDiscriminatorInt())
                .setBot(other.bot);
        return selfUser.setVerified(other.verified)
                .setMfaEnabled(other.mfaEnabled)
                .setApplicationId(other.applicationId);
    }
}
