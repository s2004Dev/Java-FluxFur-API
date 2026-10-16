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

package lonter.jfa.internal.components;

import lonter.jfa.api.components.ResolvedMedia;
import lonter.jfa.api.utils.AttachmentProxy;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ResolvedMediaImpl(String attachmentId, String url, String proxyUrl, int width, int height,
                                String contentType) implements ResolvedMedia {
  public ResolvedMediaImpl(DataObject data) {
    this(
      data.getString("attachment_id", null),
      data.getString("url"),
      data.getString("proxy_url"),
      data.getInt("width", 0),
      data.getInt("height", 0),
      data.getString("content_type", null));
  }

  @Nullable
  @Override
  public String attachmentId() {
    return attachmentId;
  }

  @NotNull
  @Override
  public String url() {
    return url;
  }

  @NotNull
  @Override
  public String proxyUrl() {
    return proxyUrl;
  }

  @NotNull
  @Override
  public AttachmentProxy getProxy() {
    return new AttachmentProxy(width > 0 && height > 0 ? proxyUrl : url);
  }

  @Nullable
  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ResolvedMediaImpl that)) {
      return false;
    }
    return Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url);
  }

  @Override
  public @NotNull String toString() {
    return new EntityString(this)
      // url is already shown by the classes containing resolved medias
      .addMetadata("proxy_url", url)
      .toString();
  }
}
