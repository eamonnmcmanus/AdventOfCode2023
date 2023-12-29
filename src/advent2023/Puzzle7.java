package advent2023;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Éamonn McManus
 */
public class Puzzle7 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle7.class.getResourceAsStream("puzzle7.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<Hand> hands = lines.stream()
          .map(Puzzle7::parseHand)
          .sorted()
          .toList();
      long sum = 0;
      for (int i = 0; i < hands.size(); i++) {
        sum += (i + 1) * hands.get(i).bid;
      }
      System.out.println(STR."Sum: \{sum}");
      List<Hand> jokerHands = hands.stream().sorted(JOKER_HAND_COMPARATOR).toList();
      long jokerSum = 0;
      for (int i = 0; i < jokerHands.size(); i++) {
        jokerSum += (i + 1) * jokerHands.get(i).bid;
      }
      System.out.println(STR."Joker sum: \{jokerSum}");
    }
  }

  private static Hand parseHand(String line) {
    String[] words = line.split(" ");
    assert words.length == 2;
    assert words[0].length() == 5;
    int bid = Integer.parseInt(words[1]);
    List<Card> cards = words[0].chars().mapToObj(c -> Card.of((char) c)).toList();
    return new Hand(cards, bid);
  }

  static class Hand implements Comparable<Hand> {
    final List<Card> cards;
    final int bid;
    final Supplier<Score> score = Suppliers.memoize(this::computeScore);
    final Supplier<Score> jokerScore = Suppliers.memoize(this::jokerScore);

    Hand(List<Card> cards, int bid) {
      this.cards = cards;
      this.bid = bid;
    }

    @Override
    public int compareTo(Hand that) {
      int cmp = this.score.get().compareTo(that.score.get());
      if (cmp != 0) {
        return cmp;
      }
      for (int i = 0; i < cards.size(); i++) {
        cmp = this.cards.get(i).compareTo(that.cards.get(i));
        if (cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }

    private Score computeScore() {
      Multiset<Card> cardSet = TreeMultiset.create(cards);
      return scoreFor(cardSet);
    }

    private static Score scoreFor(Multiset<Card> cardSet) {
      List<Integer> counts = cardSet.entrySet().stream().map(Multiset.Entry::getCount).toList();
      if (counts.contains(5)) {
        return Score.FIVE_OF_A_KIND;
      } else if (counts.contains(4)) {
        return Score.FOUR_OF_A_KIND;
      } else if (counts.contains(2) && counts.contains(3)) {
        return Score.FULL_HOUSE;
      } else if (counts.contains(3)) {
        return Score.THREE_OF_A_KIND;
      } else if (counts.contains(2) && counts.indexOf(2) != counts.lastIndexOf(2)) {
        return Score.TWO_PAIR;
      } else if (counts.contains(2)) {
        return Score.PAIR;
      } else {
        return Score.HIGH_CARD;
      }
    }

    Score jokerScore() {
      if (!cards.contains(Card.JACK)) {
        return computeScore();
      }
      Multiset<Card> cardSet = TreeMultiset.create(cards);
      int jokers = cardSet.count(Card.JACK);
      cardSet.remove(Card.JACK, jokers);
      if (cardSet.isEmpty()) {
        return Score.FIVE_OF_A_KIND;
      }
      // Enumerating the cases is just a bit too tedious. It's always most beneficial to add the
      // jokers to the most-frequent card. So just do that and see what the result is.
      Multiset.Entry<Card> biggest = cardSet.entrySet().stream()
          .max(Comparator.comparing(Multiset.Entry::getCount))
          .get();
      cardSet.add(biggest.getElement(), jokers);
      return scoreFor(cardSet);
    }

    @Override
    public String toString() {
      return cards.stream().map(Object::toString).collect(joining(""));
    }
  }

  enum Score {
    HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, FULL_HOUSE, FOUR_OF_A_KIND, FIVE_OF_A_KIND
  }

  private static final Comparator<Card> JOKER_COMPARATOR =
      (a, b) -> {
        int aOrd = a == Card.JACK ? -1 : a.ordinal();
        int bOrd = b == Card.JACK ? -1 : b.ordinal();
        return Integer.compare(aOrd, bOrd);
      };

  private static final Comparator<Hand> JOKER_HAND_COMPARATOR =
      (a, b) -> {
        int cmp = a.jokerScore.get().compareTo(b.jokerScore.get());
        if (cmp != 0) {
          return cmp;
        }
        for (int i = 0; i < a.cards.size(); i++) {
          cmp = JOKER_COMPARATOR.compare(a.cards.get(i), b.cards.get(i));
          if (cmp != 0) {
            return cmp;
          }
        }
        return cmp;
      };

  enum Card {
    TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"),
    TEN("T"), JACK("J"), QUEEN("Q"), KING("K"), ACE("A");

    private final String name;

    Card(String name) {
      this.name = name;
    }

    static Card of(char c) {
      return CHAR_TO_CARD.get(c);
    }

    @Override
    public String toString() {
      return name;
    }

    private static final ImmutableMap<Character, Card> CHAR_TO_CARD =
        Arrays.stream(values()).collect(toImmutableMap(card -> card.name.charAt(0), card -> card));
  }
}
