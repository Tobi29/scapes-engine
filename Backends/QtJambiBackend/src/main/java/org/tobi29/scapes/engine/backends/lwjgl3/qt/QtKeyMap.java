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

import com.trolltech.qt.core.Qt;
import org.tobi29.scapes.engine.input.ControllerKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QtKeyMap {
    private static final Map<Qt.MouseButton, ControllerKey> BUTTONS =
            new ConcurrentHashMap<>();
    private static final Map<Qt.Key, ControllerKey> KEYS =
            new ConcurrentHashMap<>();

    static {
        // Mouse mappings
        BUTTONS.put(Qt.MouseButton.LeftButton, ControllerKey.BUTTON_0);
        BUTTONS.put(Qt.MouseButton.RightButton, ControllerKey.BUTTON_1);
        BUTTONS.put(Qt.MouseButton.MidButton, ControllerKey.BUTTON_2);
        BUTTONS.put(Qt.MouseButton.XButton1, ControllerKey.BUTTON_3);
        BUTTONS.put(Qt.MouseButton.XButton2, ControllerKey.BUTTON_4);
        // Keyboard mappings
        KEYS.put(Qt.Key.Key_Space, ControllerKey.KEY_SPACE);
        KEYS.put(Qt.Key.Key_Apostrophe, ControllerKey.KEY_APOSTROPHE);
        KEYS.put(Qt.Key.Key_Comma, ControllerKey.KEY_COMMA);
        KEYS.put(Qt.Key.Key_Minus, ControllerKey.KEY_MINUS);
        KEYS.put(Qt.Key.Key_Period, ControllerKey.KEY_PERIOD);
        KEYS.put(Qt.Key.Key_Slash, ControllerKey.KEY_SLASH);
        KEYS.put(Qt.Key.Key_0, ControllerKey.KEY_0);
        KEYS.put(Qt.Key.Key_1, ControllerKey.KEY_1);
        KEYS.put(Qt.Key.Key_2, ControllerKey.KEY_2);
        KEYS.put(Qt.Key.Key_3, ControllerKey.KEY_3);
        KEYS.put(Qt.Key.Key_4, ControllerKey.KEY_4);
        KEYS.put(Qt.Key.Key_5, ControllerKey.KEY_5);
        KEYS.put(Qt.Key.Key_6, ControllerKey.KEY_6);
        KEYS.put(Qt.Key.Key_7, ControllerKey.KEY_7);
        KEYS.put(Qt.Key.Key_8, ControllerKey.KEY_8);
        KEYS.put(Qt.Key.Key_9, ControllerKey.KEY_9);
        KEYS.put(Qt.Key.Key_Semicolon, ControllerKey.KEY_SEMICOLON);
        KEYS.put(Qt.Key.Key_Equal, ControllerKey.KEY_EQUAL);
        KEYS.put(Qt.Key.Key_A, ControllerKey.KEY_A);
        KEYS.put(Qt.Key.Key_B, ControllerKey.KEY_B);
        KEYS.put(Qt.Key.Key_C, ControllerKey.KEY_C);
        KEYS.put(Qt.Key.Key_D, ControllerKey.KEY_D);
        KEYS.put(Qt.Key.Key_E, ControllerKey.KEY_E);
        KEYS.put(Qt.Key.Key_F, ControllerKey.KEY_F);
        KEYS.put(Qt.Key.Key_G, ControllerKey.KEY_G);
        KEYS.put(Qt.Key.Key_H, ControllerKey.KEY_H);
        KEYS.put(Qt.Key.Key_I, ControllerKey.KEY_I);
        KEYS.put(Qt.Key.Key_J, ControllerKey.KEY_J);
        KEYS.put(Qt.Key.Key_K, ControllerKey.KEY_K);
        KEYS.put(Qt.Key.Key_L, ControllerKey.KEY_L);
        KEYS.put(Qt.Key.Key_M, ControllerKey.KEY_M);
        KEYS.put(Qt.Key.Key_N, ControllerKey.KEY_N);
        KEYS.put(Qt.Key.Key_O, ControllerKey.KEY_O);
        KEYS.put(Qt.Key.Key_P, ControllerKey.KEY_P);
        KEYS.put(Qt.Key.Key_Q, ControllerKey.KEY_Q);
        KEYS.put(Qt.Key.Key_R, ControllerKey.KEY_R);
        KEYS.put(Qt.Key.Key_S, ControllerKey.KEY_S);
        KEYS.put(Qt.Key.Key_T, ControllerKey.KEY_T);
        KEYS.put(Qt.Key.Key_U, ControllerKey.KEY_U);
        KEYS.put(Qt.Key.Key_V, ControllerKey.KEY_V);
        KEYS.put(Qt.Key.Key_W, ControllerKey.KEY_W);
        KEYS.put(Qt.Key.Key_X, ControllerKey.KEY_X);
        KEYS.put(Qt.Key.Key_Y, ControllerKey.KEY_Y);
        KEYS.put(Qt.Key.Key_Z, ControllerKey.KEY_Z);
        KEYS.put(Qt.Key.Key_BracketLeft, ControllerKey.KEY_LEFT_BRACKET);
        KEYS.put(Qt.Key.Key_Backslash, ControllerKey.KEY_BACKSLASH);
        KEYS.put(Qt.Key.Key_BraceRight, ControllerKey.KEY_RIGHT_BRACKET);
        // KEYS.put(Qt.Key.Key_GRAVE_ACCENT, ControllerKey.KEY_GRAVE_ACCENT);
        // KEYS.put(Qt.Key.Key_WORLD_1, ControllerKey.KEY_WORLD_1);
        // KEYS.put(Qt.Key.Key_WORLD_2, ControllerKey.KEY_WORLD_2);
        KEYS.put(Qt.Key.Key_Escape, ControllerKey.KEY_ESCAPE);
        KEYS.put(Qt.Key.Key_Enter, ControllerKey.KEY_ENTER);
        KEYS.put(Qt.Key.Key_Return, ControllerKey.KEY_ENTER);
        KEYS.put(Qt.Key.Key_Tab, ControllerKey.KEY_TAB);
        KEYS.put(Qt.Key.Key_Backspace, ControllerKey.KEY_BACKSPACE);
        KEYS.put(Qt.Key.Key_Insert, ControllerKey.KEY_INSERT);
        KEYS.put(Qt.Key.Key_Delete, ControllerKey.KEY_DELETE);
        KEYS.put(Qt.Key.Key_Right, ControllerKey.KEY_RIGHT);
        KEYS.put(Qt.Key.Key_Left, ControllerKey.KEY_LEFT);
        KEYS.put(Qt.Key.Key_Down, ControllerKey.KEY_DOWN);
        KEYS.put(Qt.Key.Key_Up, ControllerKey.KEY_UP);
        KEYS.put(Qt.Key.Key_PageUp, ControllerKey.KEY_PAGE_UP);
        KEYS.put(Qt.Key.Key_PageDown, ControllerKey.KEY_PAGE_DOWN);
        KEYS.put(Qt.Key.Key_Home, ControllerKey.KEY_HOME);
        KEYS.put(Qt.Key.Key_End, ControllerKey.KEY_END);
        KEYS.put(Qt.Key.Key_CapsLock, ControllerKey.KEY_CAPS_LOCK);
        KEYS.put(Qt.Key.Key_ScrollLock, ControllerKey.KEY_SCROLL_LOCK);
        KEYS.put(Qt.Key.Key_NumLock, ControllerKey.KEY_NUM_LOCK);
        KEYS.put(Qt.Key.Key_Print, ControllerKey.KEY_PRINT_SCREEN);
        KEYS.put(Qt.Key.Key_Pause, ControllerKey.KEY_PAUSE);
        KEYS.put(Qt.Key.Key_F1, ControllerKey.KEY_F1);
        KEYS.put(Qt.Key.Key_F2, ControllerKey.KEY_F2);
        KEYS.put(Qt.Key.Key_F3, ControllerKey.KEY_F3);
        KEYS.put(Qt.Key.Key_F4, ControllerKey.KEY_F4);
        KEYS.put(Qt.Key.Key_F5, ControllerKey.KEY_F5);
        KEYS.put(Qt.Key.Key_F6, ControllerKey.KEY_F6);
        KEYS.put(Qt.Key.Key_F7, ControllerKey.KEY_F7);
        KEYS.put(Qt.Key.Key_F8, ControllerKey.KEY_F8);
        KEYS.put(Qt.Key.Key_F9, ControllerKey.KEY_F9);
        KEYS.put(Qt.Key.Key_F10, ControllerKey.KEY_F10);
        KEYS.put(Qt.Key.Key_F11, ControllerKey.KEY_F11);
        KEYS.put(Qt.Key.Key_F12, ControllerKey.KEY_F12);
        KEYS.put(Qt.Key.Key_F13, ControllerKey.KEY_F13);
        KEYS.put(Qt.Key.Key_F14, ControllerKey.KEY_F14);
        KEYS.put(Qt.Key.Key_F15, ControllerKey.KEY_F15);
        KEYS.put(Qt.Key.Key_F16, ControllerKey.KEY_F16);
        KEYS.put(Qt.Key.Key_F17, ControllerKey.KEY_F17);
        KEYS.put(Qt.Key.Key_F18, ControllerKey.KEY_F18);
        KEYS.put(Qt.Key.Key_F19, ControllerKey.KEY_F19);
        KEYS.put(Qt.Key.Key_F20, ControllerKey.KEY_F20);
        KEYS.put(Qt.Key.Key_F21, ControllerKey.KEY_F21);
        KEYS.put(Qt.Key.Key_F22, ControllerKey.KEY_F22);
        KEYS.put(Qt.Key.Key_F23, ControllerKey.KEY_F23);
        KEYS.put(Qt.Key.Key_F24, ControllerKey.KEY_F24);
        KEYS.put(Qt.Key.Key_F25, ControllerKey.KEY_F25);
        KEYS.put(Qt.Key.Key_Shift, ControllerKey.KEY_LEFT_SHIFT);
        KEYS.put(Qt.Key.Key_Control, ControllerKey.KEY_LEFT_CONTROL);
        KEYS.put(Qt.Key.Key_Alt, ControllerKey.KEY_LEFT_ALT);
        KEYS.put(Qt.Key.Key_Super_L, ControllerKey.KEY_LEFT_SUPER);
        KEYS.put(Qt.Key.Key_Super_R, ControllerKey.KEY_RIGHT_SUPER);
        KEYS.put(Qt.Key.Key_Menu, ControllerKey.KEY_MENU);
    }

    public static ControllerKey button(Qt.MouseButton button) {
        return BUTTONS.getOrDefault(button, ControllerKey.UNKNOWN);
    }

    public static ControllerKey key(Qt.Key key) {
        return KEYS.getOrDefault(key, ControllerKey.UNKNOWN);
    }
}
