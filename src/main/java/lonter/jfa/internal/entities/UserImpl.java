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

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.concrete.PrivateChannel;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.channel.concrete.PrivateChannelImpl;
import lonter.jfa.internal.requests.DeferredRestAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;

import java.util.EnumSet;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserImpl extends UserSnowflakeImpl implements User {
    protected final JFAImpl api;

    protected short discriminator;
    protected String name;
    protected String globalName;
    protected String avatarId;
    protected Profile profile;
    protected long privateChannelId = 0L;
    protected boolean bot;
    protected boolean system;
    protected int flags;
    protected PrimaryGuild primaryGuild;

    public UserImpl(long id, JFAImpl api) {
        super(id);
        this.api = api;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getGlobalName() {
        return globalName;
    }

    @NotNull
    @Override
    public String getDiscriminator() {
        return discriminator == 0 ? "0000" : Helpers.format("%04d", discriminator);
    }

    @Nullable
    @Override
    public String getAvatarId() {
        return avatarId;
    }

    @NotNull
    @Override
    public CacheRestAction<Profile> retrieveProfile() {
        return new DeferredRestAction<>(getJFA(), Profile.class, this::getProfile, () -> {
            Route.CompiledRoute route = Route.Users.GET_USER.compile(getId());
            return new RestActionImpl<>(getJFA(), route, (response, request) -> {
                DataObject json = response.getObject();

                String bannerId = json.getString("banner", null);
                int accentColor = json.getInt("accent_color", User.DEFAULT_ACCENT_COLOR_RAW);

                return new Profile(getIdLong(), bannerId, accentColor);
            });
        });
    }

    public Profile getProfile() {
        return profile;
    }

    @NotNull
    @Override
    public String getDefaultAvatarId() {
        // Backwards compatibility with old discriminator system
        return discriminator != 0 ? String.valueOf(discriminator % 5) : super.getDefaultAvatarId();
    }

    @NotNull
    @Override
    public String getAsTag() {
        return getName() + '#' + getDiscriminator();
    }

    @Override
    public boolean hasPrivateChannel() {
        return privateChannelId != 0;
    }

    @NotNull
    @Override
    public CacheRestAction<PrivateChannel> openPrivateChannel() {
        return new DeferredRestAction<>(getJFA(), PrivateChannel.class, this::getPrivateChannel, () -> {
            Route.CompiledRoute route = Route.Self.CREATE_PRIVATE_CHANNEL.compile();
            DataObject body = DataObject.empty().put("recipient_id", getId());
            return new RestActionImpl<>(getJFA(), route, body, (response, request) -> {
                PrivateChannel priv = api.getEntityBuilder().createPrivateChannel(response.getObject(), this);
                UserImpl.this.privateChannelId = priv.getIdLong();
                return priv;
            });
        });
    }

    public PrivateChannel getPrivateChannel() {
        if (!hasPrivateChannel()) {
            return null;
        }
        PrivateChannel channel = getJFA().getPrivateChannelById(privateChannelId);
        return channel != null ? channel : new PrivateChannelImpl(getJFA(), privateChannelId, this);
    }

    @NotNull
    @Override
    public List<Guild> getMutualGuilds() {
        return getJFA().getMutualGuilds(this);
    }

    @Override
    public boolean isBot() {
        return bot;
    }

    @Override
    public boolean isSystem() {
        return system;
    }

    @NotNull
    @Override
    public JFAImpl getJFA() {
        return api;
    }

    @NotNull
    @Override
    public EnumSet<UserFlag> getFlags() {
        return UserFlag.getFlags(flags);
    }

    @Override
    public int getFlagsRaw() {
        return flags;
    }

    @Nullable
    @Override
    public PrimaryGuild getPrimaryGuild() {
        return primaryGuild;
    }

    @Override
    public String toString() {
        return new EntityString(this).setName(name).toString();
    }

    // -- Setters --

    public UserImpl setName(String name) {
        this.name = name;
        return this;
    }

    public UserImpl setGlobalName(String globalName) {
        this.globalName = globalName;
        return this;
    }

    public UserImpl setDiscriminator(short discriminator) {
        this.discriminator = discriminator;
        return this;
    }

    public UserImpl setAvatarId(String avatarId) {
        this.avatarId = avatarId;
        return this;
    }

    public UserImpl setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    public UserImpl setPrivateChannel(PrivateChannel privateChannel) {
        if (privateChannel != null) {
            this.privateChannelId = privateChannel.getIdLong();
        }
        return this;
    }

    public UserImpl setBot(boolean bot) {
        this.bot = bot;
        return this;
    }

    public UserImpl setSystem(boolean system) {
        this.system = system;
        return this;
    }

    public UserImpl setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public UserImpl setPrimaryGuild(PrimaryGuild primaryGuild) {
        this.primaryGuild = primaryGuild;
        return this;
    }

    public short getDiscriminatorInt() {
        return discriminator;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;

        String out;
        if (!alt) {
            out = getAsMention();
        } else if (discriminator == 0 && upper) {
            out = getName().toUpperCase();
        } else if (discriminator == 0) {
            out = getName();
        } else if (upper) {
            out = getAsTag().toUpperCase();
        } else {
            out = getAsTag();
        }

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
