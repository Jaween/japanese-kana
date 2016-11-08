package com.jaween.japanese5b;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the intervals between repeated serving of question based on prior results.
 */
public class SpacedRepetition {

  private enum Difficulty {
    SKIP, EASY, MEDIUM, HARD
  }

  private static final String TAG = SpacedRepetition.class.getSimpleName();
  private static final int TIME_TAKEN_EASY = 4000;
  private static final int TIME_TAKEN_MEDIUM = 8000;
  private static final int REINSERTION_DISTANCE_EASY = 20;
  private static final int REINSERTION_DISTANCE_MEDIUM = 10;
  private static final int REINSERTION_DISTANCE_HARD = 5;
  private static final int REINSERTION_DISTANCE_CARD_COMPLETE = -1;
  private static final int SESSION_REMAINING_SUBTRACT_EASY = 10;
  private static final int SESSION_REMAINING_SUBTRACT_MEDIUM = 4;
  private static final int SESSION_REMAINING_SUBTRACT_HARD = 3;

  private List<Card> remainingSessionCards;
  private List<Card> completeSessionCards;
  private Timer timer = new Timer();

  public SpacedRepetition() {
    long currentSessionTime = getCurrentSessionTime();
    long previousSessionTime = getPreviousSessionTime();

    List<Card> revisionCards = loadRevisionCards(previousSessionTime, currentSessionTime);
    List<Card> unseenCards = loadUnseenCards();
    remainingSessionCards = shuffleMergeLists(revisionCards, unseenCards);
    completeSessionCards = new ArrayList<>();
    Log.i(TAG, "Cards for this session are: "
        + Arrays.deepToString(remainingSessionCards.toArray()));
  }

  /**
   * Returns but does not remove the top card of the desk. This also starts the timer if this is
   * the first time viewing this card this time.
   * @return current top card of the deck or null if the deck is empty
   */
  public Card getCurrentCard() {
    if (!timer.isStarted() && !timer.isRunning()) {
      timer.startPause();
    }

    Card card = null;
    if (remainingSessionCards.size() > 0) {
      card = remainingSessionCards.get(0);
    }
    return card;
  }

  /**
   * The Card returned from the subsequent call to getNextCard() will change after calling this
   * function.
   */
  public void answerCurrentCard() {
    Difficulty difficulty = computeDifficulty(timer.stop());
    reinsertCard(difficulty);
  }

  public void skipCurrentCard() {
    reinsertCard(Difficulty.SKIP);
  }

  public int getSessionRemainingCardCount() {
    return remainingSessionCards.size();
  }

  public int getSessionCompleteCardCount() {
    return completeSessionCards.size();
  }

  public float getSessionProgress() {
    int progress = completeSessionCards.size() * Card.SESSION_REMAINING_SCORE_INITIAL_VALUE;
    for (Card card : remainingSessionCards) {
      progress += Card.SESSION_REMAINING_SCORE_INITIAL_VALUE - card.getSessionRemainingScore();
    }
    int total = (remainingSessionCards.size() + completeSessionCards.size())
        * Card.SESSION_REMAINING_SCORE_INITIAL_VALUE;
    return (float) progress / (float) total;
  }

  public int getSessionTotalCardCount() {
    return remainingSessionCards.size() + completeSessionCards.size();
  }

  public void resume() {
    if (timer.isStarted() && !timer.isRunning()) {
      timer.startPause();
    }
  }

  public void pause() {
    if (timer.isStarted() && timer.isRunning()) {
      timer.startPause();
    }
  }

  private void reinsertCard(Difficulty difficulty) {
    Card card = remainingSessionCards.remove(0);
    int reinsertionDistance = computeReinsertionDistanceAndSessionRemainingScore(card, difficulty);

    if (card.getSessionRemainingScore() <= 0 ||
        reinsertionDistance == REINSERTION_DISTANCE_CARD_COMPLETE) {
      // Study of this card is complete for this session
      // TODO(jaween): Set the interval correctly (it wont increase if the initial interval is zero)
      card.setNextStudy(getCurrentSessionTime() + card.getInterval());
      card.setInterval(card.getInterval() * 2);
      Log.i(TAG, "Complete card " + card.getQuestionString());
      completeSessionCards.add(card);
    } else {
      // Reinsert card
      if (reinsertionDistance > remainingSessionCards.size()) {
        reinsertionDistance = remainingSessionCards.size();
      }
      remainingSessionCards.add(reinsertionDistance, card);
    }

    Log.i(TAG, "Current session cards are " + Arrays.deepToString(remainingSessionCards.toArray()));
  }

  private List<Card> loadRevisionCards(long previousSessionTime, long currentSessionTime) {
    // TODO(jaween): Load the cards needing revision from a database based on the session times
    List cards = new ArrayList();
    cards.add(new Card("Katakana Shi", "katakana_shi", 3, 1));
    cards.add(new Card("Katakana Tsu", "katakana_tsu", 3, 1));
    return cards;
  }

  private List<Card> loadUnseenCards() {
    // TODO(jaween): Load a set of new cards from a database
    List<Card> cards = new ArrayList<>();
    cards.add(new Card("Hiragana Ka", "hiragana_ka", -1, -1));
    cards.add(new Card("Hiragana Ki", "hiragana_ki", -1, -1));
    cards.add(new Card("Hiragana Ku", "hiragana_ku", -1, -1));
    return cards;
  }

  private long getCurrentSessionTime() {
    // TODO(jaween): Load session time from shared preferences
    return 3;
  }

  private long getPreviousSessionTime() {
    // TODO(jaween): Load session time from shared preferences
    return 0;
  }

  /**
   * Creates a new list with the contents of the two lists merged. Places items in the first list
   * ahead of the items in the second list.
   * @param listA First list
   * @param listB Second list
   * @return A new merged list
   */
  private List<Card> shuffleMergeLists(List<Card> listA, List<Card> listB) {
    List<Card> merged = new ArrayList<>(listA);
    for (Card card : listB) {
      merged.add(card);
    }
    return merged;
  }

  private Difficulty computeDifficulty(long timeTaken) {
    SpacedRepetition.Difficulty difficulty;
    if (timeTaken <= TIME_TAKEN_EASY) {
      difficulty = SpacedRepetition.Difficulty.EASY;
    } else if (timeTaken <= TIME_TAKEN_MEDIUM) {
      difficulty = SpacedRepetition.Difficulty.MEDIUM;
    } else {
      difficulty = SpacedRepetition.Difficulty.HARD;
    }
    Log.i(TAG, "Took " + timeTaken + "ms to answer, difficulty was " + difficulty);
    return difficulty;
  }

  private int computeReinsertionDistanceAndSessionRemainingScore(Card card, Difficulty difficulty) {
    switch (difficulty) {
      case EASY:
        // First time card was seen during the session and it was answered easily, this card can
        // move to the next bucket
        if (card.getSessionRemainingScore() == Card.SESSION_REMAINING_SCORE_INITIAL_VALUE) {
          return REINSERTION_DISTANCE_CARD_COMPLETE;
        } else {
          card.subtractSessionRemainingScore(SESSION_REMAINING_SUBTRACT_EASY);
        }
        return REINSERTION_DISTANCE_EASY;
      case MEDIUM:
        card.subtractSessionRemainingScore(SESSION_REMAINING_SUBTRACT_MEDIUM);
        return REINSERTION_DISTANCE_MEDIUM;
      case HARD:
        card.subtractSessionRemainingScore(SESSION_REMAINING_SUBTRACT_HARD);
        return REINSERTION_DISTANCE_HARD;
      case SKIP:
        return remainingSessionCards.size();
      default:
        Log.e(TAG, "Unknown " + Difficulty.class.getSimpleName() + " " + difficulty);
        return remainingSessionCards.size();
    }
  }

  public static class Card implements Comparable<Card> {
    private static final int SESSION_REMAINING_SCORE_INITIAL_VALUE = 10;

    private String questionString;
    private String answerString;
    private long nextStudy;
    private long interval;
    private int sessionRemainingScore;

    public Card(String questionString, String answerString, long nextStudy, long interval) {
      this.questionString = questionString;
      this.answerString = answerString;
      this.nextStudy = nextStudy;
      this.interval = interval;
      sessionRemainingScore = SESSION_REMAINING_SCORE_INITIAL_VALUE;
    }

    public String getQuestionString() {
      return questionString;
    }

    public String getAnswerString() {
      return  answerString;
    }

    public void setInterval(long interval) {
      this.interval = interval;
    }

    public long getInterval() {
      return interval;
    }

    public void setNextStudy(long nextStudy) {
      this.nextStudy = nextStudy;
    }

    public long getNextStudy() {
      return nextStudy;
    }

    public int getSessionRemainingScore() {
      return sessionRemainingScore;
    }

    public void subtractSessionRemainingScore(int amount) {
      sessionRemainingScore -= amount;
    }

    @Override
    public String toString() {
      return "{ " + getQuestionString() + ", " + getAnswerString() + ", " + interval + "}";
    }

    @Override
    public int compareTo(Card other) {
      if (other == null) {
        return 0;
      } else {
        return (int) (interval - other.getInterval());
      }
    }
  }
}
