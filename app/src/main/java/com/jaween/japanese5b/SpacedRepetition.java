package com.jaween.japanese5b;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles the intervals between repeated serving of question based on prior results.
 */
public class SpacedRepetition {
  public enum Difficulty {
    SKIP, EASY, MEDIUM, HARD
  }

  private List<Question> questions;

  public SpacedRepetition() {
    // TODO(jaween): Load questions from a database
    questions = new LinkedList<>();
    questions.add(new Question("Write Hiragana ka", "hiragana_ka"));
    questions.add(new Question("Write Hiragana ki", "hiragana_ki"));
    questions.add(new Question("Write Hiragana ku", "hiragana_ku"));
    questions.add(new Question("Write Katakana shi", "katakana_shi"));
    questions.add(new Question("Write Katakana tsu", "katakana_tsu"));
  }

  public Question getCurrentQuestion() {
    return questions.get(0);
  }

  /**
   * The subsequent call to getNextQuestion() will be different after calling this function.
   * @param difficulty The difficulty that the user had in answering the current question
   */
  public void answerQuestion(Difficulty difficulty) {
    computeInterval(getCurrentQuestion(), difficulty);
    moveToNextQuestion();
  }

  private void moveToNextQuestion() {
    Question question = questions.remove(0);
    questions.add(question);
  }

  // TODO(jaween): Look at https://en.wikipedia.org/wiki/Spaced_repetition
  private void computeInterval(Question question, Difficulty difficulty) {

  }

  public static class Question {
    private String questionString;
    private String answerString;
    private int score;

    public Question(String questionString, String answerString) {
      this.questionString = questionString;
      this.answerString = answerString;
    }

    public String getQuestionString() {
      return questionString;
    }

    public String getAnswerString() {
      return  answerString;
    }

    public int getScore() {
      return score;
    }
  }
}
