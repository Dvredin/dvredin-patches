package org.ungoogled.ui;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import java.util.WeakHashMap;

/** Gear presentation only; no dialog lifecycle, navigation or animation overrides. */
public final class SettingsMenu {
    private static final WeakHashMap<View, Drawable> GEARS = new WeakHashMap<>();
    private SettingsMenu() {}

    public static CharSequence label(View view) {
        return view.getContext().getString(0x7f141d91); // string/SETTINGS, supported Maps input
    }

    public static void drawGear(View view, Canvas canvas) {
        CharSequence title = label(view);
        view.setContentDescription(title);
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++)
                group.getChildAt(i).setContentDescription(title);
        }
        Drawable gear = GEARS.get(view);
        if (gear == null) {
            gear = view.getContext().getDrawable(0x7f0805fc).mutate(); // Maps' native settings icon
            GEARS.put(view, gear);
        }
        boolean dark = (view.getResources().getConfiguration().uiMode & 0x30) == 0x20;
        gear.setTint(dark || Shapes.BLACK ? 0xffe3e3e3 : 0xff5f6368);
        int size = Math.round(24 * view.getResources().getDisplayMetrics().density);
        int x = (view.getWidth() - size) / 2, y = (view.getHeight() - size) / 2;
        gear.setBounds(x, y, x + size, y + size);
        gear.draw(canvas);
    }
}
