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

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

internal abstract class DocumentShell(display: Display,
                                      style: Int,
                                      val application: MultiDocumentApplication,
                                      protected val hideSingleTab: Boolean) : Shell(
        display, style) {
    protected var directComposite: DocumentComposite? = null

    init {
        layout = FillLayout()
    }

    internal abstract fun updateTab()

    fun item(document: Document): DocumentComposite {
        if (isEmpty && hideSingleTab) {
            val composite = DocumentComposite(this,
                    SWT.NONE, this)
            composite.document = document
            directComposite = composite
            updateTab()
            return composite
        }
        val composite = tabItem(document)
        layout()
        updateTab()
        return composite
    }

    internal abstract fun remove(composite: DocumentComposite)

    protected abstract val isEmpty: Boolean

    protected abstract fun tabItem(document: Document): DocumentComposite

    override fun checkSubclass() {
    }

    protected fun setMenuBar(composite: DocumentComposite) {
        text = composite.document?.title ?: ""
        menuBar = if (composite.menu.itemCount == 0) {
            null
        } else {
            composite.menu
        }
    }

    protected fun clearDirect() {
        val directComposite = directComposite
        if (directComposite != null) {
            val otherDocument = directComposite.document
            directComposite.removeDocument()
            directComposite.dispose()
            this.directComposite = null
            otherDocument?.let { tabItem(it) }
        }
    }
}
