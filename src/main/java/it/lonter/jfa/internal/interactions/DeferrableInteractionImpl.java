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

package it.lonter.jfa.internal.interactions;

import it.lonter.jfa.api.exceptions.InteractionFailureException;
import it.lonter.jfa.api.interactions.InteractionHook;
import it.lonter.jfa.api.interactions.callbacks.IDeferrableCallback;
import it.lonter.jfa.api.utils.data.DataObject;
import it.lonter.jfa.internal.JFAImpl;

import org.jetbrains.annotations.NotNull;

public class DeferrableInteractionImpl extends InteractionImpl implements IDeferrableCallback {
    protected final InteractionHookImpl hook;

    public DeferrableInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
        this.hook = new InteractionHookImpl(this, jfa);
    }

    @Override
    public synchronized void releaseHook(boolean success) {
        if (success) {
            hook.ready();
        } else {
            hook.fail(new InteractionFailureException());
        }
    }

    @NotNull
    @Override
    public InteractionHook getHook() {
        return hook;
    }
}
