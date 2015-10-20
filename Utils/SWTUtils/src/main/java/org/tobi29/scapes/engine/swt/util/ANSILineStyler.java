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

package org.tobi29.scapes.engine.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANSILineStyler implements LineStyleListener, VerifyListener {
    private final Pattern controlSequencePattern =
            Pattern.compile("((\u001B\\[)(\\d+;)*(\\d+)?[m])");
    private final ArrayList<StyleRange> queuedStyles = new ArrayList<>();
    private final Color[] colorTable = new Color[20];
    private StyleRange lastStyle;

    public ANSILineStyler(StyledText styledText) {
        Display display = styledText.getDisplay();
        colorTable[0] = display.getSystemColor(SWT.COLOR_BLACK);
        colorTable[1] = display.getSystemColor(SWT.COLOR_DARK_RED);
        colorTable[2] = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        colorTable[3] = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
        colorTable[4] = display.getSystemColor(SWT.COLOR_DARK_BLUE);
        colorTable[5] = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
        colorTable[6] = display.getSystemColor(SWT.COLOR_DARK_CYAN);
        colorTable[7] = display.getSystemColor(SWT.COLOR_GRAY);
        colorTable[10] = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        colorTable[11] = display.getSystemColor(SWT.COLOR_RED);
        colorTable[12] = display.getSystemColor(SWT.COLOR_GREEN);
        colorTable[13] = display.getSystemColor(SWT.COLOR_YELLOW);
        colorTable[14] = display.getSystemColor(SWT.COLOR_BLUE);
        colorTable[15] = display.getSystemColor(SWT.COLOR_MAGENTA);
        colorTable[16] = display.getSystemColor(SWT.COLOR_CYAN);
        colorTable[17] = display.getSystemColor(SWT.COLOR_WHITE);
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    @Override
    public void lineGetStyle(LineStyleEvent event) {
        int start = event.lineOffset;
        int end = event.lineOffset + event.lineText.length();
        StyleRange firstStyle = null;
        ArrayList<StyleRange> applicableStyles = new ArrayList<>();
        for (int i = 0; i < queuedStyles.size(); i++) {
            StyleRange currentStyle = queuedStyles.get(i);
            if (currentStyle.start >= start && currentStyle.start <= end) {
                applicableStyles.add(currentStyle);
                queuedStyles.remove(i);
                if (firstStyle == null ||
                        currentStyle.start < firstStyle.start) {
                    firstStyle = currentStyle;
                }
                i--;
            }
        }
        if (lastStyle != null) {
            StyleRange initialStyle = new StyleRange();
            initialStyle.start = start;
            if (firstStyle == null) {
                initialStyle.length = end - start;
            } else {
                initialStyle.length = firstStyle.start - start - 1;
            }
            if (initialStyle.length > 0) {
                if (lastStyle.fontStyle != SWT.NORMAL ||
                        lastStyle.foreground != null ||
                        lastStyle.background != null ||
                        lastStyle.underline) {
                    initialStyle.fontStyle = lastStyle.fontStyle;
                    initialStyle.foreground = lastStyle.foreground;
                    initialStyle.background = lastStyle.background;
                    initialStyle.underline = lastStyle.underline;
                    initialStyle.underlineStyle = lastStyle.underlineStyle;
                    initialStyle.underlineColor = lastStyle.underlineColor;
                    applicableStyles.add(initialStyle);
                    lastStyle = initialStyle;
                }
            }
        }
        StyleRange[] styles = new StyleRange[applicableStyles.size()];
        for (int i = 0; i < applicableStyles.size(); i++) {
            styles[i] = applicableStyles.get(i);
        }
        event.styles = styles;
    }

    @Override
    public void verifyText(VerifyEvent e) {
        StringBuilder buffer = new StringBuilder(e.text);
        StringBuilder remainder = new StringBuilder(e.text.length());
        int offset = e.start;
        while (buffer.length() > 0) {
            byte[] bytes = buffer.toString().getBytes();
            if (bytes[0] == '\u001B') {
                Matcher matcher = controlSequencePattern.matcher(buffer);
                if (matcher.find() && matcher.start() == 0) {
                    String controlSequence = matcher.group();
                    buffer.replace(0, controlSequence.length(), "");
                    matcher = controlSequencePattern.matcher(buffer);
                    int start = offset + remainder.length();
                    int length = 0;
                    if (matcher.find()) {
                        length = matcher.start();
                    } else {
                        length = buffer.length();
                    }
                    char sequenceType = parseSequenceType(controlSequence);
                    int[] codes = parseSequenceCodes(controlSequence);
                    if (sequenceType == 'm') {
                        StyleRange newStyleRange =
                                buildStyleRange(start, length, codes);
                        queuedStyles.add(newStyleRange);
                        lastStyle = newStyleRange;
                    }
                } else {
                    remainder.append(buffer.charAt(0));
                    buffer.deleteCharAt(0);
                }
            } else {
                remainder.append(buffer.charAt(0));
                buffer.deleteCharAt(0);
            }
        }
        e.text = remainder.toString();
    }

    private StyleRange buildStyleRange(int start, int length, int[] codes) {
        StyleRange newStyleRange = new StyleRange();
        newStyleRange.start = start;
        newStyleRange.length = length;
        for (int code : codes) {
            Color tempColor = null;
            switch (code) {
                case 0:
                    newStyleRange.foreground = null;
                    newStyleRange.background = null;
                    newStyleRange.fontStyle = SWT.NORMAL;
                    newStyleRange.underline = false;
                    newStyleRange.underlineStyle = SWT.UNDERLINE_SINGLE;
                    break;
                case 1:
                    newStyleRange.fontStyle = SWT.BOLD;
                    break;
                case 2:
                    // It's actually supposed to be faint, but there's no way to display that
                    newStyleRange.fontStyle = SWT.NORMAL;
                    break;
                case 3:
                    newStyleRange.fontStyle = SWT.ITALIC;
                    break;
                case 4:
                    newStyleRange.underline = true;
                    newStyleRange.underlineStyle = SWT.UNDERLINE_SINGLE;
                    break;
                case 7:
                    // Swap foreground and background
                    tempColor = newStyleRange.foreground;
                    newStyleRange.foreground = newStyleRange.background;
                    newStyleRange.background = tempColor;
                    break;
                case 21:
                    newStyleRange.underline = true;
                    newStyleRange.underlineStyle = SWT.UNDERLINE_DOUBLE;
                    break;
                case 22:
                    newStyleRange.fontStyle = SWT.NORMAL;
                    break;
                case 24:
                    newStyleRange.underline = false;
                    newStyleRange.underlineStyle = 0;
                    break;
                case 27:
                    // Technically, this should just unset reversed foreground, but we're
                    // just going to reverse again
                    tempColor = newStyleRange.foreground;
                    newStyleRange.foreground = newStyleRange.background;
                    newStyleRange.background = tempColor;
                    break;
                default:
                    if (code >= 30 && code < 40) {
                        newStyleRange.foreground = colorTable[code - 30];
                    } else if (code >= 40 && code < 50) {
                        newStyleRange.background = colorTable[code - 40];
                    } else if (code >= 90 && code < 100) {
                        newStyleRange.foreground = colorTable[code - 90 + 10];
                    } else if (code >= 100 && code < 110) {
                        newStyleRange.background = colorTable[code - 100 + 10];
                    }
                    break;
            }
        }
        return newStyleRange;
    }

    private int[] parseSequenceCodes(String controlSequence) {
        String codeSequence = null;
        if (controlSequence.charAt(0) == '\u009B') {
            codeSequence =
                    controlSequence.substring(1, controlSequence.length() - 1);
        } else if (controlSequence.charAt(0) == '\u001B') {
            codeSequence =
                    controlSequence.substring(2, controlSequence.length() - 1);
        } else {
            return new int[]{0};
        }
        String[] codeStrings = codeSequence.split(";");
        if (codeStrings.length == 0) {
            return new int[]{0};
        } else {
            int[] codes = new int[codeStrings.length];
            for (int i = 0; i < codeStrings.length; i++) {
                if (codeStrings[i] != null && codeStrings[i].isEmpty()) {
                    codes[i] = 0;
                } else {
                    codes[i] = Integer.parseInt(codeStrings[i]);
                }
            }
            return codes;
        }
    }

    private char parseSequenceType(String controlSequence) {
        return controlSequence.charAt(controlSequence.length() - 1);
    }
}