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

package lonter.jfa.internal.handle;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.automod.AutoModRule;
import lonter.jfa.api.events.automod.AutoModRuleCreateEvent;
import lonter.jfa.api.events.automod.AutoModRuleDeleteEvent;
import lonter.jfa.api.events.automod.AutoModRuleUpdateEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.automod.AutoModRuleImpl;

public class AutoModRuleHandler extends SocketHandler {
    private final String type;

    public AutoModRuleHandler(JFAImpl api, String type) {
        super(api);
        this.type = type;
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getUnsignedLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }
        Guild guild = api.getGuildById(guildId);
        if (guild == null) {
            api.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug(
                    "Received a AUTO_MODERATION_RULE_{} for a guild that is not yet cached. JSON: {}", type, content);
            return null;
        }

        AutoModRule rule = AutoModRuleImpl.fromData(guild, content);
        switch (type) {
            case "CREATE":
                api.handleEvent(new AutoModRuleCreateEvent(api, responseNumber, rule));
                break;
            case "UPDATE":
                api.handleEvent(new AutoModRuleUpdateEvent(api, responseNumber, rule));
                break;
            case "DELETE":
                api.handleEvent(new AutoModRuleDeleteEvent(api, responseNumber, rule));
                break;
        }
        return null;
    }
}
