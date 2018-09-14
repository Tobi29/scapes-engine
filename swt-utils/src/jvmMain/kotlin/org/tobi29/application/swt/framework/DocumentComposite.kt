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

import org.eclipse.swt.widgets.Composite
import org.tobi29.application.swt.widgets.SmartMenuBar
import org.tobi29.application.swt.widgets.ifPresent

open class DocumentComposite internal constructor(parent: Composite,
                                                  style: Int,
                                                  internal val shell: DocumentShell) : Composite(
        parent, style) {
    internal val menu = SmartMenuBar(parent.shell)
    internal var document: Document? = null
        set(value) {
            field?.let {
                it.destroy()
                shell.application.composites.remove(it)
            }
            field = value
            if (value != null) {
                populate()
                shell.application.composites.put(value, this)
            }
        }

    init {
        addDisposeListener { e ->
            document?.let {
                it.forceClose()
                shell.application.composites.remove(it)
            }
            document = null
            // Changing menu bar now causes GTK2 and Win32 port to crash
            display.timerExec(0) {
                menu.ifPresent { it.dispose() }
            }
        }
    }

    fun removeDocument(): Document {
        val document = document ?: throw IllegalStateException(
                "Document removed")
        document.destroy()
        shell.application.composites.remove(document)
        this.document = null
        return document
    }

    protected open fun populate() {
        if (document == null) {
            throw IllegalStateException("Document removed")
        }
        children.forEach { it.dispose() }
        menu.items.forEach { it.dispose() }
        layout = null
        shell.application.populate(this, menu)
        document?.populate(this, menu, shell.application)
        layout()
    }

    override fun checkSubclass() {
    }
}
