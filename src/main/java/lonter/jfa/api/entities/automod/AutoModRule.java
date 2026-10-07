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

package lonter.jfa.api.entities.automod;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.ISnowflake;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.automod.build.AutoModRuleData;
import lonter.jfa.api.entities.automod.build.TriggerConfig;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.AutoModRuleManager;
import lonter.jfa.api.requests.restaction.AuditableRestAction;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Rule used for auto-moderation in a {@link Guild}.
 *
 * @see Guild#retrieveAutoModRules()
 * @see Guild#createAutoModRule(AutoModRuleData)
 */
public interface AutoModRule extends ISnowflake {
    /**
     * The maximum length of a rule name. ({@value})
     */
    int MAX_RULE_NAME_LENGTH = 100;
    /**
     * The maximum length of a keyword in {@link TriggerConfig#keywordFilter(String...)}. ({@value})
     */
    int MAX_KEYWORD_LENGTH = 60;
    /**
     * The maximum amount of keywords in {@link TriggerConfig#keywordFilter(String...)}. ({@value})
     */
    int MAX_KEYWORD_AMOUNT = 1000;
    /**
     * The maximum amount of whitelisted keywords in {@link TriggerConfig#keywordFilter(String...)}. ({@value})
     */
    int MAX_ALLOWLIST_CUSTOM_AMOUNT = 100;
    /**
     * The maximum amount of whitelisted keywords in {@link TriggerConfig#presetKeywordFilter(KeywordPreset...)}. ({@value})
     */
    int MAX_ALLOWLIST_PRESET_AMOUNT = 1000;
    /**
     * The maximum length of a regex pattern in {@link TriggerConfig#patternFilter(String...)}. ({@value})
     */
    int MAX_PATTERN_LENGTH = 260;
    /**
     * The maximum amount of regex patterns in {@link TriggerConfig#patternFilter(String...)}. ({@value})
     */
    int MAX_PATTERN_AMOUNT = 10;
    /**
     * The maximum limit of mentions in {@link TriggerConfig#mentionSpam(int)}. ({@value})
     */
    int MAX_MENTION_LIMIT = 50;
    /**
     * The maximum amount of roles that can be added to {@link AutoModRule#getExemptRoles()}. ({@value})
     */
    int MAX_EXEMPT_ROLES = 20;
    /**
     * The maximum amount of channels that can be added to {@link AutoModRule#getExemptChannels()}. ({@value})
     */
    int MAX_EXEMPT_CHANNELS = 50;

    /**
     * The {@link Guild} this rule belongs to.
     *
     * @return The guild
     */
    @NotNull
    Guild getGuild();

    /**
     * The user id of the creator of this rule.
     *
     * @return The owner id
     */
    long getCreatorIdLong();

    /**
     * The user id of the creator of this rule.
     *
     * @return The owner id
     */
    @NotNull
    default String getCreatorId() {
        return Long.toUnsignedString(getCreatorIdLong());
    }

    /**
     * The name of this rule.
     *
     * @return The name
     */
    @NotNull
    String getName();

    /**
     * The type of event that triggers this rule.
     *
     * @return The event type
     */
    @NotNull
    AutoModEventType getEventType();

    /**
     * The type of trigger that this rule uses.
     *
     * @return The trigger type
     */
    @NotNull
    AutoModTriggerType getTriggerType();

    /**
     * Whether this rule is enabled.
     *
     * @return True, if enabled
     */
    boolean isEnabled();

    /**
     * The roles which are exempt from the rule.
     * <p>All members of the exempt roles will bypass the rule.
     *
     * @return The exempt roles
     */
    @NotNull
    List<Role> getExemptRoles();

    /**
     * The channels which are exempt from the rule.
     * <p>All messages in the listed channels will bypass the rule.
     *
     * @return The exempt channels
     */
    @NotNull
    List<GuildChannel> getExemptChannels();

    /**
     * The automated {@link AutoModResponse AutoModResponses} that will be activated when the rule is triggered.
     *
     * @return The {@link AutoModResponse AutoModResponses}
     */
    @NotNull
    List<AutoModResponse> getActions();

    /**
     * The keywords that are blocked by this rule.
     * <p>Only applies to {@link AutoModTriggerType#KEYWORD}.
     *
     * @return The blocked keywords
     */
    @NotNull
    List<String> getFilteredKeywords();

    /**
     * The regex patterns that are blocked by this rule.
     * <p>Only applies to {@link AutoModTriggerType#KEYWORD}.
     *
     * @return The blocked regex patterns
     */
    @NotNull
    List<String> getFilteredRegex();

    /**
     * The keyword presets that are blocked by this rule.
     * <p>Only applies to {@link AutoModTriggerType#KEYWORD_PRESET}.
     *
     * @return The blocked keyword presets
     */
    @NotNull
    EnumSet<KeywordPreset> getFilteredPresets();

    /**
     * The whitelisted keywords that are allowed by this rule.
     * <p>Only applies to {@link AutoModTriggerType#KEYWORD} and {@link AutoModTriggerType#KEYWORD_PRESET}.
     *
     * @return The whitelisted keywords
     */
    @NotNull
    List<String> getAllowlist();

    /**
     * The maximum amount of mentions that are allowed in a message.
     * <p>Only applies to {@link AutoModTriggerType#MENTION_SPAM}.
     *
     * @return The mention limit, or 0 if this is not using {@link AutoModTriggerType#MENTION_SPAM}
     */
    int getMentionLimit();

    /**
     * Whether this rule is using the raid protection feature.
     * <p>Only applies to {@link AutoModTriggerType#MENTION_SPAM}.
     *
     * @return True, if mention raid protection is enabled
     */
    boolean isMentionRaidProtectionEnabled();

    /**
     * Returns an {@link AutoModRuleManager}, which can be used to modify this rule.
     * <p>The manager allows modifying multiple fields in a single request.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link lonter.jfa.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have the {@link lonter.jfa.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission.
     *
     * @return The manager instance
     */
    @NotNull
    @CheckReturnValue
    default AutoModRuleManager getManager() {
        return getGuild().modifyAutoModRuleById(getId());
    }

    /**
     * Deletes this rule.
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have the {@link lonter.jfa.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission.
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link Void}
     */
    @NotNull
    @CheckReturnValue
    default AuditableRestAction<Void> delete() {
        return getGuild().deleteAutoModRuleById(getId());
    }

    /**
     * Keyword presets that can be used in {@link AutoModRule#getFilteredPresets()}.
     */
    enum KeywordPreset {
        /**
         * Words that can be considered as swearing or cursing.
         */
        PROFANITY(1),
        /**
         * Words that can be considered as sexual in nature.
         */
        SEXUAL_CONTENT(2),
        /**
         * Words that can be considered as slurs or insults.
         */
        SLURS(3),
        /**
         * Placeholder for unknown values.
         */
        UNKNOWN(-1);

        private final int key;

        KeywordPreset(int key) {
            this.key = key;
        }

        /**
         * The raw value used by Fluxer to represent this preset.
         *
         * @return The raw value
         */
        public int getKey() {
            return key;
        }

        /**
         * The {@link KeywordPreset} represented by the provided key.
         *
         * @param  key
         *         The raw key
         *
         * @return The {@link KeywordPreset} or {@link #UNKNOWN}
         */
        @NotNull
        public static KeywordPreset fromKey(int key) {
            for (KeywordPreset preset : values()) {
                if (preset.key == key) {
                    return preset;
                }
            }
            return UNKNOWN;
        }
    }
}
