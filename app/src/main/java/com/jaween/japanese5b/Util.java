package com.jaween.japanese5b;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Utility functions.
 */
public class Util {
  /**
   * Creates a coloured version of the drawable on Android Lollipop and above. Returns a non-tinted
   * drawable on versions below Lollipop.
   * @param context The context to load the resource and theme (if applicable)
   * @param resourceId The drawable resource to load
   * @param colorResourceId The colour to tint the drawable
   * @return The loaded (and possibly tinted) drawable
   */
  public static Drawable getTintedDrawable(Context context, int resourceId, int colorResourceId) {
    int color;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      color = context.getResources().getColor(colorResourceId, context.getTheme());
    } else {
      color = context.getResources().getColor(colorResourceId);
    }

    Drawable drawable;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      drawable = context.getResources().getDrawable(resourceId, context.getTheme());
      drawable.setTint(color);
    } else {
      drawable = context.getResources().getDrawable(resourceId);
    }
    return drawable;
  }

  /**
   * Animates the swapping of two images on an ImageView.
   * Based on http://stackoverflow.com/a/14183532.
   * @param context The context to lead the resources
   * @param imageView The ImageView on which to animate the image change
   * @param newImageDrawable The resulting image
   * @param inResource The animation to play on the new image
   * @param outResource The animation to play on the current image
   */
  public static void animateImageChange(Context context, final ImageView imageView,
                                        final Drawable newImageDrawable, int inResource,
                                        int outResource) {
    final Animation animationIn = AnimationUtils.loadAnimation(context, inResource);
    final Animation animationOut  = AnimationUtils.loadAnimation(context, outResource);
    animationOut.setAnimationListener(new Animation.AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {

        }

        @Override public void onAnimationRepeat(Animation animation) {

        }

        @Override public void onAnimationEnd(Animation animation)
        {
            imageView.setImageDrawable(newImageDrawable);
            animationIn.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {

                }

                @Override public void onAnimationRepeat(Animation animation) {

                }

                @Override public void onAnimationEnd(Animation animation) {

                }
            });
            imageView.startAnimation(animationIn);
        }
    });
    imageView.startAnimation(animationOut);
  }
}
