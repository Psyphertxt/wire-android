/**
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.ui.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.waz.zclient.ui.R;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerLayout extends LinearLayout {

    public static final String TAG = ColorPickerLayout.class.getName();

    private static final String[] EMOJIS = new String[] {
        "\u2764",          // love
        "\uD83D\uDC4D",    // thumbs up
        "\uD83D\uDC4E",    // thumbs down
        "\uD83D\uDE03",    // smiley
        "\uD83D\uDE26",    // frowning face
        "\uD83D\uDCA9"     // poop
    };
    // the list of colors offered by the app
    private int[] accentColors;

    private List<ColorPickerView> colorDotViews = new ArrayList<>();

    private OnColorSelectedListener onColorSelectedListener;
    private OnWidthChangedListener onWidthChangedListener;

    private int currentDotRadius = getResources().getDimensionPixelSize(R.dimen.color_picker_small_dot_radius);
    private int currentEmojiSize = getResources().getDimensionPixelSize(R.dimen.sketch__emoji__icon_size__small);

    public ColorPickerLayout(Context context) {
        this(context, null);
    }

    public ColorPickerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        int[] colors = getResources().getIntArray(R.array.accents_color);
        setAccentColors(colors, colors[0]);
        invalidate();
    }


    private void populateDotsView(int selectedColor) {
        if (accentColors == null) {
            return;
        }
        if (getChildCount() != 0) {
            removeAllViews();
        }

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        for (int accentColor: accentColors) {
            final ColorPickerDotView dot = (ColorPickerDotView) layoutInflater.inflate(R.layout.color_picker_dot_view, this, false);
            dot.setColor(accentColor);
            dot.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentColor(dot);
                }
            });
            if (selectedColor == accentColor) {
                dot.setSelected(currentDotRadius);
            }
            colorDotViews.add(dot);
            this.addView(dot);
        }

        for (String emoji : EMOJIS) {
            final ColorPickerEmojiView emojiView = (ColorPickerEmojiView) layoutInflater.inflate(R.layout.color_picker_emoji_view, this, false);
            emojiView.setEmoji(emoji);
            emojiView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentColor(emojiView);
                }
            });
            colorDotViews.add(emojiView);
            this.addView(emojiView);
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                //to notify of width change
                if (onWidthChangedListener == null) {
                    return;
                }
                onWidthChangedListener.onScrollWidthChanged(getWidth());
            }
        }, 0);
    }

    public void setAccentColors(int[] colors, int selectedColor) {
        accentColors = colors;
        populateDotsView(selectedColor);
        invalidate();
    }

    public void setCurrentColor(ColorPickerView view) {
        for (ColorPickerView colorPickerView: colorDotViews) {
            if (colorPickerView != view) {
                colorPickerView.setUnselected();
            } else {
                if (view instanceof ColorPickerDotView) {
                    colorPickerView.setSelected(currentDotRadius);
                    currentDotRadius = colorPickerView.getSize();
                    ColorPickerDotView colorPickerDotView = (ColorPickerDotView) colorPickerView;
                    onColorSelectedListener.onColorSelected(colorPickerDotView.getCircleColor(), colorPickerDotView.getStrokeSize());
                } else {
                    colorPickerView.setSelected(currentEmojiSize);
                    currentEmojiSize = colorPickerView.getSize();
                    ColorPickerEmojiView colorPickerEmojiView = (ColorPickerEmojiView) colorPickerView;
                    onColorSelectedListener.onEmojiSelected(colorPickerEmojiView.getEmoji(), colorPickerEmojiView.getEmojiSize());
                }
            }
        }
        invalidate();
    }

    /**
     * Sets the callback of the parent
     */
    public void setOnColorSelectedListener(OnColorSelectedListener onColorSelectedListener) {
        this.onColorSelectedListener = onColorSelectedListener;
    }

    public void setOnWidthChangedListener(OnWidthChangedListener onWidthChangedListener) {
        this.onWidthChangedListener = onWidthChangedListener;
    }

    /**
     * Callback to parent
     */
    public interface OnColorSelectedListener {
        void onColorSelected(int color, int strokeSize);
        void onEmojiSelected(String emoji, int size);
    }

    public interface OnWidthChangedListener {
        void onScrollWidthChanged(int width);
    }
}
