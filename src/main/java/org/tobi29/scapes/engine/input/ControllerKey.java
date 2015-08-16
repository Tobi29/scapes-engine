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

package org.tobi29.scapes.engine.input;

import java.util.ArrayList;
import java.util.List;

public enum ControllerKey {
    UNKNOWN("Unknown"),
    KEY_SPACE("Space"),
    KEY_APOSTROPHE("Apostrophe"),
    KEY_COMMA("Comma"),
    KEY_MINUS("Minus"),
    KEY_PERIOD("Period"),
    KEY_SLASH("Slash"),
    KEY_0("0"),
    KEY_1("1"),
    KEY_2("2"),
    KEY_3("3"),
    KEY_4("4"),
    KEY_5("5"),
    KEY_6("6"),
    KEY_7("7"),
    KEY_8("8"),
    KEY_9("9"),
    KEY_SEMICOLON("Semicolon"),
    KEY_EQUAL("Equal"),
    KEY_A("A"),
    KEY_B("B"),
    KEY_C("C"),
    KEY_D("D"),
    KEY_E("E"),
    KEY_F("F"),
    KEY_G("G"),
    KEY_H("H"),
    KEY_I("I"),
    KEY_J("J"),
    KEY_K("K"),
    KEY_L("L"),
    KEY_M("M"),
    KEY_N("N"),
    KEY_O("O"),
    KEY_P("P"),
    KEY_Q("Q"),
    KEY_R("R"),
    KEY_S("S"),
    KEY_T("T"),
    KEY_U("U"),
    KEY_V("V"),
    KEY_W("W"),
    KEY_X("X"),
    KEY_Y("Y"),
    KEY_Z("Z"),
    KEY_LEFT_BRACKET("Left Bracket"),
    KEY_BACKSLASH("Backslash"),
    KEY_RIGHT_BRACKET("Right Bracket"),
    KEY_GRAVE_ACCENT("Grave Accent"),
    KEY_WORLD_1("World 1"),
    KEY_WORLD_2("World 2"),
    KEY_ESCAPE("Escape"),
    KEY_ENTER("Enter"),
    KEY_TAB("Tab"),
    KEY_BACKSPACE("Backspace"),
    KEY_INSERT("Insert"),
    KEY_DELETE("Delete"),
    KEY_RIGHT("Right"),
    KEY_LEFT("Left"),
    KEY_DOWN("Down"),
    KEY_UP("Up"),
    KEY_PAGE_UP("Page Up"),
    KEY_PAGE_DOWN("Page Down"),
    KEY_HOME("Home"),
    KEY_END("End"),
    KEY_CAPS_LOCK("Caps Lock"),
    KEY_SCROLL_LOCK("Scroll Lock"),
    KEY_NUM_LOCK("Num Lock"),
    KEY_PRINT_SCREEN("Print Screen"),
    KEY_PAUSE("Pause"),
    KEY_F1("F1"),
    KEY_F2("F2"),
    KEY_F3("F3"),
    KEY_F4("F4"),
    KEY_F5("F5"),
    KEY_F6("F6"),
    KEY_F7("F7"),
    KEY_F8("F8"),
    KEY_F9("F9"),
    KEY_F10("F10"),
    KEY_F11("F11"),
    KEY_F12("F12"),
    KEY_F13("F13"),
    KEY_F14("F14"),
    KEY_F15("F15"),
    KEY_F16("F16"),
    KEY_F17("F17"),
    KEY_F18("F18"),
    KEY_F19("F19"),
    KEY_F20("F20"),
    KEY_F21("F21"),
    KEY_F22("F22"),
    KEY_F23("F23"),
    KEY_F24("F24"),
    KEY_F25("F25"),
    KEY_KP_0("KP 0"),
    KEY_KP_1("KP 1"),
    KEY_KP_2("KP 2"),
    KEY_KP_3("KP 3"),
    KEY_KP_4("KP 4"),
    KEY_KP_5("KP 5"),
    KEY_KP_6("KP 6"),
    KEY_KP_7("KP 7"),
    KEY_KP_8("KP 8"),
    KEY_KP_9("KP 9"),
    KEY_KP_DECIMAL("KP Decimal"),
    KEY_KP_DIVIDE("KP Divide"),
    KEY_KP_MULTIPLY("KP Multiply"),
    KEY_KP_SUBTRACT("KP Subtract"),
    KEY_KP_ADD("KP Add"),
    KEY_KP_ENTER("KP Enter"),
    KEY_KP_EQUAL("KP Enter"),
    KEY_LEFT_SHIFT("Left Shift"),
    KEY_LEFT_CONTROL("Left Control"),
    KEY_LEFT_ALT("Left Alt"),
    KEY_LEFT_SUPER("Left Super"),
    KEY_RIGHT_SHIFT("Right Shift"),
    KEY_RIGHT_CONTROL("Right Control"),
    KEY_RIGHT_ALT("Right Alt"),
    KEY_RIGHT_SUPER("Right Super"),
    KEY_MENU("Menu"),
    BUTTON_0("Button 0"),
    BUTTON_1("Button 1"),
    BUTTON_2("Button 2"),
    BUTTON_3("Button 3"),
    BUTTON_4("Button 4"),
    BUTTON_5("Button 5"),
    BUTTON_6("Button 6"),
    BUTTON_7("Button 7"),
    BUTTON_8("Button 8"),
    BUTTON_9("Button 9"),
    BUTTON_10("Button 10"),
    BUTTON_11("Button 11"),
    BUTTON_12("Button 12"),
    BUTTON_13("Button 13"),
    BUTTON_14("Button 14"),
    BUTTON_15("Button 15"),
    BUTTON_16("Button 16"),
    BUTTON_17("Button 17"),
    BUTTON_18("Button 18"),
    BUTTON_19("Button 19"),
    BUTTON_20("Button 20"),
    BUTTON_21("Button 21"),
    BUTTON_22("Button 22"),
    BUTTON_23("Button 23"),
    BUTTON_24("Button 24"),
    BUTTON_25("Button 25"),
    BUTTON_26("Button 26"),
    BUTTON_27("Button 27"),
    BUTTON_28("Button 28"),
    BUTTON_29("Button 29"),
    BUTTON_30("Button 30"),
    BUTTON_31("Button 31"),
    BUTTON_32("Button 32"),
    BUTTON_33("Button 33"),
    BUTTON_34("Button 34"),
    BUTTON_35("Button 35"),
    BUTTON_36("Button 36"),
    BUTTON_37("Button 37"),
    BUTTON_38("Button 38"),
    BUTTON_39("Button 39"),
    BUTTON_40("Button 40"),
    BUTTON_41("Button 41"),
    BUTTON_42("Button 42"),
    BUTTON_43("Button 43"),
    BUTTON_44("Button 44"),
    BUTTON_45("Button 45"),
    BUTTON_46("Button 46"),
    BUTTON_47("Button 47"),
    BUTTON_48("Button 48"),
    BUTTON_49("Button 49"),
    BUTTON_50("Button 50"),
    BUTTON_51("Button 51"),
    BUTTON_52("Button 52"),
    BUTTON_53("Button 53"),
    BUTTON_54("Button 54"),
    BUTTON_55("Button 55"),
    BUTTON_56("Button 56"),
    BUTTON_57("Button 57"),
    BUTTON_58("Button 58"),
    BUTTON_59("Button 59"),
    BUTTON_60("Button 60"),
    BUTTON_61("Button 61"),
    BUTTON_62("Button 62"),
    BUTTON_63("Button 63"),
    BUTTON_64("Button 64"),
    BUTTON_65("Button 65"),
    BUTTON_66("Button 66"),
    BUTTON_67("Button 67"),
    BUTTON_68("Button 68"),
    BUTTON_69("Button 69"),
    BUTTON_70("Button 70"),
    BUTTON_71("Button 71"),
    BUTTON_72("Button 72"),
    BUTTON_73("Button 73"),
    BUTTON_74("Button 74"),
    BUTTON_75("Button 75"),
    BUTTON_76("Button 76"),
    BUTTON_77("Button 77"),
    BUTTON_78("Button 78"),
    BUTTON_79("Button 79"),
    SCROLL_DOWN("Scroll Down"),
    SCROLL_UP("Scroll Up"),
    SCROLL_LEFT("Scroll Left"),
    SCROLL_RIGHT("Scroll Right"),
    AXIS_0("Axis 0"),
    AXIS_1("Axis 1"),
    AXIS_2("Axis 2"),
    AXIS_3("Axis 3"),
    AXIS_4("Axis 4"),
    AXIS_5("Axis 5"),
    AXIS_6("Axis 6"),
    AXIS_7("Axis 7"),
    AXIS_8("Axis 8"),
    AXIS_9("Axis 9"),
    AXIS_10("Axis 10"),
    AXIS_11("Axis 11"),
    AXIS_12("Axis 12"),
    AXIS_13("Axis 13"),
    AXIS_14("Axis 14"),
    AXIS_15("Axis 15");
    public static final ControllerKey BUTTON_LEFT = BUTTON_0;
    public static final ControllerKey BUTTON_RIGHT = BUTTON_1;
    private static final List<ControllerKey> BUTTONS = new ArrayList<>(), AXES =
            new ArrayList<>();

    static {
        int id = 0;
        for (ControllerKey key : values()) {
            key.id = id++;
        }
        for (int i = 0; i < 80; i++) {
            BUTTONS.add(valueOf("BUTTON_" + i));
        }
        for (int i = 0; i < 16; i++) {
            AXES.add(valueOf("AXIS_" + i));
        }
    }

    private final String name;
    private int id;

    ControllerKey(String name) {
        this.name = name;
    }

    public static ControllerKey button(int i) {
        if (i < 0 || i >= BUTTONS.size()) {
            return UNKNOWN;
        }
        return BUTTONS.get(i);
    }

    public static ControllerKey axis(int i) {
        if (i < 0 || i >= AXES.size()) {
            return UNKNOWN;
        }
        return AXES.get(i);
    }

    public String humanName() {
        return name;
    }

    public int id() {
        return id;
    }
}
