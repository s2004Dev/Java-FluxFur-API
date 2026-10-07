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

package lonter.jfa.internal.interactions.command;

import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.interactions.commands.context.UserContextInteraction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.MemberImpl;

public class UserContextInteractionImpl extends ContextInteractionImpl<User> implements UserContextInteraction {
    private Member member;

    public UserContextInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
    }

    @Override
    protected User parse(DataObject interaction, DataObject resolved) {
        DataObject users = resolved.getObject("users");
        DataObject user = users.getObject(users.keys().iterator().next());

        resolved.optObject("members").filter(m -> !m.keys().isEmpty()).ifPresent(members -> {
            DataObject member = members.getObject(members.keys().iterator().next());
            this.member = interactionEntityBuilder.createMember(guild, member);
            if (this.member instanceof MemberImpl) {
                api.getEntityBuilder().updateMemberCache((MemberImpl) this.member);
            }
        });

        return api.getEntityBuilder().createUser(user);
    }

    @Override
    public Member getTargetMember() {
        return member;
    }
}
