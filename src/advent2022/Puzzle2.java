package advent2022;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle2 {
  private static final String SAMPLE = """
      A Y
      B X
      C Z
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle2.class.getResourceAsStream("puzzle2.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);

        // Part 1
        long sumPart1 = 0;
        for (String line : lines) {
          List<String> codes = Splitter.on(' ').splitToList(line);
          assert codes.size() == 2 : line;
          Item them = ABC_MAP.get(codes.get(0));
          Item us = XYZ_MAP.get(codes.get(1));
          sumPart1 += us.score + game(us, them).score;
        }
        System.out.println(STR."Part 1 sum for \{name} is \{sumPart1}");

        // Part 2
        long sumPart2 = 0;
        for (String line : lines) {
          List<String> codes = Splitter.on(' ').splitToList(line);
          assert codes.size() == 2 : line;
          Item them = ABC_MAP.get(codes.get(0));
          Result result = XYZ_RESULT_MAP.get(codes.get(1));
          Item us = play(result, them);
          sumPart2 += us.score + result.score;
        }
        System.out.println(STR."Part 2 sum for \{name} is \{sumPart2}");
      }
    }
  }

  private static final ImmutableMap<String, Item> ABC_MAP =
      ImmutableMap.of("A", Item.ROCK, "B", Item.PAPER, "C", Item.SCISSORS);
  private static final ImmutableMap<String, Item> XYZ_MAP =
      ImmutableMap.of("X", Item.ROCK, "Y", Item.PAPER, "Z", Item.SCISSORS);
  private static final ImmutableMap<String, Result> XYZ_RESULT_MAP =
      ImmutableMap.of("X", Result.LOSE, "Y", Result.DRAW, "Z", Result.WIN);

  private static Result game(Item us, Item them) {
    return switch (us) {
      case ROCK -> switch (them) {
        case ROCK -> Result.DRAW;
        case PAPER -> Result.LOSE;
        case SCISSORS -> Result.WIN;
      };
      case PAPER -> switch (them) {
        case ROCK -> Result.WIN;
        case PAPER -> Result.DRAW;
        case SCISSORS -> Result.LOSE;
      };
      case SCISSORS -> switch (them) {
        case ROCK -> Result.LOSE;
        case PAPER -> Result.WIN;
        case SCISSORS -> Result.DRAW;
      };
    };
  }

  private static Item play(Result wanted, Item them) {
    // If they play `them`, what should we play to get the result `wanted`?
    return switch (them) {
      case ROCK -> switch (wanted) {
        case WIN -> Item.PAPER;
        case DRAW -> Item.ROCK;
        case LOSE -> Item.SCISSORS;
      };
      case PAPER -> switch (wanted) {
        case WIN -> Item.SCISSORS;
        case DRAW -> Item.PAPER;
        case LOSE -> Item.ROCK;
      };
      case SCISSORS -> switch (wanted) {
        case WIN -> Item.ROCK;
        case DRAW -> Item.SCISSORS;
        case LOSE -> Item.PAPER;
      };
    };
  }

  enum Item {
    ROCK(1), PAPER(2), SCISSORS(3);

    final int score;

    private Item(int score) {
      this.score = score;
    }
  }

  enum Result {
    LOSE(0), DRAW(3), WIN(6);

    final int score;

    private Result(int score) {
      this.score = score;
    }
  }
}
