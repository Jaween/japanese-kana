package com.jaween.japanese5b;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Handles the intervals between repeated serving of question based on prior results.
 */
public class SpacedRepetition {

  private enum Difficulty {
    SKIP, EASY, MEDIUM, HARD, REPEAT
  }

  private static final String TAG = SpacedRepetition.class.getSimpleName();
  private static final int TIME_TAKEN_EASY = 4000;
  private static final int TIME_TAKEN_MEDIUM = 8000;

  private PriorityQueue<Card> revisionCards;
  private PriorityQueue<Card> remainingSessionCards;
  private List<Card> unseenCards;
  private List<Card> completeSessionCards;
  private Timer timer = new Timer();

  public SpacedRepetition() {
    int currentSessionTime = getCurrentSessionTime();
    int previousSessionTime = getPreviousSessionTime();

    revisionCards = loadRevisionCards(previousSessionTime, currentSessionTime);
    unseenCards = loadUnseenCards();
    remainingSessionCards = mergePriorityQueueAndList(revisionCards, unseenCards);
    completeSessionCards = new ArrayList<>();
    Log.i(TAG, "Cards for this session are: " + Arrays.deepToString(remainingSessionCards.toArray()));
  }

  /**
   * Returns but does not remove the top card of the desk. This also starts the timer if this is
   * the first time viewing this card this time.
   * @return current top card of the deck
   */
  public Card getCurrentCard() {
    if (!timer.isStarted() && !timer.isRunning()) {
      timer.startPause();
    }
    return remainingSessionCards.peek();
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
    // TODO(jaween): Time units are sessions scale, add inter-session repetition time units
    // Maybe compute the average time per card and then insert the card a number of cards down the
    // list based on that (but that would get out of whack when the next card is inserted back into
    // the current sessions list



    Card card = remainingSessionCards.poll();
    int nextSession = computeNextSession(difficulty);
    card.setInterval(card.getInterval() + nextSession);
    if (card.getInterval() <= getCurrentSessionTime()) {
      remainingSessionCards.add(card);
    } else {
      completeSessionCards.add(card);
    }

    Log.i(TAG, "Current session cards are " + Arrays.deepToString(remainingSessionCards.toArray()));
  }

  private PriorityQueue<Card> loadRevisionCards(int previousSessionTime, int currentSessionTime) {
    // TODO(jaween): Load the cards needing revision from a database based on the session times
    PriorityQueue cards = new PriorityQueue<>();
    cards.add(new Card("Katakana Shi", "katakana_shi", 3));
    cards.add(new Card("Katakana Tsu", "katakana_tsu", 3));
    return cards;
  }

  private List<Card> loadUnseenCards() {
    // TODO(jaween): Load a set of new cards from a database
    List<Card> cards = new ArrayList<>();
    cards.add(new Card("Hiragana Ka", "hiragana_ka", -1));
    cards.add(new Card("Hiragana Ki", "hiragana_ki", -1));
    cards.add(new Card("Hiragana Ku", "hiragana_ku", -1));
    return cards;
  }

  private int getCurrentSessionTime() {
    // TODO(jaween): Load session time from shared preferences
    return 3;
  }

  private int getPreviousSessionTime() {
    // TODO(jaween): Load session time from shared preferences
    return 0;
  }

  private PriorityQueue<Card> mergePriorityQueueAndList(PriorityQueue<Card> priorityQueue,
                                                        List<Card> list) {
    PriorityQueue<Card> merged = new PriorityQueue<>(priorityQueue);
    for (Card card : list) {
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

  private int computeNextSession(Difficulty difficulty) {
    switch (difficulty) {
      case SKIP:
        return 0;
      case EASY:
        return 2;
      case MEDIUM:
        return 1;
      case HARD:
        return 0;
      case REPEAT:
        return 4;
      default:
        return 0;
    }
  }

  public static class Card implements Comparable<Card> {
    private String questionString;
    private String answerString;
    private int interval;

    public Card(String questionString, String answerString, int interval) {
      this.questionString = questionString;
      this.answerString = answerString;
      this.interval = interval;
    }

    public String getQuestionString() {
      return questionString;
    }

    public String getAnswerString() {
      return  answerString;
    }

    public void setInterval(int interval) {
      this.interval = interval;
    }

    public int getInterval() {
      return interval;
    }

    @Override
    public int compareTo(Card other) {
      if (other == null) {
        return 0;
      } else {
        return interval - other.getInterval();
      }
    }

    @Override
    public String toString() {
      return "{ " + getQuestionString() + ", " + getAnswerString() + ", " + interval + "}";
    }
  }
}
