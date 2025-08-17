package com.nic.webdesk;

import android.graphics.Color;

public class ColorUtils {

    public static int resolveColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return Color.WHITE;
        }

        colorStr = colorStr.trim().toLowerCase();

        if (colorStr.equals("white")) {
            return Color.WHITE;
        }

        if (colorStr.startsWith("rgba")) {
            return parseRgbaString(colorStr);
        }

        try {
            return Color.parseColor(colorStr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Color.WHITE; // fallback sicuro
        }
    }

    private static int parseRgbaString(String rgba) {
        try {
            String values = rgba.substring(rgba.indexOf("(") + 1, rgba.indexOf(")"));
            String[] parts = values.split(",");

            float r = Float.parseFloat(parts[0].trim());
            float g = Float.parseFloat(parts[1].trim());
            float b = Float.parseFloat(parts[2].trim());
            float a = Float.parseFloat(parts[3].trim());

            int red = clampColorComponent(r);
            int green = clampColorComponent(g);
            int blue = clampColorComponent(b);
            int alpha = clampAlphaComponent(a);

            return Color.argb(alpha, red, green, blue);
        } catch (Exception e) {
            e.printStackTrace();
            return Color.WHITE;
        }
    }

    private static int clampColorComponent(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }

    private static int clampAlphaComponent(float alpha) {
        return Math.max(0, Math.min(255, Math.round(alpha * 255)));
    }
}
