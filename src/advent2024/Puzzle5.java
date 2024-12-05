package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle5 {
  private static final String SAMPLE =
      """
      47|53
      97|13
      97|61
      97|47
      75|29
      61|13
      75|53
      29|13
      97|29
      53|29
      61|53
      97|53
      61|29
      47|13
      75|47
      97|75
      47|61
      75|61
      47|29
      75|13
      53|13

      75,47,61,53,29
      97,61,53,29,13
      75,29,13
      75,97,47,61,53
      61,13,29
      97,13,75,29,47
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle5.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int blankIndex = lines.indexOf("");
        checkArgument(blankIndex > 0, "No blank line in input");
        List<String> orderPairs = lines.subList(0, blankIndex);
        Set<Precedes> precedes = parsePrecedes(orderPairs);
        List<String> inputs = lines.subList(blankIndex + 1, lines.size());
        long orderedTotal = 0;
        long correctedTotal = 0;
        for (String input : inputs) {
          List<String> pages = Splitter.on(',').splitToList(input);
          checkArgument(
              pages.size() % 2 == 1, "Even number of pages %d in %s", pages.size(), input);
          List<String> ordered = ordered(pages, precedes);
          int middle = Integer.parseInt(ordered.get(ordered.size() / 2));
          if (ordered.equals(pages)) {
            orderedTotal += middle;
          } else {
            correctedTotal += middle;
          }
        }
        System.out.println("Part 1 total for " + name + " is " + orderedTotal);
        System.out.println("Part 2 total for " + name + " is " + correctedTotal);
      }
    }
  }

  // This uses String rather than int even though they are page numbers. The numeric value of the
  // pages only matters when computing the totals, and I wanted to avoid possible confusion with
  // genuine ints such as list indices.
  private record Precedes(String left, String right) {}

  private static Set<Precedes> parsePrecedes(List<String> orderPairs) {
    Set<Precedes> precedes = new LinkedHashSet<>();
    for (String orderPair : orderPairs) {
      List<String> parts = Splitter.on('|').splitToList(orderPair);
      checkArgument(parts.size() == 2, "Line %s produces part count %d", orderPair, parts.size());
      String left = parts.get(0);
      String right = parts.get(1);
      Precedes order = new Precedes(left, right);
      boolean added = precedes.add(order);
      checkState(added, "Order list includes redundant %s", order);
      Precedes opposite = new Precedes(right, left);
      checkState(
          !precedes.contains(opposite), "Order list contains both %s and %s", order, opposite);
    }
    return precedes;
  }

  private static List<String> ordered(List<String> pages, Set<Precedes> precedes) {
    Comparator<String> comparator =
        (a, b) -> {
          if (precedes.contains(new Precedes(a, b))) {
            return -1;
          } else if (precedes.contains(new Precedes(b, a))) {
            return +1;
          } else {
            throw new IllegalStateException("No order defined between " + a + " and " + b);
          }
        };
    return pages.stream().sorted(comparator).toList();
  }

  // I wasted a huge amount of time here by not reading the problem statement carefully enough.
  // I read in all of the page numbers in all of the lists and sorted them all according to the
  // precedence rules. But that doesn't work: the problem says that only the rules that mention
  // the pages in a given list matter. If you try to use them all then you discover (eventually)
  // that they violate the transitive property, so you can't use them to sort all the page numbers
  // globally. You have to sort each list individually, which anyway leads to simpler code.
}