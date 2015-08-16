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
import org.tobi29.scapes.engine.input.ControllerKey;

public class GLFWKeyMap {
    private static final ControllerKey[] KEYS;

    static {
        // Keyboard mappings
        KEYS = new ControllerKey[GLFW.GLFW_KEY_LAST + 1];
        KEYS[GLFW.GLFW_KEY_SPACE] = ControllerKey.KEY_SPACE;
        KEYS[GLFW.GLFW_KEY_APOSTROPHE] = ControllerKey.KEY_APOSTROPHE;
        KEYS[GLFW.GLFW_KEY_COMMA] = ControllerKey.KEY_COMMA;
        KEYS[GLFW.GLFW_KEY_MINUS] = ControllerKey.KEY_MINUS;
        KEYS[GLFW.GLFW_KEY_PERIOD] = ControllerKey.KEY_PERIOD;
        KEYS[GLFW.GLFW_KEY_SLASH] = ControllerKey.KEY_SLASH;
        KEYS[GLFW.GLFW_KEY_0] = ControllerKey.KEY_0;
        KEYS[GLFW.GLFW_KEY_1] = ControllerKey.KEY_1;
        KEYS[GLFW.GLFW_KEY_2] = ControllerKey.KEY_2;
        KEYS[GLFW.GLFW_KEY_3] = ControllerKey.KEY_3;
        KEYS[GLFW.GLFW_KEY_4] = ControllerKey.KEY_4;
        KEYS[GLFW.GLFW_KEY_5] = ControllerKey.KEY_5;
        KEYS[GLFW.GLFW_KEY_6] = ControllerKey.KEY_6;
        KEYS[GLFW.GLFW_KEY_7] = ControllerKey.KEY_7;
        KEYS[GLFW.GLFW_KEY_8] = ControllerKey.KEY_8;
        KEYS[GLFW.GLFW_KEY_9] = ControllerKey.KEY_9;
        KEYS[GLFW.GLFW_KEY_SEMICOLON] = ControllerKey.KEY_SEMICOLON;
        KEYS[GLFW.GLFW_KEY_EQUAL] = ControllerKey.KEY_EQUAL;
        KEYS[GLFW.GLFW_KEY_A] = ControllerKey.KEY_A;
        KEYS[GLFW.GLFW_KEY_B] = ControllerKey.KEY_B;
        KEYS[GLFW.GLFW_KEY_C] = ControllerKey.KEY_C;
        KEYS[GLFW.GLFW_KEY_D] = ControllerKey.KEY_D;
        KEYS[GLFW.GLFW_KEY_E] = ControllerKey.KEY_E;
        KEYS[GLFW.GLFW_KEY_F] = ControllerKey.KEY_F;
        KEYS[GLFW.GLFW_KEY_G] = ControllerKey.KEY_G;
        KEYS[GLFW.GLFW_KEY_H] = ControllerKey.KEY_H;
        KEYS[GLFW.GLFW_KEY_I] = ControllerKey.KEY_I;
        KEYS[GLFW.GLFW_KEY_J] = ControllerKey.KEY_J;
        KEYS[GLFW.GLFW_KEY_K] = ControllerKey.KEY_K;
        KEYS[GLFW.GLFW_KEY_L] = ControllerKey.KEY_L;
        KEYS[GLFW.GLFW_KEY_M] = ControllerKey.KEY_M;
        KEYS[GLFW.GLFW_KEY_N] = ControllerKey.KEY_N;
        KEYS[GLFW.GLFW_KEY_O] = ControllerKey.KEY_O;
        KEYS[GLFW.GLFW_KEY_P] = ControllerKey.KEY_P;
        KEYS[GLFW.GLFW_KEY_Q] = ControllerKey.KEY_Q;
        KEYS[GLFW.GLFW_KEY_R] = ControllerKey.KEY_R;
        KEYS[GLFW.GLFW_KEY_S] = ControllerKey.KEY_S;
        KEYS[GLFW.GLFW_KEY_T] = ControllerKey.KEY_T;
        KEYS[GLFW.GLFW_KEY_U] = ControllerKey.KEY_U;
        KEYS[GLFW.GLFW_KEY_V] = ControllerKey.KEY_V;
        KEYS[GLFW.GLFW_KEY_W] = ControllerKey.KEY_W;
        KEYS[GLFW.GLFW_KEY_X] = ControllerKey.KEY_X;
        KEYS[GLFW.GLFW_KEY_Y] = ControllerKey.KEY_Y;
        KEYS[GLFW.GLFW_KEY_Z] = ControllerKey.KEY_Z;
        KEYS[GLFW.GLFW_KEY_LEFT_BRACKET] = ControllerKey.KEY_LEFT_BRACKET;
        KEYS[GLFW.GLFW_KEY_BACKSLASH] = ControllerKey.KEY_BACKSLASH;
        KEYS[GLFW.GLFW_KEY_RIGHT_BRACKET] = ControllerKey.KEY_RIGHT_BRACKET;
        KEYS[GLFW.GLFW_KEY_GRAVE_ACCENT] = ControllerKey.KEY_GRAVE_ACCENT;
        KEYS[GLFW.GLFW_KEY_WORLD_1] = ControllerKey.KEY_WORLD_1;
        KEYS[GLFW.GLFW_KEY_WORLD_2] = ControllerKey.KEY_WORLD_2;
        KEYS[GLFW.GLFW_KEY_ESCAPE] = ControllerKey.KEY_ESCAPE;
        KEYS[GLFW.GLFW_KEY_ENTER] = ControllerKey.KEY_ENTER;
        KEYS[GLFW.GLFW_KEY_TAB] = ControllerKey.KEY_TAB;
        KEYS[GLFW.GLFW_KEY_BACKSPACE] = ControllerKey.KEY_BACKSPACE;
        KEYS[GLFW.GLFW_KEY_INSERT] = ControllerKey.KEY_INSERT;
        KEYS[GLFW.GLFW_KEY_DELETE] = ControllerKey.KEY_DELETE;
        KEYS[GLFW.GLFW_KEY_RIGHT] = ControllerKey.KEY_RIGHT;
        KEYS[GLFW.GLFW_KEY_LEFT] = ControllerKey.KEY_LEFT;
        KEYS[GLFW.GLFW_KEY_DOWN] = ControllerKey.KEY_DOWN;
        KEYS[GLFW.GLFW_KEY_UP] = ControllerKey.KEY_UP;
        KEYS[GLFW.GLFW_KEY_PAGE_UP] = ControllerKey.KEY_PAGE_UP;
        KEYS[GLFW.GLFW_KEY_PAGE_DOWN] = ControllerKey.KEY_PAGE_DOWN;
        KEYS[GLFW.GLFW_KEY_HOME] = ControllerKey.KEY_HOME;
        KEYS[GLFW.GLFW_KEY_END] = ControllerKey.KEY_END;
        KEYS[GLFW.GLFW_KEY_CAPS_LOCK] = ControllerKey.KEY_CAPS_LOCK;
        KEYS[GLFW.GLFW_KEY_SCROLL_LOCK] = ControllerKey.KEY_SCROLL_LOCK;
        KEYS[GLFW.GLFW_KEY_NUM_LOCK] = ControllerKey.KEY_NUM_LOCK;
        KEYS[GLFW.GLFW_KEY_PRINT_SCREEN] = ControllerKey.KEY_PRINT_SCREEN;
        KEYS[GLFW.GLFW_KEY_PAUSE] = ControllerKey.KEY_PAUSE;
        KEYS[GLFW.GLFW_KEY_F1] = ControllerKey.KEY_F1;
        KEYS[GLFW.GLFW_KEY_F2] = ControllerKey.KEY_F2;
        KEYS[GLFW.GLFW_KEY_F3] = ControllerKey.KEY_F3;
        KEYS[GLFW.GLFW_KEY_F4] = ControllerKey.KEY_F4;
        KEYS[GLFW.GLFW_KEY_F5] = ControllerKey.KEY_F5;
        KEYS[GLFW.GLFW_KEY_F6] = ControllerKey.KEY_F6;
        KEYS[GLFW.GLFW_KEY_F7] = ControllerKey.KEY_F7;
        KEYS[GLFW.GLFW_KEY_F8] = ControllerKey.KEY_F8;
        KEYS[GLFW.GLFW_KEY_F9] = ControllerKey.KEY_F9;
        KEYS[GLFW.GLFW_KEY_F10] = ControllerKey.KEY_F10;
        KEYS[GLFW.GLFW_KEY_F11] = ControllerKey.KEY_F11;
        KEYS[GLFW.GLFW_KEY_F12] = ControllerKey.KEY_F12;
        KEYS[GLFW.GLFW_KEY_F13] = ControllerKey.KEY_F13;
        KEYS[GLFW.GLFW_KEY_F14] = ControllerKey.KEY_F14;
        KEYS[GLFW.GLFW_KEY_F15] = ControllerKey.KEY_F15;
        KEYS[GLFW.GLFW_KEY_F16] = ControllerKey.KEY_F16;
        KEYS[GLFW.GLFW_KEY_F17] = ControllerKey.KEY_F17;
        KEYS[GLFW.GLFW_KEY_F18] = ControllerKey.KEY_F18;
        KEYS[GLFW.GLFW_KEY_F19] = ControllerKey.KEY_F19;
        KEYS[GLFW.GLFW_KEY_F20] = ControllerKey.KEY_F20;
        KEYS[GLFW.GLFW_KEY_F21] = ControllerKey.KEY_F21;
        KEYS[GLFW.GLFW_KEY_F22] = ControllerKey.KEY_F22;
        KEYS[GLFW.GLFW_KEY_F23] = ControllerKey.KEY_F23;
        KEYS[GLFW.GLFW_KEY_F24] = ControllerKey.KEY_F24;
        KEYS[GLFW.GLFW_KEY_F25] = ControllerKey.KEY_F25;
        KEYS[GLFW.GLFW_KEY_KP_0] = ControllerKey.KEY_KP_0;
        KEYS[GLFW.GLFW_KEY_KP_1] = ControllerKey.KEY_KP_1;
        KEYS[GLFW.GLFW_KEY_KP_2] = ControllerKey.KEY_KP_2;
        KEYS[GLFW.GLFW_KEY_KP_3] = ControllerKey.KEY_KP_3;
        KEYS[GLFW.GLFW_KEY_KP_4] = ControllerKey.KEY_KP_4;
        KEYS[GLFW.GLFW_KEY_KP_5] = ControllerKey.KEY_KP_5;
        KEYS[GLFW.GLFW_KEY_KP_6] = ControllerKey.KEY_KP_6;
        KEYS[GLFW.GLFW_KEY_KP_7] = ControllerKey.KEY_KP_7;
        KEYS[GLFW.GLFW_KEY_KP_8] = ControllerKey.KEY_KP_8;
        KEYS[GLFW.GLFW_KEY_KP_9] = ControllerKey.KEY_KP_9;
        KEYS[GLFW.GLFW_KEY_KP_DECIMAL] = ControllerKey.KEY_KP_DECIMAL;
        KEYS[GLFW.GLFW_KEY_KP_DIVIDE] = ControllerKey.KEY_KP_DIVIDE;
        KEYS[GLFW.GLFW_KEY_KP_MULTIPLY] = ControllerKey.KEY_KP_MULTIPLY;
        KEYS[GLFW.GLFW_KEY_KP_SUBTRACT] = ControllerKey.KEY_KP_SUBTRACT;
        KEYS[GLFW.GLFW_KEY_KP_ADD] = ControllerKey.KEY_KP_ADD;
        KEYS[GLFW.GLFW_KEY_KP_ENTER] = ControllerKey.KEY_KP_ENTER;
        KEYS[GLFW.GLFW_KEY_KP_EQUAL] = ControllerKey.KEY_EQUAL;
        KEYS[GLFW.GLFW_KEY_LEFT_SHIFT] = ControllerKey.KEY_LEFT_SHIFT;
        KEYS[GLFW.GLFW_KEY_LEFT_CONTROL] = ControllerKey.KEY_LEFT_CONTROL;
        KEYS[GLFW.GLFW_KEY_LEFT_ALT] = ControllerKey.KEY_LEFT_ALT;
        KEYS[GLFW.GLFW_KEY_LEFT_SUPER] = ControllerKey.KEY_LEFT_SUPER;
        KEYS[GLFW.GLFW_KEY_RIGHT_SHIFT] = ControllerKey.KEY_RIGHT_SHIFT;
        KEYS[GLFW.GLFW_KEY_RIGHT_CONTROL] = ControllerKey.KEY_RIGHT_CONTROL;
        KEYS[GLFW.GLFW_KEY_RIGHT_ALT] = ControllerKey.KEY_RIGHT_ALT;
        KEYS[GLFW.GLFW_KEY_RIGHT_SUPER] = ControllerKey.KEY_RIGHT_SUPER;
        KEYS[GLFW.GLFW_KEY_MENU] = ControllerKey.KEY_MENU;
    }

    public static ControllerKey key(int id) {
        if (id < 0 || id >= KEYS.length) {
            return ControllerKey.UNKNOWN;
        }
        return KEYS[id];
    }
}
