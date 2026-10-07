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
 * Fluxer API communication features.
 *
 * <p>The {@link lonter.jfa.api.requests.RestAction RestAction} interface can be found throughout JFA and allows
 * to specify how the Requester should deal with rate limits.
 * It has extensions with additional functionalities in {@link lonter.jfa.api.requests.restaction}.
 *
 * <p>In the case of a failed Request the RestAction will be provided with an {@link lonter.jfa.api.exceptions.ErrorResponseException ErrorResponseException}
 * which contains a {@link lonter.jfa.api.requests.ErrorResponse ErrorResponse} representing the failure cause!
 */
package lonter.jfa.api.requests;
