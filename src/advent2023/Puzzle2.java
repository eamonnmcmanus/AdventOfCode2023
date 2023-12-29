package advent2023;

import static java.lang.Integer.max;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle2 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle2.class.getResourceAsStream("puzzle2.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<Game> games = lines.stream().map(Puzzle2::parseLine).toList();
      System.out.println(games);
      int allowed = games.stream().filter(Game::allowed).mapToInt(Game::number).sum();
      System.out.printf("passing sum: %d\n", allowed);
      int sum = games.stream().map(Game::maxDraw).mapToInt(Draw::power).sum();
      System.out.printf("power sum: %d\n", sum);
    }
  }

  private record Draw(int red, int green, int blue) {
    boolean allowed() {
      return red <= 12 && green <= 13 && blue <= 14;
    }

    int power() {
      return red * green * blue;
    }
  }

  private record Game(int number, List<Draw> draws) {
    boolean allowed() {
      return draws.stream().allMatch(Draw::allowed);
    }

    Draw maxDraw() {
      int red = 0, green = 0, blue = 0;
      for (Draw draw : draws) {
        red = max(red, draw.red());
        green = max(green, draw.green());
        blue = max(blue, draw.blue());
      }
      return new Draw(red, green, blue);
    }
  }

  private static final Pattern GAME = Pattern.compile("Game ([0-9]+): (.*)");

  private static Game parseLine(String line) {
    Matcher matcher = GAME.matcher(line);
    if (matcher.matches()) {
      return new Game(Integer.parseInt(matcher.group(1)), parseDraws(matcher.group(2)));
    } else {
      throw new IllegalArgumentException(line);
    }
  }

  private static List<Draw> parseDraws(String line) {
    List<String> drawStrings = List.of(line.split("; "));
    return drawStrings.stream().map(Puzzle2::parseDraw).toList();
  }

  private static Draw parseDraw(String drawString) {
    int red = 0, green = 0, blue = 0;
    for (String countString : drawString.split(", ")) {
      String[] countColour = countString.split(" ");
      int count = Integer.parseInt(countColour[0]);
      switch (countColour[1]) {
        case "red" -> red = count;
        case "green" -> green = count;
        case "blue" -> blue = count;
        default -> throw new IllegalArgumentException(countColour[1]);
      }
    }
    return new Draw(red, green, blue);
  }
}
