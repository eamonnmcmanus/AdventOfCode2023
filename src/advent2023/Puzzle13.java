package advent2023;

import static java.lang.Integer.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle13 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle13.class.getResourceAsStream("puzzle13.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<List<String>> maps = new ArrayList<>();
      List<String> soFar = new ArrayList<>();
      for (String line : lines) {
        if (line.isEmpty()) {
          maps.add(soFar);
          soFar = new ArrayList<>();
        } else {
          soFar.add(line);
        }
      }
      maps.add(soFar);
      long total = 0;
      for (List<String> map : maps) {
        total += score(map, 0);
      }
      System.out.println(STR."Total \{total}");
      long smudgeTotal = 0;
      for (List<String> map : maps) {
        smudgeTotal += smudgeScore(map);
      }
      System.out.println(STR."Smudge total \{smudgeTotal}");
    }
  }

  private static long smudgeScore(List<String> map) {
    long originalScore = score(map, 0);
    for (int i = 0; i < map.size(); i++) {
      for (int j = 0; j < map.get(i).length(); j++) {
        long smudgeScore = score(smudge(map, i, j), originalScore);
        if (smudgeScore != 0) {
          return smudgeScore;
        }
      }
    }
    map.forEach(System.err::println);
    throw new AssertionError(map);
  }

  private static List<String> smudge(List<String> map, int i, int j) {
    List<String> copy = new ArrayList<>(map);
    String line = copy.get(i);
    copy.set(i, line.substring(0, j) + smudge(line.charAt(j)) + line.substring(j + 1));
    return copy;
  }

  private static char smudge(char c) {
    return switch (c) {
      case '.' -> '#';
      case '#' -> '.';
      default -> throw new AssertionError(c);
    };
  }

  private static long score(List<String> map, long exclude) {
    for (int i = 1; i < map.size(); i++) {
      if (horizontalReflection(map, i)) {
        long score = i * 100;
        if (score != exclude) {
          return score;
        }
      }
    }
    char[][] inversion = new char[map.get(0).length()][map.size()];
    for (int i = 0; i < map.size(); i++) {
      for (int j = 0; j < map.get(i).length(); j++) {
        inversion[j][i] = map.get(i).charAt(j);
      }
    }
    List<String> invertedMap = stream(inversion).map(String::new).toList();
    for (int i = 1; i < invertedMap.size(); i++) {
      if (horizontalReflection(invertedMap, i)) {
        if (i != exclude) {
          return i;
        }
      }
    }
    return 0;
  }

  private static boolean horizontalReflection(List<String> map, int i) {
    List<String> above = map.subList(0, i).reversed();
    List<String> below = map.subList(i, map.size());
    int min = min(above.size(), below.size());
    return above.subList(0, min).equals(below.subList(0, min));
  }
}
