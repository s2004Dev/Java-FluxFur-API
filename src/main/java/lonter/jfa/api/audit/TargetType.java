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

package lonter.jfa.api.audit;

/**
 * TargetType for an {@link lonter.jfa.api.audit.ActionType ActionType}
 * <br>This describes what kind of Fluxer entity is being targeted by an auditable action!
 *
 * <p>This can be found via {@link lonter.jfa.api.audit.ActionType#getTargetType() ActionType.getTargetType()}
 * or {@link lonter.jfa.api.audit.AuditLogEntry#getTargetType() AuditLogEntry.getTargetType()}.
 * <br>This helps to decide what entity type the target id of an AuditLogEntry refers to.
 *
 * <p><b>Example</b><br>
 * If {@code entry.getTargetType()} is type {@link #GUILD}
 * <br>Then the target id returned by {@code entry.getTargetId()} and {@code entry.getTargetIdLong()}
 * can be used with {@link lonter.jfa.api.JFA#getGuildById(long) JFA.getGuildById(id)}
 */
public enum TargetType {
    GUILD,
    CHANNEL,
    ROLE,
    MEMBER,
    INVITE,
    WEBHOOK,
    EMOJI,
    INTEGRATION,
    STAGE_INSTANCE,
    STICKER,
    THREAD,
    SCHEDULED_EVENT,
    AUTO_MODERATION_RULE,
    SOUNDBOARD_SOUND,
    ONBOARDING_PROMPT_STRUCTURE,
    ONBOARDING,
    UNKNOWN
}
