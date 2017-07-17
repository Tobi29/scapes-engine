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

package org.tobi29.scapes.engine.swt.util.framework

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.CTabFolder
import org.eclipse.swt.custom.CTabFolder2Adapter
import org.eclipse.swt.custom.CTabFolderEvent
import org.eclipse.swt.custom.CTabItem
import org.eclipse.swt.widgets.Display


internal class DocumentShellCTab(display: Display,
                                 style: Int,
                                 application: MultiDocumentApplication,
                                 hideSingleTab: Boolean) : DocumentShell(
        display, style, application, hideSingleTab) {
    var tabFolder: CTabFolder? = null

    private fun tab(tabItem: CTabItem): DocumentCompositeCTab {
        return tabItem.control as? DocumentCompositeCTab ?: throw IllegalStateException(
                "Non document composite in tab folder")
    }

    override fun updateTab() {
        val composite: DocumentComposite?
        val tabFolder = tabFolder
        if (tabFolder != null) {
            val tabItem = tabFolder.selection
            val control = tabItem.control as? DocumentCompositeCTab ?: throw IllegalStateException(
                    "Non document composite in tab folder")
            composite = control as DocumentComposite
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

    override fun tabItem(document: Document): DocumentCompositeCTab {
        val tabFolder = tabFolder()
        val tabItem = CTabItem(tabFolder, SWT.CLOSE)
        val composite = DocumentCompositeCTab(tabFolder, SWT.NONE, this,
                tabItem)
        tabItem.control = composite
        composite.document = document
        tabFolder.selection = tabItem
        return composite
    }

    private fun tabFolder(): CTabFolder {
        var tabFolder = tabFolder
        if (tabFolder == null) {
            tabFolder = CTabFolder(this, SWT.NONE)
            tabFolder.addListener(SWT.Selection) { e -> updateTab() }
            tabFolder.addCTabFolder2Listener(object : CTabFolder2Adapter() {
                override fun close(event: CTabFolderEvent?) {
                    val tabItem = event?.item as CTabItem?
                    val control = tabItem?.control as? DocumentCompositeCTab ?: throw IllegalStateException(
                            "Non document composite in tab folder")
                    application.closeTabItem(control)
                }
            })
            this.tabFolder = tabFolder
        }
        clearDirect()
        return tabFolder
    }
}
