/*
 * Copyright 2010-2015 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.android.utils.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.yoavst.changesystemohelshem.R;

public class ForegroundTextView extends AppCompatTextView {

    // UI
    private Drawable foreground;

    // Controller/logic fields
    private final Rect rectPadding = new Rect();
    private boolean foregroundPadding = false;
    private boolean foregroundBoundsChanged = false;

    // Constructors
    public ForegroundTextView(Context context) {
        super(context);
    }

    public ForegroundTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForegroundTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Foreground, defStyle, 0);

        final Drawable d = a.getDrawable(R.styleable.Foreground_android_foreground);
//        foregroundPadding = a.getBoolean(R.styleable.Foreground_android_foregroundInsidePadding, false);
        foregroundPadding = false;
        // Apply foreground padding for nine patches automatically
        if (!foregroundPadding && getBackground() instanceof NinePatchDrawable) {
            final NinePatchDrawable npd = (NinePatchDrawable) getBackground();
            if (npd.getPadding(rectPadding)) {
                foregroundPadding = true;
            }
        }

        if (d != null) {
            setForeground(d);
        }
        a.recycle();
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child views in the layout.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    public void setForeground(Drawable drawable) {
        if (foreground != drawable) {
            if (foreground != null) {
                foreground.setCallback(null);
                unscheduleDrawable(foreground);
            }

            foreground = drawable;

            if (drawable != null) {
                setWillNotDraw(false);
                drawable.setCallback(this);
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
            } else {
                setWillNotDraw(true);
            }
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the drawable used as the foreground of this layout. The foreground drawable,
     * if non-null, is always drawn on top of the children.
     *
     * @return A Drawable or null if no foreground was set.
     */
    public Drawable getForeground() {
        return foreground;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (foreground != null && foreground.isStateful()) {
            foreground.setState(getDrawableState());
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || (who == foreground);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        if (foreground != null) {
            foreground.jumpToCurrentState();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        foregroundBoundsChanged = true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (foreground != null) {
            final Drawable foreground = this.foreground;

            if (foregroundBoundsChanged) {
                foregroundBoundsChanged = false;

                final int w = getRight() - getLeft();
                final int h = getBottom() - getTop();

                if (foregroundPadding) {
                    foreground.setBounds(rectPadding.left, rectPadding.top, w - rectPadding.right, h - rectPadding.bottom);
                } else {
                    foreground.setBounds(0, 0, w, h);
                }
            }
            foreground.draw(canvas);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (foreground != null) {
                    foreground.setHotspot(e.getX(), e.getY());
                }
            }
        }
        return super.onTouchEvent(e);
    }
}