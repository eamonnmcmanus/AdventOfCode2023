package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle4 {
  private static final Pattern CARD_PATTERN = Pattern.compile("Card\\s+[0-9]+: (.*)\\|(.*)");

  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle4.class.getResourceAsStream("puzzle4.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      int sum = 0;
      List<Card> cards = new ArrayList<Card>();
      for (String line : lines) {
        Matcher matcher = CARD_PATTERN.matcher(line);
        if (!matcher.matches()) {
          throw new AssertionError(line);
        }
        Set<Integer> winning = parseNumbers(matcher.group(1));
        Set<Integer> present = parseNumbers(matcher.group(2));
        Card card = new Card(winning, present);
        int count = card.score();
        if (count > 0) {
          sum += 1 << (count - 1);
        }
        cards.add(card);
      }
      System.out.println(STR."Sum is \{sum}");
      int[] counts = new int[cards.size()];
      Arrays.fill(counts, 1);
      for (int i = 0; i < cards.size(); i++) {
        int score = cards.get(i).score();
        System.out.println(STR."Score for card \{i + 1} is \{score}");
        for (int j = 1; j <= score; j++) {
          System.out.println(STR."  Update count for \{i + j + 1} from \{counts[i + j]} to \{2 * counts[i + j]}");
          counts[i + j] += counts[i];
        }
      }
      int countSum = Arrays.stream(counts).sum();
      System.out.println(STR."Count sum is \{countSum}");
    }
  }

  record Card(Set<Integer> winning, Set<Integer> present) {
    int score() {
      int count = 0;
      for (Integer number : present) {
        if (winning.contains(number)) {
          count++;
        }
      }
      return count;
    }
  }

  private static Set<Integer> parseNumbers(String line) {
    return Arrays.stream(line.split(" +")).filter(s -> !s.isEmpty()).map(Integer::parseInt).collect(toSet());
  }
}
