package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle6 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle6.class.getResourceAsStream("puzzle6.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = Arrays.asList(lineString.split("\n"));
      List<String> timeStrings = Arrays.asList(lines.get(0).split("\\s+"));
      assert timeStrings.get(0).equals("Time:");
      List<Integer> times = timeStrings.stream().skip(1).map(Integer::parseInt).toList();
      List<String> distStrings = Arrays.asList(lines.get(1).split("\\s+"));
      assert distStrings.get(0).equals("Distance:");
      List<Integer> dists = distStrings.stream().skip(1).map(Integer::parseInt).toList();
      assert times.size() == dists.size();
      int product = 1;
      for (int i = 0; i < times.size(); i++) {
        int time = times.get(i);
        int dist = dists.get(i);
        int count = 0;
        for (int j = 1; j < time; j++) {
          if (j * (time - j) > dist) {
            count++;
          }
        }
        assert count > 0;
        product *= count;
      }
      System.out.println(STR."Product \{product}");

      String timeString = timeStrings.stream().skip(1).collect(joining(""));
      int time = Integer.parseInt(timeString);
      String distString = distStrings.stream().skip(1).collect(joining(""));
      long dist = Long.parseLong(distString);
      System.out.println(STR."Time \{time} dist \{dist}");
      int count = 0;
      for (long j = 1; j < time; j++) {
        if (j * (time - j) > dist) {
          count++;
        }
      }
      assert count > 0;
      System.out.println(STR."New count \{count}");
    }
  }
}
