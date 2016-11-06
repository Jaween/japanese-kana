package com.jaween.japanese5b;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.util.Log;

import java.util.ArrayList;

/**
 * Handles loading the gesture resource and analysing handwriting for accuracy and correctness.
 */
public class HandwritingController {
  private static final String TAG = HandwritingController.class.getSimpleName();

  private GestureLibrary gestureLibrary;

  public HandwritingController(Context context) {
    gestureLibrary = GestureLibraries.fromRawResource(context, R.raw.gestures_ka_ki_shi_tsu);
    if (!gestureLibrary.load()) {
      Log.e(TAG, "Could not load gesture library");
    }
  }

  public boolean analyseHandwriting(Gesture gesture, String symbolName) {
    String log = "";

    ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
    boolean correct = false;
    for (Prediction prediction : predictions) {
      int expectedStrokeCount = gestureLibrary.getGestures(prediction.name).get(0).getStrokesCount();
      int drawnStrokeCount = gesture.getStrokesCount();
      if (expectedStrokeCount == drawnStrokeCount) {
        log += prediction.name + "(" + String.format("%.1f", prediction.score) + "), ";
        if (prediction.score > 1.0f && prediction.name.equals(symbolName)) {
          correct = true;
        }
      }
    }

    Log.i(TAG, "Correct? " + correct + ", Symbol name " + symbolName + ", Handwriting: " + log);
    return correct;
  }
}
