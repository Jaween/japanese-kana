package com.jaween.japanese5b;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Utility functions.
 */
public class Util {

  public interface AnimationEndListener {
    void onAnimationEnd();
  }

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
   * @param listener Callback fired when the animation is complete
   */
  public static void animateImageChange(Context context,
                                        final ImageView imageView,
                                        final Drawable newImageDrawable,
                                        int inResource,
                                        int outResource,
                                        final AnimationEndListener listener) {
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
                  Handler handler = new Handler();
                  handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      if (listener != null) {
                        listener.onAnimationEnd();
                      }
                    }
                  }, 300);
                }
            });
            imageView.startAnimation(animationIn);
        }
    });
    imageView.startAnimation(animationOut);
  }

  public static void animateHover(final View view, final int maxHeight) {
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.setDuration(1500);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float value = (Float) animation.getAnimatedValue();
        float elevation = maxHeight * (float) Math.sin(value * Math.PI) + maxHeight / 2;
        view.setElevation(elevation);
      }
    });
    animator.setRepeatMode(ValueAnimator.REVERSE);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.start();
  }

  public static void circularRevealView(final View view) {
    Animator anim = ViewAnimationUtils.createCircularReveal(
        view,
        view.getMeasuredWidth() / 2,
        view.getMeasuredHeight() / 2,
        0,
        view.getMeasuredWidth() / 2);
    anim.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {
        view.setAlpha(1.0f);
        view.setVisibility(View.VISIBLE);
      }

      @Override
      public void onAnimationEnd(Animator animation) {

      }

      @Override
      public void onAnimationCancel(Animator animation) {

      }

      @Override
      public void onAnimationRepeat(Animator animation) {

      }
    });
    anim.start();
  }

  public static void hideView(final View view) {
    view.animate()
        .alpha(0.0f)
        .setListener(new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {

          }

          @Override
          public void onAnimationEnd(Animator animation) {
            view.setVisibility(View.GONE);
          }

          @Override
          public void onAnimationCancel(Animator animation) {

          }

          @Override
          public void onAnimationRepeat(Animator animation) {

          }
        }).start();
  }

  public static void animateProgess(final ProgressBar progressBar, int toProgress) {
    ValueAnimator animator = ValueAnimator.ofInt(progressBar.getProgress(), toProgress);
    animator.setDuration(500);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        int progress = (Integer) animation.getAnimatedValue();
        progressBar.setProgress(progress);
      }
    });
    animator.start();
  }
}
