package com.jaween.japanese5b;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Controls a study session.
 */
public class StudyFragment extends Fragment
        implements GestureOverlayView.OnGesturePerformedListener {

  public interface SessionListener {
    void onSessionCompleteListener();
  }

  private static final String TAG = StudyFragment.class.getSimpleName();

  private static final int showAssistButtonIncorrectCountThreshold = 2;

  private GestureOverlayView gestureOverlayView;
  private HandwritingController handwritingController;
  private SpacedRepetition spacedRepetition;
  private SessionListener sessionListener;

  private ImageView answerResultImage;
  private TextView questionTextView;
  private TextView sessionProgressTextView;
  private ProgressBar sessionProgressBar;
  private Button skipButton;
  private Button assistButton;
  private Toolbar toolbar;
  private int incorrectCount = 0;
  private boolean assistShown = false;

  public static StudyFragment newInstance() {
    Bundle arguments = new Bundle();
    StudyFragment fragment = new StudyFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);

    handwritingController = new HandwritingController(getContext());
    spacedRepetition = new SpacedRepetition();


    /*if (OpenCVLoader.initDebug()) {
      Log.e(TAG, "OpenCV successfully loaded");
    } else {
      Log.e(TAG, "OpenCV failed to load");
    }*/
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_study_prod, null);
    setupViews(rootView);
    nextCard();
    return rootView;
  }

  private void setupViews(View v) {
    answerResultImage = (ImageView) v.findViewById(R.id.answer_result_image);
    questionTextView = (TextView) v.findViewById(R.id.question_text);
    sessionProgressBar = (ProgressBar) v.findViewById(R.id.session_progress_bar);

    skipButton = (Button) v.findViewById(R.id.skip_button);
    skipButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        spacedRepetition.skipCurrentCard();
      }
    });

    toolbar = (Toolbar) v.findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

    assistButton = (Button) v.findViewById(R.id.assist_button);
    Util.animateHover(assistButton, 10);

    gestureOverlayView = (GestureOverlayView) v.findViewById(R.id.gesture_overlay);
    gestureOverlayView.addOnGesturePerformedListener(this);
    gestureOverlayView.setGestureStrokeLengthThreshold(0.0f);
    gestureOverlayView.setGestureStrokeAngleThreshold(0.0f);
    gestureOverlayView.setGestureStrokeSquarenessTreshold(0.0f);
  }

  @Override
  public void onResume() {
    super.onResume();
    spacedRepetition.resume();
  }

  @Override
  public void onPause() {
    super.onPause();
    spacedRepetition.pause();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof SessionListener)) {
      throw new ClassCastException("Host of " + StudyFragment.class.getSimpleName() + " must " +
          "implement " + SessionListener.class.getSimpleName());
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        getActivity().onBackPressed();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Either moves to the next question and updates the UI, or if there are no more cards remaining,
   * ends the session.
   */
  private void nextCard() {
    // Progress text
    int completeCardCount = spacedRepetition.getSessionCompleteCardCount();
    int totalCardCount = spacedRepetition.getSessionTotalCardCount();
    float progressRatio = (float) completeCardCount / (float) totalCardCount;
    int progress = (int) (progressRatio * sessionProgressBar.getMax());

    // Progress bar
    Util.animateProgess(sessionProgressBar, progress);

    if (spacedRepetition.getSessionRemainingCardCount() > 0) {
      // Question
      Drawable drawable = Util.getTintedDrawable(getContext(), R.mipmap.ic_question_48dp,
          android.R.color.primary_text_light);
      answerResultImage.setImageDrawable(drawable);
      questionTextView.setText(spacedRepetition.getCurrentCard().getQuestionString());

      // Assist
      incorrectCount = 0;
      hideAssistButton();
      assistShown = false;
    } else {
      if (sessionListener != null) {
        sessionListener.onSessionCompleteListener();
      }
      gestureOverlayView.setEnabled(false);
    }
  }

  private void answerCorrect() {
    Drawable drawable = Util.getTintedDrawable(getContext(), R.mipmap.ic_correct_48dp,
        R.color.colorCorrect);
    Util.animateImageChange(getContext(), answerResultImage, drawable, R.anim.fade_in,
        R.anim.fade_out, new Util.AnimationEndListener() {
          @Override
          public void onAnimationEnd() {
            nextCard();
          }
        });

    spacedRepetition.answerCurrentCard();
  }

  private void answerIncorrect() {
    Drawable incorrectDrawable = Util.getTintedDrawable(getContext(), R.mipmap.ic_incorrect_48dp,
        R.color.colorIncorrect);
    final Drawable questionDrawable = Util.getDrawable(getContext(), R.mipmap.ic_question_48dp);
    Util.animateImageChange(getContext(), answerResultImage, incorrectDrawable, R.anim.fade_in,
        R.anim.fade_out, new Util.AnimationEndListener() {
          @Override
          public void onAnimationEnd() {
            Util.animateImageChange(getContext(), answerResultImage, questionDrawable,
                R.anim.fade_in, R.anim.fade_out, null);
          }
        });

    incorrectCount++;
    if (!assistShown && incorrectCount >= showAssistButtonIncorrectCountThreshold) {
      showAssistButton();
    }
  }

  private void showAssistButton() {
    assistShown = true;
    Util.circularRevealView(assistButton);
  }

  private void hideAssistButton() {
    Util.hideView(assistButton);
  }

  @Override
  public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
    SpacedRepetition.Card card = spacedRepetition.getCurrentCard();
    boolean correct = handwritingController.analyseHandwriting(gesture, card.getAnswerString());
    if (correct) {
      answerCorrect();
    } else {
      answerIncorrect();
    }
  }
}
