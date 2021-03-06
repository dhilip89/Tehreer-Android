/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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
import android.graphics.RectF;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.Exceptions;
import com.mta.tehreer.internal.collections.SafeFloatList;
import com.mta.tehreer.internal.collections.SafeIntList;
import com.mta.tehreer.internal.collections.SafePointList;
import com.mta.tehreer.sfnt.WritingDirection;

/**
 * A <code>GlyphRun</code> object is a collection of consecutive glyphs sharing the same attributes
 * and direction.
 */
public class GlyphRun {

    private IntrinsicRun mIntrinsicRun;
	private int mCharStart;
	private int mCharEnd;
    private int mGlyphOffset;
    private int mGlyphCount;
    private float mOriginX;
    private float mOriginY;
    private float mWidth = Float.NEGATIVE_INFINITY;

	GlyphRun(IntrinsicRun intrinsicRun, int charStart, int charEnd) {
        mIntrinsicRun = intrinsicRun;
        mCharStart = charStart;
        mCharEnd = charEnd;
        mGlyphOffset = intrinsicRun.charGlyphStart(charStart);
        mGlyphCount = intrinsicRun.charGlyphEnd(charEnd - 1) - mGlyphOffset;
	}

    GlyphRun(GlyphRun otherRun) {
        mIntrinsicRun = otherRun.mIntrinsicRun;
        mGlyphOffset = otherRun.mGlyphOffset;
        mGlyphCount = otherRun.mGlyphCount;
    }

    private String checkRange(int glyphStart, int glyphEnd) {
        if (glyphStart < 0) {
            return ("Glyph Start: " + glyphStart);
        }
        if (glyphEnd > mGlyphCount) {
            return ("Glyph End: " + glyphEnd + ", Glyph Count: " + mGlyphCount);
        }
        if (glyphStart > glyphEnd) {
            return ("Glyph Start: " + glyphStart + ", Glyph End: " + glyphEnd);
        }

        return null;
    }

    IntrinsicRun getGlyphRun() {
        return mIntrinsicRun;
    }

    /**
     * Returns the index to the first character of this run in source text.
     *
     * @return The index to the first character of this run in source text.
     */
    public int getCharStart() {
        return mCharStart;
    }

    /**
     * Returns the index after the last character of this run in source text.
     *
     * @return The index after the last character of this run in source text.
     */
    public int getCharEnd() {
        return mCharEnd;
    }

    /**
     * Returns the bidirectional level of this run.
     *
     * @return The bidirectional level of this run.
     */
    public byte getBidiLevel() {
        return mIntrinsicRun.bidiLevel;
    }

    /**
     * Returns the typeface of this run.
     *
     * @return The typeface of this run.
     */
    public Typeface getTypeface() {
        return mIntrinsicRun.typeface;
    }

    /**
     * Returns the type size of this run.
     *
     * @return The type size of this run.
     */
    public float getTypeSize() {
        return mIntrinsicRun.typeSize;
    }

    /**
     * Returns the writing direction of this run.
     *
     * @return The writing direction of this run.
     */
    public WritingDirection getWritingDirection() {
        return mIntrinsicRun.writingDirection();
    }

    /**
     * Returns the number of glyphs in this run.
     *
     * @return The number of glyphs in this run.
     */
    public int getGlyphCount() {
        return mGlyphCount;
    }

    /**
     * Returns a list of glyph IDs in this run.
     *
     * @return A list of glyph IDs in this run.
     */
    public IntList getGlyphIds() {
        return new SafeIntList(mIntrinsicRun.glyphIds, mGlyphOffset, mGlyphCount);
    }

    /**
     * Returns a list of glyph offsets in this run.
     *
     * @return A list of glyph offsets in this run.
     */
    public PointList getGlyphOffsets() {
        return new SafePointList(mIntrinsicRun.glyphOffsets, mGlyphOffset, mGlyphCount);
    }

    /**
     * Returns a list of glyph advances in this run.
     *
     * @return A list of glyph advances in this run.
     */
    public FloatList getGlyphAdvances() {
        return new SafeFloatList(mIntrinsicRun.glyphAdvances, mGlyphOffset, mGlyphCount);
    }

    /**
     * Returns a list of indexes, mapping each character in this run to corresponding glyph.
     *
     * @return A list of indexes, mapping each character in this run to corresponding glyph.
     */
    public IntList getClusterMap() {
        return new IndexList(mIntrinsicRun.clusterMap,
                             mCharStart - mIntrinsicRun.charStart,
                             mCharEnd - mCharStart,
                             mGlyphOffset);
    }

    private static class IndexList extends IntList {

        final int[] array;
        final int offset;
        final int size;
        final int difference;

        IndexList(int[] array, int offset, int size, int difference) {
            this.array = array;
            this.offset = offset;
            this.size = size;
            this.difference = difference;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int get(int index) {
            if (index < 0 || index >= size) {
                throw Exceptions.indexOutOfBounds(index, size);
            }

            return array[index + offset] - difference;
        }

        @Override
        public void copyTo(int[] array, int atIndex) {
            System.arraycopy(this.array, offset, array, atIndex, size);

            if (difference != 0) {
                int length = size();
                for (int i = atIndex; i < length; i++) {
                    array[i] -= difference;
                }
            }
        }

        @Override
        public IntList subList(int fromIndex, int toIndex) {
            if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
                throw new IndexOutOfBoundsException();
            }

            return new IndexList(array, offset + fromIndex, toIndex - fromIndex, difference);
        }
    }

    int getCharGlyphStart(int charIndex) {
        return (mIntrinsicRun.charGlyphStart(charIndex) - mGlyphOffset);
    }

    int getCharGlyphEnd(int charIndex) {
        return (mIntrinsicRun.charGlyphEnd(charIndex) - mGlyphOffset);
    }

    /**
     * Returns the x- origin of this run in parent line.
     *
     * @return The x- origin of this run in parent line.
     */
    public float getOriginX() {
        return mOriginX;
    }

    void setOriginX(float originX) {
        mOriginX = originX;
    }

    /**
     * Returns the y- origin of this run in parent line.
     *
     * @return The y- origin of this run in parent line.
     */
    public float getOriginY() {
        return mOriginY;
    }

    void setOriginY(float originY) {
        mOriginY = originY;
    }

    /**
     * Returns the ascent of this run. The ascent is the distance from the top of the
     * <code>GlyphRun</code> to the baseline. It is always either positive or zero.
     *
     * @return The ascent of this run.
     */
    public float getAscent() {
        return mIntrinsicRun.ascent();
    }

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the <code>GlyphRun</code>. It is always either positive or zero.
     *
     * @return The descent of this run.
     */
    public float getDescent() {
        return mIntrinsicRun.descent();
    }

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     *
     * @return The leading of this run.
     */
    public float getLeading() {
        return mIntrinsicRun.leading();
    }

    /**
     * Returns the typographic width of this run.
     *
     * @return The typographic width of this run.
     */
    public float getWidth() {
        // Locking is not required for constant width.
        if (mWidth == Float.NEGATIVE_INFINITY) {
            mWidth = computeTypographicExtent(0, mGlyphCount);
        }

        return mWidth;
    }

    /**
     * Returns the typographic height of this run.
     *
     * @return The typographic height of this run.
     */
    public float getHeight() {
        return (mIntrinsicRun.ascent() + mIntrinsicRun.descent() + mIntrinsicRun.leading());
    }

    /**
     * Calculates the typographic extent for the given glyph range in this run. The typographic
     * extent is equal to the sum of advances of glyphs.
     *
     * @param glyphStart The index to first glyph being measured.
     * @param glyphEnd The index after the last glyph being measured.
     * @return The typographic extent for the given glyph range in this run.
     */
    public float computeTypographicExtent(int glyphStart, int glyphEnd) {
        String rangeError = checkRange(glyphStart, glyphEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

        glyphStart += mGlyphOffset;
        glyphEnd += mGlyphOffset;

        return mIntrinsicRun.measureGlyphs(glyphStart, glyphEnd);
    }

    /**
     * Calculates the bounding box for the given glyph range in this run. The bounding box is a
     * rectangle that encloses the paths of this run's glyphs in the given range, as tightly as
     * possible.
     *
     * @param renderer The renderer to use for calculating the bounding box. This is required
     *                 because the renderer could have settings in it that would cause changes in
     *                 the bounding box.
     * @param glyphStart The index to the first glyph being measured.
     * @param glyphEnd The index after the last glyph being measured.
     * @return A rectangle that tightly encloses the paths of this run's glyphs in the given range.
     *
     * @throws IllegalArgumentException if <code>glyphStart</code> is negative, or
     *         <code>glyphEnd</code> is greater than total number of glyphs in the run, or
     *         <code>glyphStart</code> is greater than <code>glyphEnd</code>.
     */
	public RectF computeBoundingBox(Renderer renderer, int glyphStart, int glyphEnd) {
        String rangeError = checkRange(glyphStart, glyphEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

	    renderer.setTypeface(mIntrinsicRun.typeface);
	    renderer.setTypeSize(mIntrinsicRun.typeSize);
        renderer.setWritingDirection(mIntrinsicRun.writingDirection());

        return renderer.computeBoundingBox(getGlyphIds().subList(glyphStart, glyphEnd),
                                           getGlyphOffsets().subList(glyphStart, glyphEnd),
                                           getGlyphAdvances().subList(glyphStart, glyphEnd));
	}

    /**
     * Draws this run completely onto the given <code>canvas</code> using the given
     * <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     */
	public void draw(Renderer renderer, Canvas canvas) {
	    draw(renderer, canvas, 0, mGlyphCount);
	}

    /**
     * Draws a part of this run onto the given <code>canvas</code> using the given
     * <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     * @param glyphStart The index to the first glyph being drawn.
     * @param glyphEnd The index after the last glyph being drawn.
     *
     * @throws IllegalArgumentException if <code>glyphStart</code> is negative, or
     *         <code>glyphEnd</code> is greater than total number of glyphs in the run, or
     *         <code>glyphStart</code> is greater than <code>glyphEnd</code>.
     */
	public void draw(Renderer renderer, Canvas canvas, int glyphStart, int glyphEnd) {
        String rangeError = checkRange(glyphStart, glyphEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

	    renderer.setTypeface(mIntrinsicRun.typeface);
        renderer.setTypeSize(mIntrinsicRun.typeSize);
        renderer.setWritingDirection(mIntrinsicRun.writingDirection());

	    renderer.drawGlyphs(canvas,
                            getGlyphIds().subList(glyphStart, glyphEnd),
                            getGlyphOffsets().subList(glyphStart, glyphEnd),
                            getGlyphAdvances().subList(glyphStart, glyphEnd));
	}

    @Override
    public String toString() {
        return "GlyphRun{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", bidiLevel=" + getBidiLevel()
                + ", writingDirection=" + getWritingDirection().toString()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphIds=" + getGlyphIds().toString()
                + ", glyphOffsets=" + getGlyphOffsets().toString()
                + ", glyphAdvances=" + getGlyphAdvances().toString()
                + ", clusterMap=" + getClusterMap().toString()
                + ", originX=" + getOriginX()
                + ", originY=" + getOriginY()
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", leading=" + getLeading()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + "}";
    }
}
