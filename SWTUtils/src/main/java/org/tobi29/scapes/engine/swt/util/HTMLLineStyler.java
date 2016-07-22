package org.tobi29.scapes.engine.swt.util;

import java8.util.Optional;
import java8.util.function.BiConsumer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.tobi29.scapes.engine.utils.Triple;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HTMLLineStyler
        implements DisposeListener, LineStyleListener, VerifyListener {
    private static final XMLInputFactory FACTORY =
            XMLInputFactory.newInstance();
    private final Set<StyleRange> styles =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, Color> colorTable = new ConcurrentHashMap<>();

    public HTMLLineStyler(StyledText text) {
        this(text.getDisplay());
        text.addLineStyleListener(this);
        text.addVerifyListener(this);
        text.addDisposeListener(this);
    }

    public HTMLLineStyler(Display display) {
        colorTable.put("black", display.getSystemColor(SWT.COLOR_BLACK));
        colorTable.put("white", display.getSystemColor(SWT.COLOR_WHITE));
        colorTable.put("darkred", display.getSystemColor(SWT.COLOR_DARK_RED));
        colorTable
                .put("darkgreen", display.getSystemColor(SWT.COLOR_DARK_GREEN));
        colorTable.put("darkyellow",
                display.getSystemColor(SWT.COLOR_DARK_YELLOW));
        colorTable.put("darkblue", display.getSystemColor(SWT.COLOR_DARK_BLUE));
        colorTable.put("darkmagenta",
                display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
        colorTable.put("darkcyan", display.getSystemColor(SWT.COLOR_DARK_CYAN));
        colorTable.put("darkgray", display.getSystemColor(SWT.COLOR_DARK_GRAY));
        colorTable.put("red", display.getSystemColor(SWT.COLOR_RED));
        colorTable.put("green", display.getSystemColor(SWT.COLOR_GREEN));
        colorTable.put("yellow", display.getSystemColor(SWT.COLOR_YELLOW));
        colorTable.put("blue", display.getSystemColor(SWT.COLOR_BLUE));
        colorTable.put("magenta", display.getSystemColor(SWT.COLOR_MAGENTA));
        colorTable.put("cyan", display.getSystemColor(SWT.COLOR_CYAN));
        colorTable.put("gray", display.getSystemColor(SWT.COLOR_GRAY));
    }

    @Override
    public void lineGetStyle(LineStyleEvent event) {
        int start = event.lineOffset;
        int end = event.lineOffset + event.lineText.length();
        event.styles = styles.stream().filter(style -> {
            int styleStart = style.start;
            int styleEnd = styleStart + style.length;
            return !(end <= styleStart || start >= styleEnd);
        }).toArray(StyleRange[]::new);
    }

    @Override
    public void verifyText(VerifyEvent e) {
        try {
            XMLStreamReader reader =
                    FACTORY.createXMLStreamReader(new StringReader(e.text));
            StringBuilder output = new StringBuilder(e.text.length());
            Deque<Triple<Integer, String, BiConsumer<StyleRange, StringBuilder>>>
                    styles = new ArrayDeque<>();
            while (reader.hasNext()) {
                int event = reader.getEventType();
                switch (event) {
                    case XMLStreamConstants.CHARACTERS:
                        output.append(reader.getText());
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        String type = reader.getName().getLocalPart();
                        switch (type) {
                            case "p":
                            case "span":
                            case "b":
                                int start = e.start + output.length();
                                styles.add(new Triple<>(start, type,
                                        addStyle(reader)));
                                break;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        type = reader.getName().getLocalPart();
                        switch (type) {
                            case "p":
                            case "span":
                            case "b":
                                int end = e.start + output.length();
                                Iterator<Triple<Integer, String, BiConsumer<StyleRange, StringBuilder>>>
                                        iterator = styles.descendingIterator();
                                Triple<Integer, String, BiConsumer<StyleRange, StringBuilder>>
                                        style = null;
                                while (iterator.hasNext()) {
                                    Triple<Integer, String, BiConsumer<StyleRange, StringBuilder>>
                                            next = iterator.next();
                                    if (next.b.equals(type)) {
                                        style = next;
                                        break;
                                    }
                                }
                                if (style == null) {
                                    break;
                                }
                                StyleRange styleRange = new StyleRange();
                                styleRange.start = style.a;
                                styleRange.length = end - style.a;
                                style.c.accept(styleRange, output);
                                this.styles.add(styleRange);
                                break;
                        }
                        break;
                }
                reader.next();
            }
            e.text = output.toString();
        } catch (XMLStreamException e1) {
            e.doit = false;
        }
    }

    private BiConsumer<StyleRange, StringBuilder> addStyle(
            XMLStreamReader reader) {
        String type = reader.getName().getLocalPart();
        int count = reader.getAttributeCount();
        Properties style = new Properties();
        for (int i = 0; i < count; i++) {
            if ("style".equals(reader.getAttributeName(i).getLocalPart())) {
                String[] attribute = reader.getAttributeValue(i).split(";");
                for (String property : attribute) {
                    String[] split = property.split(":", 2);
                    if (split.length == 2) {
                        style.setProperty(split[0].trim(), split[1].trim());
                    }
                }
            }
        }
        String colorName = style.getProperty("color");
        Optional<Color> color;
        if (colorName == null) {
            color = Optional.empty();
        } else {
            color = Optional.ofNullable(colorTable.get(colorName));
        }
        switch (type) {
            case "p":
            case "span":
                return (styleRange, text) -> {
                    color.ifPresent(c -> styleRange.foreground = c);
                    text.append('\n');
                };
            case "b":
                return (styleRange, text) -> {
                    color.ifPresent(c -> styleRange.foreground = c);
                    styleRange.fontStyle |= SWT.BOLD;
                };
        }
        return (styleRange, text) -> {
        };
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
    }
}
