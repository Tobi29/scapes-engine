/*
 * Copyright 2012-2015 Tobi29
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
package org.tobi29.scapes.engine.backends.lwjgl3.qt;

import com.trolltech.qt.QNoSuchEnumValueException;
import com.trolltech.qt.QtInfo;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.opengl.QGLFormat;
import com.trolltech.qt.opengl.QGLWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.Game;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.backends.lwjgl3.ContainerLWJGL3;
import org.tobi29.scapes.engine.gui.GlyphRenderer;
import org.tobi29.scapes.engine.input.Controller;
import org.tobi29.scapes.engine.input.ControllerJoystick;
import org.tobi29.scapes.engine.input.ControllerKey;
import org.tobi29.scapes.engine.opengl.Container;
import org.tobi29.scapes.engine.opengl.GraphicsCheckException;
import org.tobi29.scapes.engine.qt.util.ImageConverter;
import org.tobi29.scapes.engine.utils.DesktopException;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Sync;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.io.ProcessStream;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerQWidget extends ContainerLWJGL3 {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ContainerQWidget.class);
    private static final Path[] EMPTY_PATH = {};
    private final QMainWindow window;
    private final Sync sync;
    private final QCursor defaultCursor, hiddenCursor;
    private final List<JInputControllers.JoystickMapper> virtualJoysticks =
            new ArrayList<>();
    private final JInputControllers controllers;
    private final QGLWidget canvas;
    private Optional<String> invalidContext = Optional.empty();
    private boolean running = true, mouseGrabbed, mouseGrabbedCurrent;

    public ContainerQWidget(ScapesEngine engine) {
        super(engine);
        LOGGER.info("Qt version: {}", QtInfo.versionString());
        Game game = engine.game();
        QApplication.setApplicationName(game.name());
        QApplication.setApplicationVersion(game.version().toString());
        Image icon = game.icon();
        QApplication.setWindowIcon(
                new QIcon(QPixmap.fromImage(ImageConverter.image(icon))));
        LOGGER.info("Creating Qt window...");
        window = new QMainWindow() {
            @Override
            protected void closeEvent(QCloseEvent arg__1) {
                arg__1.ignore();
                engine.stop();
            }

            @Override
            protected void keyPressEvent(QKeyEvent arg__1) {
                super.keyPressEvent(arg__1);
                try {
                    ControllerKey virtualKey =
                            QtKeyMap.key(Qt.Key.resolve(arg__1.key()));
                    if (virtualKey != ControllerKey.UNKNOWN) {
                        addPressEvent(virtualKey, arg__1.isAutoRepeat() ?
                                Controller.PressState.REPEAT :
                                Controller.PressState.PRESS);
                    }
                } catch (QNoSuchEnumValueException e) {
                }
                String text = arg__1.text();
                for (int i = 0; i < text.length(); i++) {
                    addTypeEvent(text.charAt(i));
                }
            }

            @Override
            protected void keyReleaseEvent(QKeyEvent arg__1) {
                super.keyReleaseEvent(arg__1);
                try {
                    ControllerKey virtualKey =
                            QtKeyMap.key(Qt.Key.resolve(arg__1.key()));
                    if (virtualKey != ControllerKey.UNKNOWN &&
                            !arg__1.isAutoRepeat()) {
                        addPressEvent(virtualKey,
                                Controller.PressState.RELEASE);
                    }
                } catch (QNoSuchEnumValueException e) {
                }
            }
        };
        LOGGER.info("Creating Qt OpenGL context...");
        QGLFormat format = new QGLFormat();
        format.setProfile(QGLFormat.OpenGLContextProfile.CoreProfile);
        format.setVersion(3, 2);
        format.setSwapInterval(engine.config().vSync() ? 1 : 0);
        canvas = new OpenGLWidget(format);
        window.setCentralWidget(canvas);
        window.setWindowTitle(engine.game().name());
        window.resize(1280, 720);
        sync = new Sync(engine.config().fps(), 5000000000L, false, "Rendering");
        defaultCursor = new QCursor();
        hiddenCursor = new QCursor(Qt.CursorShape.BlankCursor);
        controllers = new JInputControllers(virtualJoysticks);
    }

    @Override
    public void setMouseGrabbed(boolean value) {
        mouseGrabbed = value;
    }

    @Override
    public Collection<ControllerJoystick> joysticks() {
        joysticksChanged = false;
        return virtualJoysticks.stream()
                .map(JInputControllers.JoystickMapper::joystick)
                .collect(Collectors.toList());
    }

    @Override
    public boolean loadFont(String asset) {
        Resource font = engine.files().get(asset + ".otf");
        if (!font.exists()) {
            font = engine.files().get(asset + ".ttf");
        }
        try {
            QByteArray array = new QByteArray(
                    ProcessStream.processSource(font, ProcessStream.asArray()));
            return QFontDatabase.addApplicationFontFromData(array) != -1;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public GlyphRenderer createGlyphRenderer(String fontName, int size) {
        return new QtGlyphRenderer(fontName, size);
    }

    @Override
    public void run() throws DesktopException {
        sync.init();
        while (running) {
            while (!tasks.isEmpty()) {
                tasks.poll().run();
            }
            if (!valid) {
                initWindow(engine.config().isFullscreen());
                valid = true;
            }
            QApplication.processEvents();
            joysticksChanged = controllers.poll();
            canvas.updateGL();
            if (invalidContext.isPresent()) {
                throw new GraphicsCheckException(invalidContext.get());
            }
            containerResized = false;
            if (mouseGrabbed != mouseGrabbedCurrent) {
                mouseGrabbedCurrent = mouseGrabbed;
                if (mouseGrabbedCurrent) {
                    canvas.grabMouse();
                    canvas.setCursor(hiddenCursor);
                    int width = canvas.width() / 2;
                    int height = canvas.height() / 2;
                    QCursor.setPos(
                            canvas.mapToGlobal(new QPoint(width, height)));
                } else {
                    canvas.releaseMouse();
                    canvas.setCursor(defaultCursor);
                }
            }
        }
        engine.dispose();
        window.dispose();
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public Path[] openFileDialog(Pair<String, String>[] extensions,
            String title, boolean multiple) {
        return exec(() -> {
            StringBuilder filter = new StringBuilder(extensions.length * 10);
            if (extensions.length > 0) {
                Pair<String, String> extension = extensions[0];
                filter.append(extension.b).append(" (").append(extension.a)
                        .append(')');
                for (int i = 1; i < extensions.length; i++) {
                    extension = extensions[i];
                    filter.append(";;").append(extension.b).append(" (")
                            .append(extension.a).append(')');
                }
            }
            QFileDialog.Filter dialogFilter =
                    new QFileDialog.Filter(filter.toString());
            if (multiple) {
                List<String> files = QFileDialog
                        .getOpenFileNames(window, title, "", dialogFilter,
                                new QFileDialog.Options());
                return files.stream().map(Paths::get).toArray(Path[]::new);
            } else {
                String file = QFileDialog
                        .getOpenFileName(window, title, "", dialogFilter);
                if (file.isEmpty()) {
                    return EMPTY_PATH;
                }
                return new Path[]{Paths.get(file)};
            }
        });
    }

    @Override
    public Optional<Path> saveFileDialog(Pair<String, String>[] extensions,
            String title) {
        return exec(() -> {
            StringBuilder filter = new StringBuilder(extensions.length * 10);
            if (extensions.length > 0) {
                Pair<String, String> extension = extensions[0];
                filter.append(extension.b).append(" (").append(extension.a)
                        .append(')');
                for (int i = 1; i < extensions.length; i++) {
                    extension = extensions[i];
                    filter.append(";;").append(extension.b).append(" (")
                            .append(extension.a).append(')');
                }
            }
            QFileDialog.Filter dialogFilter =
                    new QFileDialog.Filter(filter.toString());
            String file = QFileDialog
                    .getSaveFileName(window, title, "", dialogFilter);
            if (file.isEmpty()) {
                return Optional.<Path>empty(); // Fucking hell, Java...
            }
            return Optional.of(Paths.get(file));
        });
    }

    @Override
    public void message(Container.MessageType messageType, String title,
            String message) {
        exec(() -> {
            switch (messageType) {
                case ERROR:
                    QMessageBox.critical(window, title, message);
                    break;
                case INFORMATION:
                    QMessageBox.information(window, title, message);
                    break;
                case WARNING:
                    QMessageBox.warning(window, title, message);
                    break;
                case QUESTION:
                    QMessageBox.question(window, title, message);
                    break;
            }
        });
    }

    @Override
    public void openFile(Path path) {
        exec(() -> {
            try {
                QDesktopServices
                        .openUrl(new QUrl(path.toUri().toURL().toString()));
            } catch (MalformedURLException e) {
            }
        });
    }

    protected void initWindow(boolean fullscreen) {
        canvas.setGeometry(0, 0, 1, 1);
        if (fullscreen) {
            // Force window onto primary instead of top-left monitor
            QRect monitor = QApplication.desktop().screenGeometry();
            window.move(monitor.x(), monitor.y());
            window.showFullScreen();
        } else {
            window.showNormal();
        }
    }

    @Override
    public void clipboardCopy(String value) {
        exec(() -> QApplication.clipboard().setText(value));
    }

    @Override
    public String clipboardPaste() {
        return exec(() -> QApplication.clipboard().text());
    }

    private class OpenGLWidget extends QGLWidget {
        private OpenGLWidget(QGLFormat format) {
            super(format);
            setMouseTracking(true);
        }

        @Override
        protected void initializeGL() {
            super.initializeGL();
            invalidContext = initContext();
            engine.graphics().reset();
        }

        @Override
        protected void paintGL() {
            super.paintGL();
            if (!invalidContext.isPresent()) {
                engine.render(sync.delta());
                sync.cap();
            }
        }

        @Override
        protected void resizeEvent(QResizeEvent arg__1) {
            super.resizeEvent(arg__1);
            QSize size = arg__1.size();
            containerWidth = size.width();
            containerHeight = size.height();
        }

        @Override
        protected void resizeGL(int w, int h) {
            super.resizeGL(w, h);
            contentWidth = w;
            contentHeight = h;
            containerResized = true;
        }

        @Override
        protected void mouseMoveEvent(QMouseEvent arg__1) {
            super.mouseMoveEvent(arg__1);
            QPointF pos = arg__1.posF();
            double xpos = pos.x();
            double ypos = pos.y();
            double dx, dy;
            if (mouseGrabbed) {
                int width = width() / 2;
                int height = height() / 2;
                dx = xpos - width;
                dy = ypos - height;
                QCursor.setPos(mapToGlobal(new QPoint(width, height)));
            } else {
                dx = xpos - mouseX;
                dy = ypos - mouseY;
                mouseX = (int) xpos;
                mouseY = (int) ypos;
            }
            if (dx != 0.0 || dy != 0.0) {
                set(xpos, ypos);
                addDelta(dx, dy);
            }
        }

        @Override
        protected void mousePressEvent(QMouseEvent arg__1) {
            super.mousePressEvent(arg__1);
            ControllerKey virtualKey = QtKeyMap.button(arg__1.button());
            if (virtualKey != ControllerKey.UNKNOWN) {
                addPressEvent(virtualKey, Controller.PressState.PRESS);
            }
        }

        @Override
        protected void mouseReleaseEvent(QMouseEvent arg__1) {
            super.mouseReleaseEvent(arg__1);
            ControllerKey virtualKey = QtKeyMap.button(arg__1.button());
            if (virtualKey != ControllerKey.UNKNOWN) {
                addPressEvent(virtualKey, Controller.PressState.RELEASE);
            }
        }

        @Override
        protected void wheelEvent(QWheelEvent arg__1) {
            super.wheelEvent(arg__1);
            double delta = arg__1.delta() / 120.0;
            if (arg__1.orientation() == Qt.Orientation.Horizontal) {
                addScroll(delta, 0.0);
            } else {
                addScroll(0.0, delta);
            }
        }
    }
}
