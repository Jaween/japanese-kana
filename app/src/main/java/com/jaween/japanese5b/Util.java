package com.jaween.japanese5b;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Utility functions.
 */
public class Util {
  /**
   * Creates a coloured version of the drawable on Android Lollipop and above. Returns a non-tinted
   * drawable on versions below Lollipop.
   * @param context The context to load the resource and theme (if applicable)
   * @param resourceId The drawable resource to load
   * @param tintColor The colour tint the drawable
   * @return The loaded (and possibly tinted) drawable
   */
  public static Drawable getTintedDrawable(Context context, int resourceId, int tintColor) {
    Drawable drawable;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      drawable = context.getResources().getDrawable(resourceId, context.getTheme());
      drawable.setTint(tintColor);
    } else {
      drawable = context.getResources().getDrawable(resourceId);
    }
    return drawable;
  }
}
