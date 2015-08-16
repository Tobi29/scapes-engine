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

package org.tobi29.scapes.engine.backends.lwjgl3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.tobi29.scapes.engine.input.Controller;
import org.tobi29.scapes.engine.input.ControllerJoystick;
import org.tobi29.scapes.engine.input.ControllerKey;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GLFWControllers {
    private static final double DEADZONES = 0.05, DEADZONES_SCALE = 0.95;
    private final Map<Integer, ControllerJoystick> virtualJoysticks;
    private final Map<Integer, boolean[]> virtualJoystickStates =
            new ConcurrentHashMap<>();

    public GLFWControllers(Map<Integer, ControllerJoystick> virtualJoysticks) {
        this.virtualJoysticks = virtualJoysticks;
    }

    private static double deadzones(double value) {
        if (value > DEADZONES) {
            return (value - DEADZONES) / DEADZONES_SCALE;
        } else if (value < -DEADZONES) {
            return (value + DEADZONES) / DEADZONES_SCALE;
        }
        return 0.0f;
    }

    @SuppressWarnings("ForLoopThatDoesntUseLoopVariable")
    public boolean poll() {
        boolean joysticksChanged = false;
        for (int joystick = GLFW.GLFW_JOYSTICK_1;
                joystick <= GLFW.GLFW_JOYSTICK_LAST; joystick++) {
            if (GLFW.glfwJoystickPresent(joystick) == GL11.GL_TRUE) {
                String name = GLFW.glfwGetJoystickName(joystick);
                FloatBuffer axes = GLFW.glfwGetJoystickAxes(joystick);
                ByteBuffer buttons = GLFW.glfwGetJoystickButtons(joystick);
                ControllerJoystick virtualJoystick =
                        virtualJoysticks.get(joystick);
                boolean[] states = virtualJoystickStates.get(joystick);
                if (virtualJoystick == null) {
                    assert states == null;
                    virtualJoystick =
                            new ControllerJoystick(name, axes.capacity());
                    states = new boolean[buttons.remaining()];
                    virtualJoysticks.put(joystick, virtualJoystick);
                    virtualJoystickStates.put(joystick, states);
                    joysticksChanged = true;
                } else {
                    assert states != null;
                    if (!name.equals(virtualJoystick.name()) ||
                            buttons.remaining() != states.length ||
                            axes.remaining() != virtualJoystick.axes()) {
                        virtualJoystick =
                                new ControllerJoystick(name, axes.capacity());
                        states = new boolean[buttons.remaining()];
                        virtualJoysticks.put(joystick, virtualJoystick);
                        virtualJoystickStates.put(joystick, states);
                        joysticksChanged = true;
                    }
                }
                for (int i = 0; axes.hasRemaining(); i++) {
                    virtualJoystick.setAxis(i, deadzones(axes.get()));
                }
                for (int i = 0; buttons.hasRemaining(); i++) {
                    boolean value = buttons.get() == 1;
                    if (states[i] != value) {
                        states[i] = value;
                        ControllerKey button = ControllerKey.button(i);
                        if (button != ControllerKey.UNKNOWN) {
                            virtualJoystick.addPressEvent(button,
                                    value ? Controller.PressState.PRESS :
                                            Controller.PressState.RELEASE);
                        }
                    }
                }
            } else if (virtualJoysticks.containsKey(joystick)) {
                virtualJoysticks.remove(joystick);
                virtualJoystickStates.remove(joystick);
                joysticksChanged = true;
            }
        }
        return joysticksChanged;
    }
}
