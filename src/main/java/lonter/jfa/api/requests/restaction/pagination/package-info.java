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

/**
 * Extensions of {@link lonter.jfa.api.requests.RestAction RestAction} that allow
 * to access paginated fluxer endpoints like the message history of a {@link lonter.jfa.api.entities.channel.middleman.MessageChannel MessageChannel}.
 * <br>The {@link lonter.jfa.api.requests.restaction.pagination.PaginationAction PaginationAction} is designed to work
 * as an {@link java.lang.Iterable Iterable} of the specified endpoint. Each implementation specifies the endpoints it will
 * use in the class-level javadoc.
 *
 * @since 3.1
 */
package lonter.jfa.api.requests.restaction.pagination;
