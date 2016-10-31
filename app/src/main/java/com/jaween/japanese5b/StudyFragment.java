package com.jaween.japanese5b;

import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Controls a study session.
 */
public class StudyFragment extends Fragment
        implements GestureOverlayView.OnGesturePerformedListener {

  private static String TAG = StudyFragment.class.getSimpleName();

  private GestureOverlayView gestureOverlayView;
  private HandwritingController handwritingController;
  private SpacedRepetition spacedRepetition;

  private Button clearButton;
  private Button doneButton;
  private Button skipButton;
  private ImageView answerResultImage;
  private TextView questionTextView;

  public static StudyFragment newInstance() {
    Bundle arguments = new Bundle();
    StudyFragment fragment = new StudyFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    handwritingController = new HandwritingController(getContext());
    spacedRepetition = new SpacedRepetition();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_study_prod, null);
    setupViews(rootView);
    updateQuestion();
    return rootView;
  }

  private void setupViews(View v) {
    clearButton = (Button) v.findViewById(R.id.clear);
    doneButton = (Button) v.findViewById(R.id.done_button);
    skipButton = (Button) v.findViewById(R.id.skip_button);
    answerResultImage = (ImageView) v.findViewById(R.id.answer_result_image);
    questionTextView = (TextView) v.findViewById(R.id.question_text);

    // TODO(jaween): Implement 'done' action, currently this button performs 'next' action
    doneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        moveToNextQuestion(SpacedRepetition.Difficulty.MEDIUM);
      }
    });

    skipButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        moveToNextQuestion(SpacedRepetition.Difficulty.SKIP);
      }
    });

    gestureOverlayView = (GestureOverlayView) v.findViewById(R.id.gesture_overlay);
    gestureOverlayView.addOnGesturePerformedListener(this);
    gestureOverlayView.setUncertainGestureColor(Color.MAGENTA);
    gestureOverlayView.setGestureStrokeLengthThreshold(0.0f);
    gestureOverlayView.setGestureStrokeAngleThreshold(0.0f);
    gestureOverlayView.setGestureStrokeSquarenessTreshold(0.0f);

    //tempText = (TextView) v.findViewById(R.id.temp_text);

    /*LinearLayout mainLayout = (LinearLayout) v.findViewById(R.id.temp_layout);
    for (String gestureName : gestureLibrary.getGestureEntries()) {
      LinearLayout layout = new LinearLayout(getContext());
      layout.setOrientation(LinearLayout.HORIZONTAL);
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.MATCH_PARENT);
      layout.setLayoutParams(params);
      for (Gesture gesture : gestureLibrary.getGestures(gestureName)) {
        Bitmap bitmap = gesture.toBitmap(128, 128, 10, 10);
        ImageView panel = new ImageView(getContext());
        panel.setLayoutParams(
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        panel.setImageBitmap(bitmap);
        layout.addView(panel);
      }
      mainLayout.addView(layout);
    }*/
  }

  private void moveToNextQuestion(SpacedRepetition.Difficulty difficulty) {
    spacedRepetition.answerQuestion(difficulty);
    updateQuestion();
  }

  private void updateQuestion() {
    questionTextView.setText(spacedRepetition.getCurrentQuestion().getQuestionString());

    Drawable drawable = Util.getTintedDrawable(getContext(), R.mipmap.ic_question_48dp,
        Color.MAGENTA);
    answerResultImage.setImageDrawable(drawable);
  }

  private void showCorrectAnswerAnimation() {
    Drawable drawable = Util.getTintedDrawable(getContext(), R.mipmap.ic_correct_48dp,
        Color.GREEN);
    answerResultImage.setImageDrawable(drawable);
  }

  private void showIncorrectAnswerAnimation() {
    Drawable drawable = Util.getTintedDrawable(getContext(), R.mipmap.ic_incorrect_48dp, Color.RED);
    answerResultImage.setImageDrawable(drawable);
  }

  @Override
  public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
    SpacedRepetition.Question question = spacedRepetition.getCurrentQuestion();
    boolean correct = handwritingController.analyseHandwriting(gesture, question.getAnswerString());
    if (correct) {
      showCorrectAnswerAnimation();
    } else {
      showIncorrectAnswerAnimation();
    }
  }
}
