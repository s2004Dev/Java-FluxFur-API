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

package lonter.jfa.api.utils;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Widget;
import lonter.jfa.api.exceptions.RateLimitedException;
import lonter.jfa.api.requests.RestConfig;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.WidgetImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.IOUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The WidgetUtil is a class for interacting with various facets of Fluxer's
 * guild widgets
 *
 * @author John A. Grosh
 */
public class WidgetUtil {
    public static final String WIDGET_PNG = RestConfig.DEFAULT_BASE_URL + "guilds/%s/widget.png?style=%s";
    public static final String WIDGET_URL = RestConfig.DEFAULT_BASE_URL + "guilds/%s/widget.json";
    public static final String WIDGET_HTML =
            "<iframe src=\"https://fluxer.com/widget?id=%s&theme=%s\" width=\"%d\" height=\"%d\" allowtransparency=\"true\" frameborder=\"0\"></iframe>";

    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br>This banner will only be available if the guild in question has the
     * Widget enabled.
     *
     * @param  guild
     *         The guild
     * @param  type
     *         The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    @NotNull
    public static String getWidgetBanner(@NotNull Guild guild, @NotNull BannerType type) {
        Checks.notNull(guild, "Guild");
        return getWidgetBanner(guild.getId(), type);
    }

    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br>This banner will only be available if the guild in question has the
     * Widget enabled. Additionally, this method can be used independently of
     * being on the guild in question.
     *
     * @param  guildId
     *         the guild ID
     * @param  type
     *         The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    @NotNull
    public static String getWidgetBanner(@NotNull String guildId, @NotNull BannerType type) {
        Checks.notNull(guildId, "GuildId");
        Checks.notNull(type, "BannerType");
        return String.format(WIDGET_PNG, guildId, type.name().toLowerCase());
    }

    /**
     * Gets the pre-made HTML Widget for the specified guild using the specified
     * settings. The widget will only display correctly if the guild in question
     * has the Widget enabled.
     *
     * @param  guild
     *         the guild
     * @param  theme
     *         the theme, light or dark
     * @param  width
     *         the width of the widget
     * @param  height
     *         the height of the widget
     *
     * @return a String containing the pre-made widget with the supplied settings
     */
    @NotNull
    public static String getPremadeWidgetHtml(@NotNull Guild guild, @NotNull WidgetTheme theme, int width, int height) {
        Checks.notNull(guild, "Guild");
        return getPremadeWidgetHtml(guild.getId(), theme, width, height);
    }

    /**
     * Gets the pre-made HTML Widget for the specified guild using the specified
     * settings. The widget will only display correctly if the guild in question
     * has the Widget enabled. Additionally, this method can be used independently
     * of being on the guild in question.
     *
     * @param  guildId
     *         the guild ID
     * @param  theme
     *         the theme, light or dark
     * @param  width
     *         the width of the widget
     * @param  height
     *         the height of the widget
     *
     * @return a String containing the pre-made widget with the supplied settings
     */
    @NotNull
    public static String getPremadeWidgetHtml(
            @NotNull String guildId, @NotNull WidgetTheme theme, int width, int height) {
        Checks.notNull(guildId, "GuildId");
        Checks.notNull(theme, "WidgetTheme");
        Checks.notNegative(width, "Width");
        Checks.notNegative(height, "Height");
        return Helpers.format(WIDGET_HTML, guildId, theme.name().toLowerCase(), width, height);
    }

    /**
     * Makes a GET request to get the information for a Guild's widget. This
     * widget (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     * <p>This Widget can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     *
     * @param  guildId
     *         The id of the Guild
     *
     * @throws lonter.jfa.api.exceptions.RateLimitedException
     *         If the request was rate limited, <b>respect the timeout</b>!
     * @throws java.lang.NumberFormatException
     *         If the provided {@code guildId} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return {@code null} if the provided guild ID is not a valid Fluxer guild ID
     *         <br>a Widget object with null fields and isAvailable() returning
     *         false if the guild ID is valid but the guild in question does not
     *         have the widget enabled
     *         <br>a filled-in Widget object if the guild ID is valid and the guild
     *         in question has the widget enabled.
     */
    @Nullable
    public static Widget getWidget(@NotNull String guildId) throws RateLimitedException {
        return getWidget(MiscUtil.parseSnowflake(guildId));
    }

    /**
     * Makes a GET request to get the information for a Guild's widget. This
     * widget (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     * <p>This Widget can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     *
     * @param  guildId
     *         The id of the Guild
     *
     * @throws java.io.UncheckedIOException
     *         If an I/O error occurs
     * @throws lonter.jfa.api.exceptions.RateLimitedException
     *         If the request was rate limited, <b>respect the timeout</b>!
     *
     * @return {@code null} if the provided guild ID is not a valid Fluxer guild ID
     *         <br>a Widget object with null fields and isAvailable() returning
     *         false if the guild ID is valid but the guild in question does not
     *         have the widget enabled
     *         <br>a filled-in Widget object if the guild ID is valid and the guild
     *         in question has the widget enabled.
     */
    @Nullable
    public static Widget getWidget(long guildId) throws RateLimitedException {
        Checks.notNull(guildId, "GuildId");

        HttpURLConnection connection;
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(String.format(WIDGET_URL, guildId))
                .method("GET", null)
                .header("user-agent", RestConfig.USER_AGENT)
                .header("accept-encoding", "gzip")
                .build();

        try (Response response = client.newCall(request).execute()) {
            int code = response.code();
            InputStream data = IOUtil.getBody(response);

            switch (code) {
                case 200: // ok
                {
                    try (InputStream stream = data) {
                        return new WidgetImpl(DataObject.fromJson(stream));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                case 400: // not valid snowflake
                case 404: // guild not found
                    return null;
                case 403: // widget disabled
                    return new WidgetImpl(guildId);
                case 429: // ratelimited
                {
                    long retryAfter;
                    try (InputStream stream = data) {
                        retryAfter = DataObject.fromJson(stream).getLong("retry_after");
                    } catch (Exception e) {
                        retryAfter = 0;
                    }
                    throw new RateLimitedException(WIDGET_URL, retryAfter);
                }
                default:
                    throw new IllegalStateException(
                            "An unknown status was returned: " + code + " " + response.message());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Represents the available banner types
     * <br>Each of these has a different appearance:
     *
     * <p>
     * <br><b>Shield</b> - tiny, only contains Fluxer logo and online count
     * <br><b>Banner1</b> - medium, contains server name, icon, and online count, and a "Powered by Fluxer" bar on the bottom
     * <br><b>Banner2</b> - small, contains server name, icon, and online count, and a Fluxer logo on the side
     * <br><b>Banner3</b> - medium, contains server name, icon, and online count, and a Fluxer logo with a "Chat Now" bar on the bottom
     * <br><b>Banner4</b> - large, contains a very big Fluxer logo, server name, icon, and online count, and a big "Join My Server" button
     */
    public enum BannerType {
        SHIELD,
        BANNER1,
        BANNER2,
        BANNER3,
        BANNER4
    }

    /**
     * Represents the color scheme of the widget
     * <br>These color themes match Fluxer's dark and light themes
     */
    public enum WidgetTheme {
        LIGHT,
        DARK
    }
}
