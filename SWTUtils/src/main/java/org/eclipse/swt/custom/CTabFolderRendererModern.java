/*
******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Lars Vogel <Lars.Vogel@vogella.com> - Bug 455263
 *******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class CTabFolderRendererModern extends CTabFolderRenderer {
    public CTabFolderRendererModern(CTabFolder parent) {
        super(parent);
    }

    @Override
    protected void draw(int part, int state, Rectangle bounds, GC gc) {
        switch (part) {
            case PART_BACKGROUND:
                drawBackground(gc, bounds, state);
                break;
            case PART_BODY:
                drawBody(gc, bounds, state);
                break;
            case PART_HEADER:
                drawTabArea(gc, bounds, state);
                break;
            case PART_MAX_BUTTON:
                drawMaximize(gc, bounds, state);
                break;
            case PART_MIN_BUTTON:
                drawMinimize(gc, bounds, state);
                break;
            case PART_CHEVRON_BUTTON:
                drawChevron(gc, bounds, state);
                break;
            default:
                if (part >= 0 && part < parent.getItemCount()) {
                    if (bounds.width == 0 || bounds.height == 0) {
                        return;
                    }
                    if ((state & SWT.SELECTED) != 0) {
                        drawSelected(part, gc, bounds, state);
                    } else {
                        drawUnselected(part, gc, bounds, state);
                    }
                }
                break;
        }
    }

    @Override
    void drawBackground(GC gc, Rectangle bounds, int state) {
        boolean selected = (state & SWT.SELECTED) != 0;
        Color defaultBackground =
                selected ? parent.selectionBackground : parent.getBackground();
        Image image = selected ? parent.selectionBgImage : null;
        Color[] colors = selected ? parent.selectionGradientColors :
                parent.gradientColors;
        int[] percents = selected ? parent.selectionGradientPercents :
                parent.gradientPercents;
        boolean vertical = selected ? parent.selectionGradientVertical :
                parent.gradientVertical;

        drawBackground(gc, null, bounds.x, bounds.y, bounds.width,
                bounds.height, defaultBackground, image, colors, percents,
                vertical);
    }

    @Override
    void drawBackground(GC gc, int[] shape, boolean selected) {
        Color defaultBackground =
                selected ? parent.selectionBackground : parent.getBackground();
        Image image = selected ? parent.selectionBgImage : null;
        Color[] colors = selected ? parent.selectionGradientColors :
                parent.gradientColors;
        int[] percents = selected ? parent.selectionGradientPercents :
                parent.gradientPercents;
        boolean vertical = selected ? parent.selectionGradientVertical :
                parent.gradientVertical;
        Point size = parent.getSize();
        int width = size.x;
        int height = parent.tabHeight +
                ((parent.getStyle() & SWT.FLAT) != 0 ? 1 : 3);
        int x = 0;

        int borderLeft = parent.borderVisible ? 1 : 0;
        int borderTop = parent.onBottom ? borderLeft : 0;
        int borderBottom = parent.onBottom ? 0 : borderLeft;

        if (borderLeft > 0) {
            x += 1;
            width -= 2;
        }
        int y = parent.onBottom ? size.y - borderBottom - height : borderTop;
        drawBackground(gc, shape, x, y, width, height, defaultBackground, image,
                colors, percents, vertical);
    }

    @Override
    void drawBackground(GC gc, int[] shape, int x, int y, int width, int height,
            Color defaultBackground, Image image, Color[] colors,
            int[] percents, boolean vertical) {
        Region clipping = null, region = null;
        if (shape != null) {
            clipping = new Region();
            gc.getClipping(clipping);
            region = new Region();
            region.add(shape);
            region.intersect(clipping);
            gc.setClipping(region);
        }
        if (image != null) {
            // draw the background image in shape
            gc.setBackground(defaultBackground);
            gc.fillRectangle(x, y, width, height);
            Rectangle imageRect = image.getBounds();
            gc.drawImage(image, imageRect.x, imageRect.y, imageRect.width,
                    imageRect.height, x, y, width, height);
        } else if (colors != null) {
            // draw gradient
            if (colors.length == 1) {
                Color background =
                        colors[0] != null ? colors[0] : defaultBackground;
                gc.setBackground(background);
                gc.fillRectangle(x, y, width, height);
            } else {
                if (vertical) {
                    if (parent.onBottom) {
                        int pos = 0;
                        if (percents[percents.length - 1] < 100) {
                            pos = (100 - percents[percents.length - 1]) *
                                    height / 100;
                            gc.setBackground(defaultBackground);
                            gc.fillRectangle(x, y, width, pos);
                        }
                        Color lastColor = colors[colors.length - 1];
                        if (lastColor == null) {
                            lastColor = defaultBackground;
                        }
                        for (int i = percents.length - 1; i >= 0; i--) {
                            gc.setForeground(lastColor);
                            lastColor = colors[i];
                            if (lastColor == null) {
                                lastColor = defaultBackground;
                            }
                            gc.setBackground(lastColor);
                            int percentage =
                                    i > 0 ? percents[i] - percents[i - 1] :
                                            percents[i];
                            int gradientHeight = percentage * height / 100;
                            gc.fillGradientRectangle(x, y + pos, width,
                                    gradientHeight, true);
                            pos += gradientHeight;
                        }
                    } else {
                        Color lastColor = colors[0];
                        if (lastColor == null) {
                            lastColor = defaultBackground;
                        }
                        int pos = 0;
                        for (int i = 0; i < percents.length; i++) {
                            gc.setForeground(lastColor);
                            lastColor = colors[i + 1];
                            if (lastColor == null) {
                                lastColor = defaultBackground;
                            }
                            gc.setBackground(lastColor);
                            int percentage =
                                    i > 0 ? percents[i] - percents[i - 1] :
                                            percents[i];
                            int gradientHeight = percentage * height / 100;
                            gc.fillGradientRectangle(x, y + pos, width,
                                    gradientHeight, true);
                            pos += gradientHeight;
                        }
                        if (pos < height) {
                            gc.setBackground(defaultBackground);
                            gc.fillRectangle(x, pos, width, height - pos + 1);
                        }
                    }
                } else { //horizontal gradient
                    y = 0;
                    height = parent.getSize().y;
                    Color lastColor = colors[0];
                    if (lastColor == null) {
                        lastColor = defaultBackground;
                    }
                    int pos = 0;
                    for (int i = 0; i < percents.length; ++i) {
                        gc.setForeground(lastColor);
                        lastColor = colors[i + 1];
                        if (lastColor == null) {
                            lastColor = defaultBackground;
                        }
                        gc.setBackground(lastColor);
                        int gradientWidth = percents[i] * width / 100 - pos;
                        gc.fillGradientRectangle(x + pos, y, gradientWidth,
                                height, false);
                        pos += gradientWidth;
                    }
                    if (pos < width) {
                        gc.setBackground(defaultBackground);
                        gc.fillRectangle(x + pos, y, width - pos, height);
                    }
                }
            }
        } else {
            // draw a solid background using default background in shape
            if ((parent.getStyle() & SWT.NO_BACKGROUND) != 0 ||
                    !defaultBackground.equals(parent.getBackground())) {
                gc.setBackground(defaultBackground);
                gc.fillRectangle(x, y, width, height);
            }
        }
        if (shape != null) {
            gc.setClipping(clipping);
            clipping.dispose();
            region.dispose();
        }
    }

    @Override
    void drawBorder(GC gc, int[] shape) {

        gc.setForeground(parent.getDisplay().getSystemColor(BORDER1_COLOR));
        gc.drawPolyline(shape);
    }

    @Override
    void drawBody(GC gc, Rectangle bounds, int state) {
        Point size = new Point(bounds.width, bounds.height);
        int selectedIndex = parent.selectedIndex;
        int tabHeight = parent.tabHeight;

        int border = parent.borderVisible ? 1 : 0;
        int borderTop = parent.onBottom ? border : 0;
        int borderBottom = parent.onBottom ? 0 : border;

        int style = parent.getStyle();
        int highlightHeader = (style & SWT.FLAT) != 0 ? 1 : 3;
        int highlightMargin = (style & SWT.FLAT) != 0 ? 0 : 2;

        // fill in body
        if (!parent.minimized) {
            int width = size.x - border - border - 2 * highlightMargin;
            int height = size.y - borderTop - borderBottom - tabHeight -
                    highlightHeader - highlightMargin;
            // Draw highlight margin
            if (highlightMargin > 0) {
                int[] shape = null;
                if (parent.onBottom) {
                    int x2 = size.x - border;
                    int y2 =
                            size.y - borderBottom - tabHeight - highlightHeader;
                    shape = new int[]{border, borderTop, x2, borderTop, x2, y2,
                            x2 - highlightMargin, y2, x2 - highlightMargin,
                            borderTop + highlightMargin,
                            border + highlightMargin,
                            borderTop + highlightMargin,
                            border + highlightMargin, y2, border, y2};
                } else {
                    int y1 = borderTop + tabHeight + highlightHeader;
                    int x2 = size.x - border;
                    int y2 = size.y - borderBottom;
                    shape = new int[]{border, y1, border + highlightMargin, y1,
                            border + highlightMargin, y2 - highlightMargin,
                            x2 - highlightMargin, y2 - highlightMargin,
                            x2 - highlightMargin, y1, x2, y1, x2, y2, border,
                            y2};
                }
                // If horizontal gradient, show gradient across the whole area
                if (selectedIndex != -1 &&
                        parent.selectionGradientColors != null &&
                        parent.selectionGradientColors.length > 1 &&
                        !parent.selectionGradientVertical) {
                    drawBackground(gc, shape, true);
                } else if (selectedIndex == -1 &&
                        parent.gradientColors != null &&
                        parent.gradientColors.length > 1 &&
                        !parent.gradientVertical) {
                    drawBackground(gc, shape, false);
                } else {
                    gc.setBackground(
                            selectedIndex == -1 ? parent.getBackground() :
                                    parent.selectionBackground);
                    gc.fillPolygon(shape);
                }
            }
            //Draw client area
            if ((parent.getStyle() & SWT.NO_BACKGROUND) != 0) {
                gc.setBackground(parent.getBackground());
                int marginWidth = parent.marginWidth;
                int marginHeight = parent.marginHeight;
                int xClient = border + marginWidth + highlightMargin, yClient;
                if (parent.onBottom) {
                    yClient = borderTop + highlightMargin + marginHeight;
                } else {
                    yClient = borderTop + tabHeight + highlightHeader +
                            marginHeight;
                }
                gc.fillRectangle(xClient - marginWidth, yClient - marginHeight,
                        width, height);
            }
        } else {
            if ((parent.getStyle() & SWT.NO_BACKGROUND) != 0) {
                int height =
                        borderTop + tabHeight + highlightHeader + borderBottom;
                if (size.y > height) {
                    gc.setBackground(parent.getParent().getBackground());
                    gc.fillRectangle(0, height, size.x, size.y - height);
                }
            }
        }

        //draw 1 pixel border around outside
        if (border > 0) {
            gc.setForeground(parent.getDisplay().getSystemColor(BORDER1_COLOR));
            int x1 = border - 1;
            int x2 = size.x - border;
            int y1 = parent.onBottom ? borderTop - 1 : borderTop + tabHeight;
            int y2 = parent.onBottom ? size.y - tabHeight - borderBottom - 1 :
                    size.y - borderBottom;
            gc.drawLine(x1, y1, x1, y2); // left
            gc.drawLine(x2, y1, x2, y2); // right
            if (parent.onBottom) {
                gc.drawLine(x1, y1, x2, y1); // top
            } else {
                gc.drawLine(x1, y2, x2, y2); // bottom
            }
        }
    }

    @Override
    void drawClose(GC gc, Rectangle closeRect, int closeImageState) {
        if (closeRect.width == 0 || closeRect.height == 0) {
            return;
        }
        Display display = parent.getDisplay();

        // draw X 7x7
        int x = closeRect.x + Math.max(1, (closeRect.width - 7) / 2) + 4;
        int y = closeRect.y + Math.max(1, (closeRect.height - 7) / 2) + 4;
        y += parent.onBottom ? -1 : 1;
        int size = 4;

        Color closeBorder = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        switch (closeImageState & (SWT.HOT | SWT.SELECTED | SWT.BACKGROUND)) {
            case SWT.NONE:
                gc.setForeground(closeBorder);
                gc.drawLine(x - size, y - size, x + size, y + size);
                gc.drawLine(x - size, y + size, x + size, y - size);
                break;
            case SWT.HOT:
                gc.setForeground(getFillColor());
                gc.drawLine(x - size, y - size, x + size, y + size);
                gc.drawLine(x - size, y + size, x + size, y - size);
                break;
            case SWT.SELECTED:
                gc.setForeground(closeBorder);
                gc.drawLine(x - size, y - size, x + size, y + size);
                gc.drawLine(x - size, y + size, x + size, y - size);
                break;
            case SWT.BACKGROUND:
                gc.setForeground(closeBorder);
                gc.drawLine(x - size, y - size, x + size, y + size);
                gc.drawLine(x - size, y + size, x + size, y - size);
                break;
        }
    }

    @Override
    void drawChevron(GC gc, Rectangle chevronRect, int chevronImageState) {
        if (chevronRect.width == 0 || chevronRect.height == 0) {
            return;
        }
        int selectedIndex = parent.selectedIndex;
        // draw chevron (10x7)
        Display display = parent.getDisplay();
        Point dpi = display.getDPI();
        int fontHeight = 72 * 10 / dpi.y;
        FontData fd = parent.getFont().getFontData()[0];
        fd.setHeight(fontHeight);
        Font f = new Font(display, fd);
        int fHeight = f.getFontData()[0].getHeight() * dpi.y / 72;
        int indent = Math.max(2, (chevronRect.height - fHeight - 4) / 2);
        int x = chevronRect.x + 2;
        int y = chevronRect.y + indent;
        int count;
        int itemCount = parent.getItemCount();
        if (parent.single) {
            count = selectedIndex == -1 ? itemCount : itemCount - 1;
        } else {
            int showCount = 0;
            while (showCount < parent.priority.length &&
                    parent.items[parent.priority[showCount]].showing) {
                showCount++;
            }
            count = itemCount - showCount;
        }
        String chevronString =
                count > 99 ? "99+" : String.valueOf(count); //$NON-NLS-1$
        switch (chevronImageState & (SWT.HOT | SWT.SELECTED)) {
            case SWT.NONE:
                Color chevronBorder =
                        parent.single ? parent.getSelectionForeground() :
                                parent.getForeground();
                gc.setForeground(chevronBorder);
                gc.setFont(f);
                gc.drawLine(x, y, x + 2, y + 2);
                gc.drawLine(x + 2, y + 2, x, y + 4);
                gc.drawLine(x + 1, y, x + 3, y + 2);
                gc.drawLine(x + 3, y + 2, x + 1, y + 4);
                gc.drawLine(x + 4, y, x + 6, y + 2);
                gc.drawLine(x + 6, y + 2, x + 5, y + 4);
                gc.drawLine(x + 5, y, x + 7, y + 2);
                gc.drawLine(x + 7, y + 2, x + 4, y + 4);
                gc.drawString(chevronString, x + 7, y + 3, true);
                break;
            case SWT.HOT:
                gc.setForeground(display.getSystemColor(BUTTON_BORDER));
                gc.setBackground(display.getSystemColor(BUTTON_FILL));
                gc.setFont(f);
                gc.fillRoundRectangle(chevronRect.x, chevronRect.y,
                        chevronRect.width, chevronRect.height, 6, 6);
                gc.drawRoundRectangle(chevronRect.x, chevronRect.y,
                        chevronRect.width - 1, chevronRect.height - 1, 6, 6);
                gc.drawLine(x, y, x + 2, y + 2);
                gc.drawLine(x + 2, y + 2, x, y + 4);
                gc.drawLine(x + 1, y, x + 3, y + 2);
                gc.drawLine(x + 3, y + 2, x + 1, y + 4);
                gc.drawLine(x + 4, y, x + 6, y + 2);
                gc.drawLine(x + 6, y + 2, x + 5, y + 4);
                gc.drawLine(x + 5, y, x + 7, y + 2);
                gc.drawLine(x + 7, y + 2, x + 4, y + 4);
                gc.drawString(chevronString, x + 7, y + 3, true);
                break;
            case SWT.SELECTED:
                gc.setForeground(display.getSystemColor(BUTTON_BORDER));
                gc.setBackground(display.getSystemColor(BUTTON_FILL));
                gc.setFont(f);
                gc.fillRoundRectangle(chevronRect.x, chevronRect.y,
                        chevronRect.width, chevronRect.height, 6, 6);
                gc.drawRoundRectangle(chevronRect.x, chevronRect.y,
                        chevronRect.width - 1, chevronRect.height - 1, 6, 6);
                gc.drawLine(x + 1, y + 1, x + 3, y + 3);
                gc.drawLine(x + 3, y + 3, x + 1, y + 5);
                gc.drawLine(x + 2, y + 1, x + 4, y + 3);
                gc.drawLine(x + 4, y + 3, x + 2, y + 5);
                gc.drawLine(x + 5, y + 1, x + 7, y + 3);
                gc.drawLine(x + 7, y + 3, x + 6, y + 5);
                gc.drawLine(x + 6, y + 1, x + 8, y + 3);
                gc.drawLine(x + 8, y + 3, x + 5, y + 5);
                gc.drawString(chevronString, x + 8, y + 4, true);
                break;
        }
        f.dispose();
    }

    /*
     * Draw a highlight effect along the left, top, and right edges of the tab.
     * Only for curved tabs, on top.
     * Do not draw if insufficient colors.
     */
    @Override
    void drawHighlight(GC gc, Rectangle bounds, int state, int rightEdge) {
        //only draw for curvy tabs and only draw for top tabs
        if (parent.simple || parent.onBottom) {
            return;
        }

        if (selectionHighlightGradientBegin == null) {
            return;
        }

        Color[] gradients = selectionHighlightGradientColorsCache;
        if (gradients == null) {
            return;
        }
        int gradientsSize = gradients.length;
        if (gradientsSize == 0) {
            return;        //shouldn't happen but just to be tidy
        }

        int x = bounds.x;
        int y = bounds.y;

        gc.setForeground(gradients[0]);

        //draw top horizontal line
        gc.drawLine(TOP_LEFT_CORNER_HILITE[0] + x + 1,
                //rely on fact that first pair is top/right of curve
                1 + y, rightEdge - curveIndent, 1 + y);

        int[] leftHighlightCurve = TOP_LEFT_CORNER_HILITE;

        int d = parent.tabHeight - topCurveHighlightEnd.length / 2;

        int lastX = 0;
        int lastY = 0;
        int lastColorIndex = 0;

        //draw upper left curve highlight
        for (int i = 0; i < leftHighlightCurve.length / 2; i++) {
            int rawX = leftHighlightCurve[i << 1];
            int rawY = leftHighlightCurve[(i << 1) + 1];
            lastX = rawX + x;
            lastY = rawY + y;
            lastColorIndex = rawY - 1;
            gc.setForeground(gradients[lastColorIndex]);
            gc.drawPoint(lastX, lastY);
        }
        //draw left vertical line highlight
        for (int i = lastColorIndex; i < gradientsSize; i++) {
            gc.setForeground(gradients[i]);
            gc.drawPoint(lastX, 1 + lastY++);
        }

        int rightEdgeOffset = rightEdge - curveIndent;

        //draw right swoop highlight up to diagonal portion
        for (int i = 0; i < topCurveHighlightStart.length / 2; i++) {
            int rawX = topCurveHighlightStart[i << 1];
            int rawY = topCurveHighlightStart[(i << 1) + 1];
            lastX = rawX + rightEdgeOffset;
            lastY = rawY + y;
            lastColorIndex = rawY - 1;
            if (lastColorIndex >= gradientsSize) {
                break;    //can happen if tabs are unusually short and cut off the curve
            }
            gc.setForeground(gradients[lastColorIndex]);
            gc.drawPoint(lastX, lastY);
        }
        //draw right diagonal line highlight
        for (int i = lastColorIndex; i < lastColorIndex + d; i++) {
            if (i >= gradientsSize) {
                break;    //can happen if tabs are unusually short and cut off the curve
            }
            gc.setForeground(gradients[i]);
            gc.drawPoint(1 + lastX++, 1 + lastY++);
        }

        //draw right swoop highlight from diagonal portion to end
        for (int i = 0; i < topCurveHighlightEnd.length / 2; i++) {
            int rawX = topCurveHighlightEnd[i <<
                    1]; //d is already encoded in this value
            int rawY = topCurveHighlightEnd[(i << 1) + 1]; //d already encoded
            lastX = rawX + rightEdgeOffset;
            lastY = rawY + y;
            lastColorIndex = rawY - 1;
            if (lastColorIndex >= gradientsSize) {
                break;    //can happen if tabs are unusually short and cut off the curve
            }
            gc.setForeground(gradients[lastColorIndex]);
            gc.drawPoint(lastX, lastY);
        }
    }

    /*
     * Draw the unselected border for the receiver on the left.
     *
     * @param gc
     */
    @Override
    void drawLeftUnselectedBorder(GC gc, Rectangle bounds, int state) {
        int x = bounds.x;
        int y = bounds.y;
        int height = bounds.height;

        int[] shape = null;
        if (parent.onBottom) {
            int[] left = parent.simple ? SIMPLE_UNSELECTED_INNER_CORNER :
                    BOTTOM_LEFT_CORNER;

            shape = new int[left.length + 2];
            int index = 0;
            shape[index++] = x;
            shape[index++] = y - 1;
            for (int i = 0; i < left.length / 2; i++) {
                shape[index++] = x + left[2 * i];
                shape[index++] = y + height + left[2 * i + 1] - 1;
            }
        } else {
            int[] left = parent.simple ? SIMPLE_UNSELECTED_INNER_CORNER :
                    TOP_LEFT_CORNER;

            shape = new int[left.length + 2];
            int index = 0;
            shape[index++] = x;
            shape[index++] = y + height;
            for (int i = 0; i < left.length / 2; i++) {
                shape[index++] = x + left[2 * i];
                shape[index++] = y + left[2 * i + 1];
            }
        }

        drawBorder(gc, shape);
    }

    @Override
    void drawMaximize(GC gc, Rectangle maxRect, int maxImageState) {
        if (maxRect.width == 0 || maxRect.height == 0) {
            return;
        }
        Display display = parent.getDisplay();
        // 5x4 or 7x9
        int x = maxRect.x + (maxRect.width - 10) / 2;
        int y = maxRect.y + 3;

        gc.setForeground(display.getSystemColor(BUTTON_BORDER));
        gc.setBackground(display.getSystemColor(BUTTON_FILL));

        switch (maxImageState & (SWT.HOT | SWT.SELECTED)) {
            case SWT.NONE:
                if (!parent.getMaximized()) {
                    gc.fillRectangle(x, y, 9, 9);
                    gc.drawRectangle(x, y, 9, 9);
                    gc.drawLine(x + 1, y + 2, x + 8, y + 2);
                } else {
                    gc.fillRectangle(x, y + 3, 5, 4);
                    gc.fillRectangle(x + 2, y, 5, 4);
                    gc.drawRectangle(x, y + 3, 5, 4);
                    gc.drawRectangle(x + 2, y, 5, 4);
                    gc.drawLine(x + 3, y + 1, x + 6, y + 1);
                    gc.drawLine(x + 1, y + 4, x + 4, y + 4);
                }
                break;
            case SWT.HOT:
                gc.fillRoundRectangle(maxRect.x, maxRect.y, maxRect.width,
                        maxRect.height, 6, 6);
                gc.drawRoundRectangle(maxRect.x, maxRect.y, maxRect.width - 1,
                        maxRect.height - 1, 6, 6);
                if (!parent.getMaximized()) {
                    gc.fillRectangle(x, y, 9, 9);
                    gc.drawRectangle(x, y, 9, 9);
                    gc.drawLine(x + 1, y + 2, x + 8, y + 2);
                } else {
                    gc.fillRectangle(x, y + 3, 5, 4);
                    gc.fillRectangle(x + 2, y, 5, 4);
                    gc.drawRectangle(x, y + 3, 5, 4);
                    gc.drawRectangle(x + 2, y, 5, 4);
                    gc.drawLine(x + 3, y + 1, x + 6, y + 1);
                    gc.drawLine(x + 1, y + 4, x + 4, y + 4);
                }
                break;
            case SWT.SELECTED:
                gc.fillRoundRectangle(maxRect.x, maxRect.y, maxRect.width,
                        maxRect.height, 6, 6);
                gc.drawRoundRectangle(maxRect.x, maxRect.y, maxRect.width - 1,
                        maxRect.height - 1, 6, 6);
                if (!parent.getMaximized()) {
                    gc.fillRectangle(x + 1, y + 1, 9, 9);
                    gc.drawRectangle(x + 1, y + 1, 9, 9);
                    gc.drawLine(x + 2, y + 3, x + 9, y + 3);
                } else {
                    gc.fillRectangle(x + 1, y + 4, 5, 4);
                    gc.fillRectangle(x + 3, y + 1, 5, 4);
                    gc.drawRectangle(x + 1, y + 4, 5, 4);
                    gc.drawRectangle(x + 3, y + 1, 5, 4);
                    gc.drawLine(x + 4, y + 2, x + 7, y + 2);
                    gc.drawLine(x + 2, y + 5, x + 5, y + 5);
                }
                break;
        }
    }

    @Override
    void drawMinimize(GC gc, Rectangle minRect, int minImageState) {
        if (minRect.width == 0 || minRect.height == 0) {
            return;
        }
        Display display = parent.getDisplay();
        // 5x4 or 9x3
        int x = minRect.x + (minRect.width - 10) / 2;
        int y = minRect.y + 3;

        gc.setForeground(display.getSystemColor(BUTTON_BORDER));
        gc.setBackground(display.getSystemColor(BUTTON_FILL));

        switch (minImageState & (SWT.HOT | SWT.SELECTED)) {
            case SWT.NONE:
                if (!parent.getMinimized()) {
                    gc.fillRectangle(x, y, 9, 3);
                    gc.drawRectangle(x, y, 9, 3);
                } else {
                    gc.fillRectangle(x, y + 3, 5, 4);
                    gc.fillRectangle(x + 2, y, 5, 4);
                    gc.drawRectangle(x, y + 3, 5, 4);
                    gc.drawRectangle(x + 2, y, 5, 4);
                    gc.drawLine(x + 3, y + 1, x + 6, y + 1);
                    gc.drawLine(x + 1, y + 4, x + 4, y + 4);
                }
                break;
            case SWT.HOT:
                gc.fillRoundRectangle(minRect.x, minRect.y, minRect.width,
                        minRect.height, 6, 6);
                gc.drawRoundRectangle(minRect.x, minRect.y, minRect.width - 1,
                        minRect.height - 1, 6, 6);
                if (!parent.getMinimized()) {
                    gc.fillRectangle(x, y, 9, 3);
                    gc.drawRectangle(x, y, 9, 3);
                } else {
                    gc.fillRectangle(x, y + 3, 5, 4);
                    gc.fillRectangle(x + 2, y, 5, 4);
                    gc.drawRectangle(x, y + 3, 5, 4);
                    gc.drawRectangle(x + 2, y, 5, 4);
                    gc.drawLine(x + 3, y + 1, x + 6, y + 1);
                    gc.drawLine(x + 1, y + 4, x + 4, y + 4);
                }
                break;
            case SWT.SELECTED:
                gc.fillRoundRectangle(minRect.x, minRect.y, minRect.width,
                        minRect.height, 6, 6);
                gc.drawRoundRectangle(minRect.x, minRect.y, minRect.width - 1,
                        minRect.height - 1, 6, 6);
                if (!parent.getMinimized()) {
                    gc.fillRectangle(x + 1, y + 1, 9, 3);
                    gc.drawRectangle(x + 1, y + 1, 9, 3);
                } else {
                    gc.fillRectangle(x + 1, y + 4, 5, 4);
                    gc.fillRectangle(x + 3, y + 1, 5, 4);
                    gc.drawRectangle(x + 1, y + 4, 5, 4);
                    gc.drawRectangle(x + 3, y + 1, 5, 4);
                    gc.drawLine(x + 4, y + 2, x + 7, y + 2);
                    gc.drawLine(x + 2, y + 5, x + 5, y + 5);
                }
                break;
        }
    }

    /*
     * Draw the unselected border for the receiver on the right.
     *
     * @param gc
     */
    @Override
    void drawRightUnselectedBorder(GC gc, Rectangle bounds, int state) {
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        int[] shape = null;
        int startX = x + width - 1;

        if (parent.onBottom) {
            int[] right = parent.simple ? SIMPLE_UNSELECTED_INNER_CORNER :
                    BOTTOM_RIGHT_CORNER;

            shape = new int[right.length + 2];
            int index = 0;

            for (int i = 0; i < right.length / 2; i++) {
                shape[index++] = startX + right[2 * i];
                shape[index++] = y + height + right[2 * i + 1] - 1;
            }
            shape[index++] = startX;
            shape[index++] = y - 1;
        } else {
            int[] right = parent.simple ? SIMPLE_UNSELECTED_INNER_CORNER :
                    TOP_RIGHT_CORNER;

            shape = new int[right.length + 2];
            int index = 0;

            for (int i = 0; i < right.length / 2; i++) {
                shape[index++] = startX + right[2 * i];
                shape[index++] = y + right[2 * i + 1];
            }

            shape[index++] = startX;
            shape[index++] = y + height;
        }

        drawBorder(gc, shape);
    }

    @Override
    void drawSelected(int itemIndex, GC gc, Rectangle bounds, int state) {
        CTabItem item = parent.items[itemIndex];
        int x = bounds.x;
        int y = bounds.y;
        int height = bounds.height;
        int width = bounds.width;
        if (!parent.simple && !parent.single) {
            width -= curveWidth - curveIndent;
        }
        int border = parent.borderVisible ? 1 : 0;
        int borderTop = parent.onBottom ? border : 0;
        int borderBottom = parent.onBottom ? 0 : border;

        Point size = parent.getSize();

        int rightEdge = Math.min(x + width, parent.getRightItemEdge(gc));
        //     Draw selection border across all tabs

        if ((state & SWT.BACKGROUND) != 0) {
            int highlightHeader = (parent.getStyle() & SWT.FLAT) != 0 ? 1 : 3;
            int xx = border;
            int yy = parent.onBottom ?
                    size.y - borderBottom - parent.tabHeight - highlightHeader :
                    borderTop + parent.tabHeight + 1;
            int w = size.x - border - border;
            int h = highlightHeader - 1;
            int[] shape = {xx, yy, xx + w, yy, xx + w, yy + h, xx, yy + h};
            if (parent.selectionGradientColors != null &&
                    !parent.selectionGradientVertical) {
                drawBackground(gc, shape, true);
            } else {
                gc.setBackground(parent.selectionBackground);
                gc.fillRectangle(xx, yy, w, h);
            }

            if (parent.single) {
                if (!item.showing) {
                    return;
                }
            } else {
                // if selected tab scrolled out of view or partially out of view
                // just draw bottom line
                if (!item.showing) {
                    int x1 = Math.max(0, border - 1);
                    int y1 = parent.onBottom ? y - 1 : y + height;
                    int x2 = size.x - border;
                    gc.setForeground(
                            parent.getDisplay().getSystemColor(BORDER1_COLOR));
                    gc.drawLine(x1, y1, x2, y1);
                    return;
                }

                // draw selected tab background and outline
                shape = null;
                if (parent.onBottom) {
                    int[] left = parent.simple ? SIMPLE_BOTTOM_LEFT_CORNER :
                            BOTTOM_LEFT_CORNER;
                    int[] right =
                            parent.simple ? SIMPLE_BOTTOM_RIGHT_CORNER : curve;
                    if (border == 0 && itemIndex == parent.firstIndex) {
                        left = new int[]{x, y + height};
                    }
                    shape = new int[left.length + right.length + 8];
                    int index = 0;
                    shape[index++] =
                            x; // first point repeated here because below we reuse shape to draw outline
                    shape[index++] = y - 1;
                    shape[index++] = x;
                    shape[index++] = y - 1;
                    for (int i = 0; i < left.length / 2; i++) {
                        shape[index++] = x + left[2 * i];
                        shape[index++] = y + height + left[2 * i + 1] - 1;
                    }
                    for (int i = 0; i < right.length / 2; i++) {
                        shape[index++] =
                                parent.simple ? rightEdge - 1 + right[2 * i] :
                                        rightEdge - curveIndent + right[2 * i];
                        shape[index++] = parent.simple ?
                                y + height + right[2 * i + 1] - 1 :
                                y + right[2 * i + 1] - 2;
                    }
                    shape[index++] = parent.simple ? rightEdge - 1 :
                            rightEdge + curveWidth - curveIndent;
                    shape[index++] = y - 1;
                    shape[index++] = parent.simple ? rightEdge - 1 :
                            rightEdge + curveWidth - curveIndent;
                    shape[index++] = y - 1;
                } else {
                    int[] left = parent.simple ? SIMPLE_TOP_LEFT_CORNER :
                            TOP_LEFT_CORNER;
                    int[] right =
                            parent.simple ? SIMPLE_TOP_RIGHT_CORNER : curve;
                    if (border == 0 && itemIndex == parent.firstIndex) {
                        left = new int[]{x, y};
                    }
                    shape = new int[left.length + right.length + 8];
                    int index = 0;
                    shape[index++] =
                            x; // first point repeated here because below we reuse shape to draw outline
                    shape[index++] = y + height + 1;
                    shape[index++] = x;
                    shape[index++] = y + height + 1;
                    for (int i = 0; i < left.length / 2; i++) {
                        shape[index++] = x + left[2 * i];
                        shape[index++] = y + left[2 * i + 1];
                    }
                    for (int i = 0; i < right.length / 2; i++) {
                        shape[index++] =
                                parent.simple ? rightEdge - 1 + right[2 * i] :
                                        rightEdge - curveIndent + right[2 * i];
                        shape[index++] = y + right[2 * i + 1];
                    }
                    shape[index++] = parent.simple ? rightEdge - 1 :
                            rightEdge + curveWidth - curveIndent;
                    shape[index++] = y + height + 1;
                    shape[index++] = parent.simple ? rightEdge - 1 :
                            rightEdge + curveWidth - curveIndent;
                    shape[index++] = y + height + 1;
                }

                Rectangle clipping = gc.getClipping();
                Rectangle clipBounds = item.getBounds();
                clipBounds.height += 1;
                if (parent.onBottom) {
                    clipBounds.y -= 1;
                }
                boolean tabInPaint = clipping.intersects(clipBounds);

                if (tabInPaint) {
                    // fill in tab background
                    if (parent.selectionGradientColors != null &&
                            !parent.selectionGradientVertical) {
                        drawBackground(gc, shape, true);
                    } else {
                        Color defaultBackground = parent.selectionBackground;
                        Image image = parent.selectionBgImage;
                        Color[] colors = parent.selectionGradientColors;
                        int[] percents = parent.selectionGradientPercents;
                        boolean vertical = parent.selectionGradientVertical;
                        xx = x;
                        yy = parent.onBottom ? y - 1 : y + 1;
                        w = width;
                        h = height;
                        if (!parent.single && !parent.simple) {
                            w += curveWidth - curveIndent;
                        }
                        drawBackground(gc, shape, xx, yy, w, h,
                                defaultBackground, image, colors, percents,
                                vertical);
                    }
                }

                //Highlight MUST be drawn before the outline so that outline can cover it in the right spots (start of swoop)
                //otherwise the curve looks jagged
                drawHighlight(gc, bounds, state, rightEdge);

                // draw outline
                shape[0] = Math.max(0, border - 1);
                if (border == 0 && itemIndex == parent.firstIndex) {
                    shape[1] = parent.onBottom ? y + height - 1 : y;
                    shape[5] = shape[3] = shape[1];
                }
                shape[shape.length - 2] = size.x - border + 1;
                for (int i = 0; i < shape.length / 2; i++) {
                    if (shape[2 * i + 1] == y + height + 1) {
                        shape[2 * i + 1] -= 1;
                    }
                }
                Color borderColor =
                        parent.getDisplay().getSystemColor(BORDER1_COLOR);
                if (!borderColor.equals(lastBorderColor)) {
                    createAntialiasColors();
                }
                antialias(shape, selectedInnerColor, selectedOuterColor, gc);
                gc.setForeground(borderColor);
                gc.drawPolyline(shape);

                if (!tabInPaint) {
                    return;
                }
            }
        }

        if ((state & SWT.FOREGROUND) != 0) {
            // draw Image
            Rectangle trim = computeTrim(itemIndex, SWT.NONE, 0, 0, 0, 0);
            int xDraw = x - trim.x;
            if (parent.single && (parent.showClose || item.showClose)) {
                xDraw += item.closeRect.width;
            }
            Image image = item.getImage();
            if (image != null && !image.isDisposed()) {
                Rectangle imageBounds = image.getBounds();
                // only draw image if it won't overlap with close button
                int maxImageWidth = rightEdge - xDraw - (trim.width + trim.x);
                if (!parent.single && item.closeRect.width > 0) {
                    maxImageWidth -= item.closeRect.width + INTERNAL_SPACING;
                }
                if (imageBounds.width < maxImageWidth) {
                    int imageX = xDraw;
                    int imageY = y + (height - imageBounds.height) / 2;
                    imageY += parent.onBottom ? -1 : 1;
                    gc.drawImage(image, imageX, imageY);
                    xDraw += imageBounds.width + INTERNAL_SPACING;
                }
            }

            // draw Text
            int textWidth = rightEdge - xDraw - (trim.width + trim.x);
            if (!parent.single && item.closeRect.width > 0) {
                textWidth -= item.closeRect.width + INTERNAL_SPACING;
            }
            if (textWidth > 0) {
                Font gcFont = gc.getFont();
                gc.setFont(item.font == null ? parent.getFont() : item.font);

                if (item.shortenedText == null ||
                        item.shortenedTextWidth != textWidth) {
                    item.shortenedText =
                            shortenText(gc, item.getText(), textWidth);
                    item.shortenedTextWidth = textWidth;
                }
                Point extent = gc.textExtent(item.shortenedText, FLAGS);
                int textY = y + (height - extent.y) / 2;
                textY += parent.onBottom ? -1 : 1;

                gc.setForeground(parent.selectionForeground);
                gc.drawText(item.shortenedText, xDraw, textY, FLAGS);
                gc.setFont(gcFont);

                // draw a Focus rectangle
                if (parent.isFocusControl()) {
                    Display display = parent.getDisplay();
                    if (parent.simple || parent.single) {
                        gc.setBackground(
                                display.getSystemColor(SWT.COLOR_BLACK));
                        gc.setForeground(
                                display.getSystemColor(SWT.COLOR_WHITE));
                        gc.drawFocus(xDraw - 1, textY - 1, extent.x + 2,
                                extent.y + 2);
                    } else {
                        gc.setForeground(display.getSystemColor(BUTTON_BORDER));
                        gc.drawLine(xDraw, textY + extent.y + 1,
                                xDraw + extent.x + 1, textY + extent.y + 1);
                    }
                }
            }
            if (parent.showClose || item.showClose) {
                drawClose(gc, item.closeRect, item.closeImageState);
            }
        }
    }

    @Override
    void drawTabArea(GC gc, Rectangle bounds, int state) {
        Point size = parent.getSize();
        int[] shape = null;
        Color borderColor = parent.getDisplay().getSystemColor(BORDER1_COLOR);
        int tabHeight = parent.tabHeight;
        int style = parent.getStyle();

        int border = parent.borderVisible ? 1 : 0;
        int borderTop = parent.onBottom ? border : 0;
        int borderBottom = parent.onBottom ? 0 : border;

        int selectedIndex = parent.selectedIndex;
        int highlightHeader = (style & SWT.FLAT) != 0 ? 1 : 3;
        if (tabHeight == 0) {
            if ((style & SWT.FLAT) != 0 && (style & SWT.BORDER) == 0) {
                return;
            }
            int x1 = border - 1;
            int x2 = size.x - border;
            int y1 = parent.onBottom ?
                    size.y - borderBottom - highlightHeader - 1 :
                    borderTop + highlightHeader;
            int y2 = parent.onBottom ? size.y - borderBottom : borderTop;
            if (border > 0 && parent.onBottom) {
                y2 -= 1;
            }

            shape = new int[]{x1, y1, x1, y2, x2, y2, x2, y1};

            // If horizontal gradient, show gradient across the whole area
            if (selectedIndex != -1 && parent.selectionGradientColors != null &&
                    parent.selectionGradientColors.length > 1 &&
                    !parent.selectionGradientVertical) {
                drawBackground(gc, shape, true);
            } else if (selectedIndex == -1 && parent.gradientColors != null &&
                    parent.gradientColors.length > 1 &&
                    !parent.gradientVertical) {
                drawBackground(gc, shape, false);
            } else {
                gc.setBackground(selectedIndex == -1 ? parent.getBackground() :
                        parent.selectionBackground);
                gc.fillPolygon(shape);
            }

            //draw 1 pixel border
            if (border > 0) {
                gc.setForeground(borderColor);
                gc.drawPolyline(shape);
            }
            return;
        }

        int x = Math.max(0, border - 1);
        int y = parent.onBottom ? size.y - borderBottom - tabHeight : borderTop;
        int width = size.x - border - border + 1;
        int height = tabHeight - 1;
        boolean simple = parent.simple;
        // Draw Tab Header
        if (parent.onBottom) {
            int[] left, right;
            if ((style & SWT.BORDER) != 0) {
                left = simple ? SIMPLE_BOTTOM_LEFT_CORNER : BOTTOM_LEFT_CORNER;
                right = simple ? SIMPLE_BOTTOM_RIGHT_CORNER :
                        BOTTOM_RIGHT_CORNER;
            } else {
                left = simple ? SIMPLE_BOTTOM_LEFT_CORNER_BORDERLESS :
                        BOTTOM_LEFT_CORNER_BORDERLESS;
                right = simple ? SIMPLE_BOTTOM_RIGHT_CORNER_BORDERLESS :
                        BOTTOM_RIGHT_CORNER_BORDERLESS;
            }
            shape = new int[left.length + right.length + 4];
            int index = 0;
            shape[index++] = x;
            shape[index++] = y - highlightHeader;
            for (int i = 0; i < left.length / 2; i++) {
                shape[index++] = x + left[2 * i];
                shape[index++] = y + height + left[2 * i + 1];
                if (border == 0) {
                    shape[index - 1] += 1;
                }
            }
            for (int i = 0; i < right.length / 2; i++) {
                shape[index++] = x + width + right[2 * i];
                shape[index++] = y + height + right[2 * i + 1];
                if (border == 0) {
                    shape[index - 1] += 1;
                }
            }
            shape[index++] = x + width;
            shape[index++] = y - highlightHeader;
        } else {
            int[] left, right;
            if ((style & SWT.BORDER) != 0) {
                left = simple ? SIMPLE_TOP_LEFT_CORNER : TOP_LEFT_CORNER;
                right = simple ? SIMPLE_TOP_RIGHT_CORNER : TOP_RIGHT_CORNER;
            } else {
                left = simple ? SIMPLE_TOP_LEFT_CORNER_BORDERLESS :
                        TOP_LEFT_CORNER_BORDERLESS;
                right = simple ? SIMPLE_TOP_RIGHT_CORNER_BORDERLESS :
                        TOP_RIGHT_CORNER_BORDERLESS;
            }
            shape = new int[left.length + right.length + 4];
            int index = 0;
            shape[index++] = x;
            shape[index++] = y + height + highlightHeader + 1;
            for (int i = 0; i < left.length / 2; i++) {
                shape[index++] = x + left[2 * i];
                shape[index++] = y + left[2 * i + 1];
            }
            for (int i = 0; i < right.length / 2; i++) {
                shape[index++] = x + width + right[2 * i];
                shape[index++] = y + right[2 * i + 1];
            }
            shape[index++] = x + width;
            shape[index++] = y + height + highlightHeader + 1;
        }
        // Fill in background
        boolean single = parent.single;
        boolean bkSelected = single && selectedIndex != -1;
        drawBackground(gc, shape, bkSelected);
        // Fill in parent background for non-rectangular shape
        Region r = new Region();
        r.add(new Rectangle(x, y, width + 1, height + 1));
        r.subtract(shape);
        gc.setBackground(parent.getParent().getBackground());
        fillRegion(gc, r);
        r.dispose();

        // Draw selected tab
        if (selectedIndex == -1) {
            // if no selected tab - draw line across bottom of all tabs
            int y1 = parent.onBottom ? size.y - borderBottom - tabHeight - 1 :
                    borderTop + tabHeight;
            int x2 = size.x - border;
            gc.setForeground(borderColor);
            gc.drawLine(border, y1, x2, y1);
        }

        // Draw border line
        if (border > 0) {
            if (!borderColor.equals(lastBorderColor)) {
                createAntialiasColors();
            }
            antialias(shape, null, tabAreaColor, gc);
            gc.setForeground(borderColor);
            gc.drawPolyline(shape);
        }
    }

    @Override
    void drawUnselected(int index, GC gc, Rectangle bounds, int state) {
        CTabItem item = parent.items[index];
        int x = bounds.x;
        int y = bounds.y;
        int height = bounds.height;
        int width = bounds.width;

        // Do not draw partial items
        if (!item.showing) {
            return;
        }

        Rectangle clipping = gc.getClipping();
        if (!clipping.intersects(bounds)) {
            return;
        }

        if ((state & SWT.BACKGROUND) != 0) {
            if (index > 0 && index < parent.selectedIndex) {
                drawLeftUnselectedBorder(gc, bounds, state);
            }
            // If it is the last one then draw a line
            if (index > parent.selectedIndex) {
                drawRightUnselectedBorder(gc, bounds, state);
            }
        }

        if ((state & SWT.FOREGROUND) != 0) {
            // draw Image
            Rectangle trim = computeTrim(index, SWT.NONE, 0, 0, 0, 0);
            int xDraw = x - trim.x;
            Image image = item.getImage();
            if (image != null && !image.isDisposed() &&
                    parent.showUnselectedImage) {
                Rectangle imageBounds = image.getBounds();
                // only draw image if it won't overlap with close button
                int maxImageWidth = x + width - xDraw - (trim.width + trim.x);
                if (parent.showUnselectedClose &&
                        (parent.showClose || item.showClose)) {
                    maxImageWidth -= item.closeRect.width + INTERNAL_SPACING;
                }
                if (imageBounds.width < maxImageWidth) {
                    int imageX = xDraw;
                    int imageHeight = imageBounds.height;
                    int imageY = y + (height - imageHeight) / 2;
                    imageY += parent.onBottom ? -1 : 1;
                    int imageWidth = imageBounds.width * imageHeight /
                            imageBounds.height;
                    gc.drawImage(image, imageBounds.x, imageBounds.y,
                            imageBounds.width, imageBounds.height, imageX,
                            imageY, imageWidth, imageHeight);
                    xDraw += imageWidth + INTERNAL_SPACING;
                }
            }
            // draw Text
            int textWidth = x + width - xDraw - (trim.width + trim.x);
            if (parent.showUnselectedClose &&
                    (parent.showClose || item.showClose)) {
                textWidth -= item.closeRect.width + INTERNAL_SPACING;
            }
            if (textWidth > 0) {
                Font gcFont = gc.getFont();
                gc.setFont(item.font == null ? parent.getFont() : item.font);
                if (item.shortenedText == null ||
                        item.shortenedTextWidth != textWidth) {
                    item.shortenedText =
                            shortenText(gc, item.getText(), textWidth);
                    item.shortenedTextWidth = textWidth;
                }
                Point extent = gc.textExtent(item.shortenedText, FLAGS);
                int textY = y + (height - extent.y) / 2;
                textY += parent.onBottom ? -1 : 1;
                gc.setForeground(parent.getForeground());
                gc.drawText(item.shortenedText, xDraw, textY, FLAGS);
                gc.setFont(gcFont);
            }
            // draw close
            if (parent.showUnselectedClose &&
                    (parent.showClose || item.showClose)) {
                drawClose(gc, item.closeRect, item.closeImageState);
            }
        }
    }

    @Override
    void fillRegion(GC gc, Region region) {
        // NOTE: region passed in to this function will be modified
        Region clipping = new Region();
        gc.getClipping(clipping);
        region.intersect(clipping);
        gc.setClipping(region);
        gc.fillRectangle(region.getBounds());
        gc.setClipping(clipping);
        clipping.dispose();
    }
}
