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

package lonter.jfa.api.entities.detached;

/**
 * Represents an entity which can be detached.
 *
 * <p>A detached entity is one that was created from a potentially partial snapshot at some point in time,
 * they are never cached and thus cannot be retrieved, nor be updated.
 *
 * <p>An entity may be detached when the bot does not have access to it,
 * for example, in user-installed interactions outside guilds the bot is in,
 * or in guilds which the bot is installed without the {@code bot} scope.
 *
 * <p>Most methods of detached entities that would otherwise return a {@link lonter.jfa.api.requests.RestAction RestAction}
 * will throw a {@link lonter.jfa.api.exceptions.DetachedEntityException DetachedEntityException} instead.
 */
public interface IDetachableEntity {
    /**
     * Whether this entity is detached.
     *
     * <p>If this returns {@code true},
     * this entity cannot be retrieved, will never be updated, and
     * most methods that would otherwise return a {@link lonter.jfa.api.requests.RestAction RestAction}
     * will throw a {@link lonter.jfa.api.exceptions.DetachedEntityException DetachedEntityException} instead.
     *
     * @return {@code True}, if the entity is detached
     */
    boolean isDetached();
}
