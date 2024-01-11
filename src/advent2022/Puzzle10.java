package advent2022;

import static java.lang.Math.abs;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author emcmanus@google.com (Éamonn McManus)
 */
public class Puzzle10 {
  private static final String SAMPLE =
      """
      addx 15
      addx -11
      addx 6
      addx -3
      addx 5
      addx -1
      addx -8
      addx 13
      addx 4
      noop
      addx -1
      addx 5
      addx -1
      addx 5
      addx -1
      addx 5
      addx -1
      addx 5
      addx -1
      addx -35
      addx 1
      addx 24
      addx -19
      addx 1
      addx 16
      addx -11
      noop
      noop
      addx 21
      addx -15
      noop
      noop
      addx -3
      addx 9
      addx 1
      addx -3
      addx 8
      addx 1
      addx 5
      noop
      noop
      noop
      noop
      noop
      addx -36
      noop
      addx 1
      addx 7
      noop
      noop
      noop
      addx 2
      addx 6
      noop
      noop
      noop
      noop
      noop
      addx 1
      noop
      noop
      addx 7
      addx 1
      noop
      addx -13
      addx 13
      addx 7
      noop
      addx 1
      addx -33
      noop
      noop
      noop
      addx 2
      noop
      noop
      noop
      addx 8
      noop
      addx -1
      addx 2
      addx 1
      noop
      addx 17
      addx -9
      addx 1
      addx 1
      addx -3
      addx 11
      noop
      noop
      addx 1
      noop
      addx 1
      noop
      noop
      addx -13
      addx -19
      addx 1
      addx 3
      addx 26
      addx -30
      addx 12
      addx -1
      addx 3
      addx 1
      noop
      noop
      noop
      addx -9
      addx 18
      addx 1
      addx 2
      noop
      noop
      addx 9
      noop
      noop
      noop
      addx -1
      addx 2
      addx -37
      addx 1
      addx 3
      noop
      addx 15
      addx -21
      addx 22
      addx -6
      addx 1
      noop
      addx 2
      addx 1
      noop
      addx -10
      noop
      noop
      addx 20
      addx 1
      addx 2
      addx 2
      addx -6
      addx -11
      noop
      noop
      noop
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle10.class.getResourceAsStream("puzzle10.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Instr> instrs = lines.stream().map(line -> parseInstr(line)).toList();
        long total = 0;
        long x = 1;
        int cycle = 1;
        char[][] grid = new char[6][40];
        for (char[] line : grid) {
          Arrays.fill(line, '.');
        }
        for (Instr instr : instrs) {
          if (cycle % 40 == 20) {
            total += x * cycle;
          }
          int px = (cycle - 1) % 40;
          int py = (cycle - 1) / 40;
          if (abs(x -px) <= 1) {
            grid[py][px] = '#';
          }
          cycle++;
          if (instr instanceof AddX(int deltaX)) {
            if (cycle % 40 == 20) {
              total += x * cycle;
            }
            px = (cycle - 1) % 40;
            py = (cycle - 1) / 40;
            if (abs(x -px) <= 1) {
              grid[py][px] = '#';
            }
            x += deltaX;
            cycle++;
          }
        }
        System.out.println(STR."For \{name}, total signal strength \{total}, final cycle \{cycle}");
        System.out.println(STR."For \{name}, grid:");
        for (char[] line : grid) {
          System.out.println(new String(line));
        }
      }
    }
  }

  private static Instr parseInstr(String line) {
    if (line.startsWith("addx ")) {
      return new AddX(Integer.parseInt(line.substring(5)));
    } else if (line.equals("noop")) {
      return new NoOp();
    } else {
      throw new AssertionError(line);
    }
  }

  sealed interface Instr {}

  record AddX(int n) implements Instr {}

  record NoOp() implements Instr {}
}
