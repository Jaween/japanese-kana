package com.jaween.japanese5b;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class StudyActivity extends AppCompatActivity implements StudyFragment.SessionListener {

  private static final String TAG_STUDY_FRAGMENT = "study_fragment";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_study);

    StudyFragment studyFragment = StudyFragment.newInstance();
    if (getSupportFragmentManager().findFragmentByTag(TAG_STUDY_FRAGMENT) == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.fragment_container, studyFragment, TAG_STUDY_FRAGMENT)
          .commit();
    }
  }

  @Override
  public void onSessionCompleteListener() {
    Toast.makeText(this, "Session Complete", Toast.LENGTH_SHORT).show();
  }
}
