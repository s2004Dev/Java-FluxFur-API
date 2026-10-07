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

package lonter.jfa.api.events.automod;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.automod.AutoModExecution;
import lonter.jfa.api.entities.automod.AutoModResponse;
import lonter.jfa.api.entities.automod.AutoModRule;
import lonter.jfa.api.entities.automod.AutoModTriggerType;
import lonter.jfa.api.entities.channel.unions.GuildMessageChannelUnion;
import lonter.jfa.api.events.Event;
import lonter.jfa.api.requests.GatewayIntent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that an automated {@link AutoModResponse} has been triggered through an {@link AutoModRule}.
 *
 * <p><b>Requirements</b><br>
 * This event requires the {@link GatewayIntent#AUTO_MODERATION_EXECUTION AUTO_MODERATION_EXECUTION} intent to be enabled.
 * <br>This event will only fire for guilds where the bot has the {@link lonter.jfa.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission.
 * Additionally, access to {@link #getContent()} and {@link #getMatchedContent()} requires the {@link GatewayIntent#MESSAGE_CONTENT MESSAGE_CONTENT} intent to be enabled.
 */
public class AutoModExecutionEvent extends Event implements AutoModExecution {
    private final AutoModExecution execution;

    public AutoModExecutionEvent(@NotNull JFA api, long responseNumber, @NotNull AutoModExecution execution) {
        super(api, responseNumber);
        this.execution = execution;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return execution.getGuild();
    }

    @Nullable
    @Override
    public GuildMessageChannelUnion getChannel() {
        return execution.getChannel();
    }

    @NotNull
    @Override
    public AutoModResponse getResponse() {
        return execution.getResponse();
    }

    @NotNull
    @Override
    public AutoModTriggerType getTriggerType() {
        return execution.getTriggerType();
    }

    @Override
    public long getUserIdLong() {
        return execution.getUserIdLong();
    }

    @Override
    public long getRuleIdLong() {
        return execution.getRuleIdLong();
    }

    @Override
    public long getMessageIdLong() {
        return execution.getMessageIdLong();
    }

    @Override
    public long getAlertMessageIdLong() {
        return execution.getAlertMessageIdLong();
    }

    @NotNull
    @Override
    public String getContent() {
        return execution.getContent();
    }

    @Nullable
    @Override
    public String getMatchedContent() {
        return execution.getMatchedContent();
    }

    @Nullable
    @Override
    public String getMatchedKeyword() {
        return execution.getMatchedKeyword();
    }
}
