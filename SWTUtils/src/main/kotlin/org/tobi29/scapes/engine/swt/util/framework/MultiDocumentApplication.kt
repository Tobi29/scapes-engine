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
import org.eclipse.swt.widgets.Shell
import org.tobi29.scapes.engine.swt.util.widgets.Dialogs
import org.tobi29.scapes.engine.swt.util.widgets.SmartMenuBar
import org.tobi29.scapes.engine.utils.Version
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class MultiDocumentApplication : Application {
    internal val composites = HashMap<Document, DocumentComposite>()

    protected constructor(name: String,
                          id: String,
                          version: Version) : super(name, id, version)

    protected constructor(name: String,
                          id: String,
                          version: Version,
                          taskExecutor: TaskExecutor) : super(name, id, version,
            taskExecutor)

    abstract fun populate(composite: DocumentComposite,
                          menu: SmartMenuBar)

    fun openShell(document: Document) {
        shell(document)
    }

    fun openTab(document: Document) {
        tab(document)
    }

    fun document(composite: DocumentComposite): Document {
        return composite.document ?: throw IllegalStateException(
                "Composite does not hold document")
    }


    fun openNewShell(source: DocumentComposite,
                     document: Document) {
        shell(source, document)
    }

    fun openNewTab(source: DocumentComposite,
                   document: Document) {
        tab(source, document)
    }

    fun replaceTab(source: DocumentComposite,
                   document: Document) {
        reuse(source, document)
    }

    fun closeTab(source: DocumentComposite) {
        closeTabItem(source)
    }

    fun message(source: DocumentComposite,
                style: Int,
                title: String,
                message: String): Int {
        return message(source.shell, style, title, message)
    }

    fun message(source: Shell,
                style: Int,
                title: String,
                message: String): Int {
        source.open()
        return Dialogs.openMessage(source, style, title, message)
    }

    fun access(document: Document,
               consumer: (DocumentComposite) -> Unit): Boolean {
        val output = AtomicBoolean()
        display.syncExec { output.set(accessDocument(document, consumer)) }
        return output.get()
    }

    fun accessAsync(document: Document,
                    consumer: (DocumentComposite) -> Unit) {
        display.asyncExec { accessDocument(document, consumer) }
    }

    private fun accessDocument(document: Document,
                               consumer: (DocumentComposite) -> Unit): Boolean {
        val composite = composites[document] ?: return false
        assert(composite.document == document)
        consumer(composite)
        return true
    }

    internal fun closeTabItem(source: DocumentComposite) {
        source.document?.forceClose()
        source.shell.remove(source)
    }

    private fun shell(document: Document): DocumentComposite {
        // TODO: Make this configurable
        val shell = DocumentShellCTab(display, SWT.SHELL_TRIM, this, true)
        return tabFromShell(shell, document)
    }

    private fun tab(document: Document): DocumentComposite {
        val activeShell = display.activeShell
        if (activeShell is DocumentShell) {
            return tabFromShell(activeShell, document)
        }
        for (shell in display.shells) {
            if (shell is DocumentShell) {
                return tabFromShell(shell, document)
            }
        }
        return shell(document)
    }

    private fun shell(source: DocumentComposite,
                      document: Document): DocumentComposite {
        return tryReuse(source, document) ?: shell(document)
    }

    private fun tab(source: DocumentComposite,
                    document: Document): DocumentComposite {
        return tryReuse(source, document) ?: tabFromShell(source.shell,
                document)
    }

    private fun reuse(source: DocumentComposite,
                      document: Document): DocumentComposite {
        source.document?.forceClose()
        source.document = document
        source.shell.updateTab()
        return source
    }

    private fun tryReuse(source: DocumentComposite,
                         document: Document): DocumentComposite? {
        if (!(source.document?.isModified ?: false) && (source.document?.isEmpty ?: true)) {
            return reuse(source, document)
        }
        return null
    }

    private fun tabFromShell(source: DocumentShell,
                             document: Document): DocumentComposite {
        val composite = source.item(document)
        if (source.isVisible) {
            source.forceActive()
        } else {
            source.open()
        }
        return composite
    }
}
