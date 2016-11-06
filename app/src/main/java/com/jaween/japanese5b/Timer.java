package com.jaween.japanese5b;

import android.util.Log;

public class Timer {

  private static final String TAG = Timer.class.getSimpleName();

  private long startTime;
  private long ellapsedTime = 0;
  private boolean running = false;
  private boolean started = false;

  public void startPause() {
    if (running) {
      ellapsedTime = System.currentTimeMillis() - startTime;
      running = false;
      Log.i(TAG, "Pausing at " + ellapsedTime + "ms");
    } else {
      startTime = System.currentTimeMillis() - ellapsedTime;
      running = true;
      Log.i(TAG, "Starting/resuming at " + ellapsedTime + "ms");
    }
  }

  public long stop() {
    if (running) {
      long totalTime = System.currentTimeMillis() - startTime;
      ellapsedTime = 0;
      running = false;
      started = false;
      Log.i(TAG, "Stopping at " + ellapsedTime + "ms");
      return totalTime;
    } else {
      Log.i(TAG, "Stopping at 0ms");
      return 0;
    }
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isStarted() {
    return started;
  }
}
