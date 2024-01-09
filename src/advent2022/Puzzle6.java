package advent2022;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle6 {
  private static final String SAMPLE =
      """
      mjqjpqmgbljsphdztnvjfqwrcgsmlb
      bvwbjplbgvbhsrlpgdmjqwftvncz
      nppdvjthqldpwncqszvftbrmjlhg
      nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg
      zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle6.class.getResourceAsStream("puzzle6.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Integer> packetStarts = lines.stream().map(line -> findStart(line, 4)).toList();
        System.out.println(STR."Packet starts for \{name}: \{packetStarts}");
        List<Integer> messageStarts = lines.stream().map(line -> findStart(line, 14)).toList();
        System.out.println(STR."Message starts for \{name}: \{messageStarts}");
      }
    }
  }

  private static int findStart(String s, int len) {
    Deque<Character> recent = new ArrayDeque<>();
    for (int i = 0; i < s.length(); i++) {
      if (recent.size() == len) {
        recent.removeFirst();
      }
      recent.addLast(s.charAt(i));
      if (new HashSet<>(recent).size() == len) {
        return i + 1;
      }
    }
    return -1;
  }
}
