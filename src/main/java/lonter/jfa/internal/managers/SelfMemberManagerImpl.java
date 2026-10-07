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

import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.SelfMember;
import lonter.jfa.api.managers.SelfMemberManager;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelfMemberManagerImpl extends ManagerBase<SelfMemberManager> implements SelfMemberManager {
    protected final SelfMember selfMember;

    protected String nickname;
    protected Icon avatar;
    protected Icon banner;
    protected String bio;

    public SelfMemberManagerImpl(@NotNull SelfMember selfMember) {
        super(
                selfMember.getJFA(),
                Route.Guilds.MODIFY_SELF.compile(selfMember.getGuild().getId()));
        this.selfMember = selfMember;
    }

    @NotNull
    @Override
    public SelfMember getMember() {
        return selfMember;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl reset(long fields) {
        super.reset(fields);
        if ((fields & NICKNAME) == NICKNAME) {
            nickname = null;
        }
        if ((fields & AVATAR) == AVATAR) {
            avatar = null;
        }
        if ((fields & BANNER) == BANNER) {
            banner = null;
        }
        if ((fields & BIO) == BIO) {
            bio = null;
        }
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl reset() {
        super.reset();
        nickname = null;
        avatar = null;
        banner = null;
        bio = null;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setNickname(@Nullable String nickname) {
        if (nickname != null) {
            Checks.notLonger(nickname, Member.MAX_NICKNAME_LENGTH, "Nickname");
        }
        this.nickname = nickname;
        set |= NICKNAME;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setAvatar(@Nullable Icon avatar) {
        this.avatar = avatar;
        set |= AVATAR;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setBanner(@Nullable Icon banner) {
        this.banner = banner;
        set |= BANNER;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public SelfMemberManagerImpl setBio(@Nullable String bio) {
        if (bio != null) {
            Checks.notLonger(bio, SelfMember.MAX_BIO_LENGTH, "Bio");
        }
        this.bio = bio;
        set |= BIO;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject body = DataObject.empty();

        if (shouldUpdate(NICKNAME)) {
            body.put("nick", nickname);
        }
        if (shouldUpdate(AVATAR)) {
            body.put("avatar", avatar == null ? null : avatar.getEncoding());
        }
        if (shouldUpdate(BANNER)) {
            body.put("banner", banner == null ? null : banner.getEncoding());
        }
        if (shouldUpdate(BIO)) {
            body.put("bio", bio);
        }

        reset();
        return getRequestBody(body);
    }

    @Override
    protected void handleSuccess(Response response, Request<Void> request) {
        request.onSuccess(null);
    }
}
