/*
 * Copyright 2014 Frakbot (Sebastiano Poggi and Francesco Pontillo)
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

package com.z.startwar.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.z.startwar.R;

public class StarWarView extends TextView {

    private static final float FLOAT_EPSILON = 0.001f;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private int mTextColor = 0xffffc92a;   // 黄色

    private float mAngle = 60f;
    private float mScrollPosition = 0f;
    private float mEndScrollMult = 2f;
    private float mDistanceFromText = 0f;

    private final Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();
    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;

    public StarWarView(Context context) {
        super(context);
        init(null, 0, context);
    }

    public StarWarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarWarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle, context);
    }

    private void init(AttributeSet attrs, int defStyle, Context context) {
        final TypedArray a = context.obtainStyledAttributes(
            attrs, R.styleable.StarWarView, defStyle, 0);

        if (a != null) {
            final int N = a.getIndexCount();

            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);

                if (attr == R.styleable.StarWarView_angle) {
                    float angle = a.getFloat(attr, mAngle);
                    setAngle(angle);
                }
                else if (attr == R.styleable.StarWarView_scrollPosition) {
                    float scrollPercent = a.getFloat(attr, 0f);
                    setScrollPosition(scrollPercent);
                }
                else if (attr == R.styleable.StarWarView_endScrollMultiplier) {
                    float scrollMult = a.getFloat(attr, 0f);
                    setEndScrollMult(scrollMult);
                }
                else if (attr == R.styleable.StarWarView_distanceFromText) {
                    float distance = a.getFloat(attr, 0f);
                    setDistanceFromText(distance);
                }
            }

            a.recycle();
        }

        initTextPaint();
    }

    //初始化文本画笔
    private void initTextPaint() {
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setColor(mTextColor);
    }

    public void setScrollPosition(float scrollPosition) {
        if (scrollPosition < 0f) {
            scrollPosition = 0f;
        }
        else if (scrollPosition > 1f) {
            scrollPosition = 1f;
        }

        if (Math.abs(mScrollPosition - scrollPosition) > FLOAT_EPSILON) {
            mScrollPosition = scrollPosition;
            invalidate();
        }
    }

    public void setAngle(float angle) {
        if (Math.abs(mAngle - angle) > FLOAT_EPSILON) {
            mAngle = angle;
            invalidate();
        }
    }

    /**
     * Sets the end scrolling multiplier value.
     * This value is multiplied by the available height of the View
     * (that is, {@link #getHeight()} less the top/bottom paddings)
     * and used to offset the end scrolling point. Adjust this to
     * have the text scrolling end in the desired position.
     * Depending on the angle value, you may need to set this to 2,
     * 3 or more to have the text get scrolled completely out of
     * view.
     *
     * @param endScrollMult The end scrolling multiplier.
     *
     * @see #setAngle(float)
     */
    public void setEndScrollMult(float endScrollMult) {
        if (Math.abs(mEndScrollMult - endScrollMult) > FLOAT_EPSILON) {
            mEndScrollMult = endScrollMult;
            invalidate();
        }
    }

    public void setDistanceFromText(float distanceFromText) {
        mDistanceFromText = distanceFromText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();

        final CharSequence text = getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        int contentWidth = getWidth() - mPaddingLeft - mPaddingRight;
        int contentHeight = getHeight() - mPaddingTop - mPaddingBottom;

        final int saveCnt = canvas.save();

        // 平移相机
        canvas.getMatrix(mMatrix);
        mCamera.save();

        int cX = contentWidth / 2 + mPaddingLeft;
        int cY = contentHeight / 2 + mPaddingTop;
        mCamera.rotateX(mAngle);
        mCamera.translate(0, 0, mDistanceFromText);
        mCamera.getMatrix(mMatrix);
        mMatrix.preTranslate(-cX, -cY);
        mMatrix.postTranslate(cX, cY);
        mCamera.restore();

        canvas.concat(mMatrix);

        canvas.translate(0f, contentHeight - mScrollPosition *
                                             (mTextLayout.getHeight() + mEndScrollMult * contentHeight));

        // 绘制文本
        mTextLayout.draw(canvas);

        canvas.restoreToCount(saveCnt);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        if (mTextPaint == null) {
            initTextPaint();
        }

        measureAndLayoutText(text);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mTextColor = color;
        initTextPaint();
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        super.setTextColor(colors);

        if (!colors.isStateful()) {
            mTextColor = colors.getDefaultColor();
        }
        else {
            mTextColor = colors.getColorForState(getDrawableState(), colors.getDefaultColor());
        }

        initTextPaint();
        invalidate();
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);

        initTextPaint();
        invalidate();
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);

        initTextPaint();
        invalidate();
    }

    @Override
    public void setTextAppearance(Context context, int resid) {
        super.setTextAppearance(context, resid);

        initTextPaint();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 测量和布局文本
        final CharSequence text = getText();
        measureAndLayoutText(text);
    }

    private void measureAndLayoutText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mTextLayout = null;
            return;
        }

        int availableWidth = getWidth() - mPaddingLeft - mPaddingRight;
        mTextLayout = new StaticLayout(text, mTextPaint, availableWidth, Layout.Alignment.ALIGN_CENTER,
                                       1.1f, 0f, true);
    }
}
