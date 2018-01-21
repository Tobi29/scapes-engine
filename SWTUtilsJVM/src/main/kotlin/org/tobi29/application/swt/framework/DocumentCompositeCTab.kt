/*
 * Copyright 2012-2017 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.application.swt.framework

import org.eclipse.swt.custom.CTabItem
import org.eclipse.swt.widgets.Composite

internal class DocumentCompositeCTab(parent: Composite,
                                     style: Int,
                                     shell: DocumentShell,
                                     val tabItem: CTabItem) : DocumentComposite(
        parent, style, shell) {

    init {
        addDisposeListener { e -> tabItem.dispose() }
    }

    override fun populate() {
        super.populate()
        tabItem.text = document?.shortTitle ?: ""
    }
}
