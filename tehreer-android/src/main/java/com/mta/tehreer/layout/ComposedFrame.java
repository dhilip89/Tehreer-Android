/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mta.tehreer.layout;

import android.graphics.Canvas;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.Description;

import java.util.Collections;
import java.util.List;

/**
 * Represents a frame containing multiple lines of text. The frame object is the output resulting
 * from text-framing process performed by a typesetter object.
 */
public class ComposedFrame {

    private final int charStart;
    private final int charEnd;
    private final List<ComposedLine> lineList;

    private float mOriginX;
    private float mOriginY;
    private float mWidth;
    private float mHeight;

    ComposedFrame(int charStart, int charEnd, List<ComposedLine> lineList) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.lineList = Collections.unmodifiableList(lineList);
    }

    void setContainerRect(float originX, float originY, float width, float height) {
        mOriginX = originX;
        mOriginY = originY;
        mWidth = width;
        mHeight = height;
    }

    /**
     * Returns the index to the first character of this frame in source text.
     *
     * @return The index to the first character of this frame in source text.
     */
    public int getCharStart() {
        return charStart;
    }

    /**
     * Returns the index after the last character of this frame in source text.
     *
     * @return The index after the last character of this frame in source text.
     */
    public int getCharEnd() {
        return charEnd;
    }

    /**
     * Returns the x- origin of this frame.
     *
     * @return The x- origin of this frame.
     */
    public float getOriginX() {
        return mOriginX;
    }

    /**
     * Returns the y- origin of this frame.
     *
     * @return The y- origin of this frame.
     */
    public float getOriginY() {
        return mOriginY;
    }

    /**
     * Returns the width of this frame.
     *
     * @return The width of this frame.
     */
    public float getWidth() {
        return mWidth;
    }

    /**
     * Returns the height of this frame.
     *
     * @return The height of this frame.
     */
    public float getHeight() {
        return mHeight;
    }

    /**
     * Returns an unmodifiable list that contains all the lines of this frame.
     *
     * @return An unmodifiable list that contains all the lines of this frame.
     */
    public List<ComposedLine> getLines() {
        return lineList;
    }

    /**
     * Returns the index of line representing the specified position.
     *
     * @param x The x- coordinate of position.
     * @param y The y- coordinate of position.
     * @return The index of line representing the specified position.
     */
    public int getLineIndexFromPosition(float x, float y) {
        int lineCount = lineList.size();
        int lineIndex;

        for (lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            ComposedLine line = lineList.get(lineIndex);
            float top = line.getTop();
            float bottom = top + line.getHeight();
            if (y >= top && y <= bottom) {
                break;
            }
        }

        if (lineIndex == lineCount) {
            lineIndex = lineCount - 1;
        }

        return lineIndex;
    }

    /**
     * Draws this frame onto the given <code>canvas</code> using the given <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this frame.
     * @param canvas The canvas onto which to draw this frame.
     * @param x The x- position at which to draw this frame.
     * @param y The y- position at which to draw this frame.
     */
    public void draw(Renderer renderer, Canvas canvas, float x, float y) {
        for (ComposedLine composedLine : lineList) {
            canvas.translate(x, y);
            composedLine.draw(renderer, canvas, composedLine.getOriginX(), composedLine.getOriginY());
            canvas.translate(-x, -y);
        }
    }

    @Override
    public String toString() {
        return "ComposedFrame{charStart=" + charStart
                + ", charEnd=" + charEnd
                + ", originX=" + getOriginX()
                + ", originY=" + getOriginY()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + ", lines=" + Description.forIterable(lineList)
                + "}";
    }
}
