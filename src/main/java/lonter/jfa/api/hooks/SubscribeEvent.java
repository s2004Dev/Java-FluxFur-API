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

package lonter.jfa.api.hooks;

import java.lang.annotation.*;

/**
 * Annotation used by the {@link lonter.jfa.api.hooks.AnnotatedEventManager AnnotatedEventManager}
 * this is only picked up if the event manager implementation has been set to use the {@link lonter.jfa.api.hooks.AnnotatedEventManager AnnotatedEventManager}
 * via {@link lonter.jfa.api.JFABuilder#setEventManager(IEventManager) JFABuilder.setEventManager(IEventManager)}
 *
 * @see lonter.jfa.api.hooks.AnnotatedEventManager
 * @see lonter.jfa.api.JFABuilder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SubscribeEvent {}
