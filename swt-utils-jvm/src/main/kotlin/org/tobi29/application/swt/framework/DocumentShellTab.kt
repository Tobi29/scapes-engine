/*
 * Copyright 2012-2018 Tobi29
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

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.TabFolder
import org.eclipse.swt.widgets.TabItem

internal class DocumentShellTab(display: Display,
                                style: Int,
                                application: MultiDocumentApplication,
                                hideSingleTab: Boolean) : DocumentShell(
        display, style, application, hideSingleTab) {
    var tabFolder: TabFolder? = null

    private fun tab(tabItem: TabItem): DocumentCompositeTab {
        return tabItem.control as? DocumentCompositeTab ?: throw IllegalStateException(
                "Non document composite in tab folder")
    }

    override fun updateTab() {
        var composite: DocumentComposite? = null
        val tabFolder = tabFolder
        if (tabFolder != null) {
            val tabItems = tabFolder.selection
            if (tabItems.isNotEmpty()) {
                val control = tabItems[0].control as? DocumentCompositeTab ?: throw IllegalStateException(
                        "Non document composite in tab folder")
                composite = control
            }
        } else {
            composite = directComposite
        }
        composite?.let { setMenuBar(it) }
    }

    override fun remove(composite: DocumentComposite) {
        composite.document?.destroy()
        val tabFolder = tabFolder
        if (tabFolder != null) {
            composite.dispose()
            val composites = tabFolder.items.map { tab(it) }
            if (composites.isEmpty()) {
                dispose()
            } else if (composites.size == 1 && hideSingleTab) {
                val document = composites[0].removeDocument()
                tabFolder.dispose()
                this.tabFolder = null
                item(document)
                layout()
                updateTab()
            }
        } else if (directComposite == composite) {
            dispose()
        }
    }

    override val isEmpty: Boolean
        get() = directComposite == null && tabFolder == null

    override fun tabItem(document: Document): DocumentCompositeTab {
        val tabFolder = tabFolder()
        val tabItem = TabItem(tabFolder, SWT.CLOSE)
        val composite = DocumentCompositeTab(tabFolder,
                SWT.NONE, this, tabItem)
        tabItem.control = composite
        composite.document = document
        tabFolder.setSelection(tabItem)
        return composite
    }

    private fun tabFolder(): TabFolder {
        var tabFolder = tabFolder
        if (tabFolder == null) {
            tabFolder = TabFolder(this, SWT.NONE)
            tabFolder.addListener(SWT.Selection) { e -> updateTab() }
            this.tabFolder = tabFolder
        }
        clearDirect()
        return tabFolder
    }
}
