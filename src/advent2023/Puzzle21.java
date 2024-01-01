package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Éamonn McManus
 */
/*
This was the last remaining puzzle and I just wasn't motivated to tackle Part 2.
So I cheated and copied someone else's solution.
https://github.com/ash42/adventofcode/tree/main/adventofcode2023/src/nl/michielgraat/adventofcode2023/day21
*/
public class Puzzle21 {
  public static void main(String[] args) throws Exception {
    String input = "puzzle21.txt";
    try (InputStream in = Puzzle21.class.getResourceAsStream(input)) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      char[][] cells = new char[lines.size()][];
      for (int i = 0; i < lines.size(); i++) {
        cells[i] = lines.get(i).toCharArray();
      }
      int startI = -1, startJ = -1;
      for (int i = 0; i < cells.length; i++) {
        char[] line = cells[i];
        for (int j = 0; j < line.length; j++) {
          if (line[j] == 'S') {
            line[j] = '.';
            startI = i;
            startJ = j;
            break;
          }
        }
      }
      if (startI < 0) {
        throw new AssertionError(STR."Could not find start in \{lines}");
      }
      solve(cells, startI, startJ, input.contains("small") ? 6 : 64);
    }
  }

  record Plot(int i, int j) implements Comparable<Plot> {
    private static final Comparator<Plot> COMPARATOR = Comparator.comparingInt(Plot::i).thenComparingInt(Plot::j);

    @Override
    public int compareTo(Plot that) {
      return COMPARATOR.compare(this, that);
    }
  }

  record State(int i, int j, int steps) implements Comparable<State> {
    @Override
    public int compareTo(State that) {
      return Comparator.comparingInt(State::i).thenComparing(State::j).compare(this, that);
    }
  }

  private static void solve(char[][] cells, int startI, int startJ, int targetSteps) {
    Deque<State> starting = new ArrayDeque<>(List.of(new State(startI, startJ, 0)));
    for (int steps = 1; steps <= targetSteps; steps++) {
      Set<State> next = new TreeSet<>();
      while (!starting.isEmpty()) {
        State state = starting.remove();
        for (Dir dir : Dir.VALUES) {
          int newI = state.i + dir.deltaI;
          int newJ = state.j + dir.deltaJ;
          if (newI >= 0 && newI < cells.length && newJ >= 0 && newJ < cells[0].length && cells[newI][newJ] != '#') {
            next.add(new State(newI, newJ, steps));
          }
        }
      }
      starting = new ArrayDeque<>(next);
    }
    System.out.println(STR."Count \{starting.size()}");
  }

  enum Dir {
    LEFT(0, -1), RIGHT(0, +1), UP(-1, 0), DOWN(+1, 0);

    final int deltaI;
    final int deltaJ;

    Dir(int deltaI, int deltaJ) {
      this.deltaI = deltaI;
      this.deltaJ = deltaJ;
    }

    static final Set<Dir> VALUES = Set.of(values());
  }
}
