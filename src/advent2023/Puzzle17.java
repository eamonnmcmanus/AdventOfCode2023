package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/*
 * I gave up after doing this wrong and running out of energy. The solution here is based on
 * https://advent-of-code.xavd.id/writeups/2023/day/17/
 */
public class Puzzle17 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle17.class.getResourceAsStream("puzzle17.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      Cell[][] cells = parseCellLines(lines);
      var puzzle = new Puzzle17(cells, cells.length, cells[0].length);
      puzzle.solve(0, 3);
      puzzle.solve(4, 10);
    }
  }

  private final Cell[][] cells;
  private final int maxI;
  private final int maxJ;

  Puzzle17(Cell[][] cells, int maxI, int maxJ) {
    this.cells = cells;
    this.maxI = maxI;
    this.maxJ = maxJ;
  }

  void solve(int minSteps, int maxSteps) {
    PriorityQueue<State> queue = new PriorityQueue<>();
    queue.add(new State(0, new Position(0, 0, Dir.RIGHT), 0));
    queue.add(new State(0, new Position(0, 0, Dir.DOWN), 0));
    Set<PositionAndSteps> seen = new HashSet<>();
    while (true) {
      State state = queue.remove();
      Position pos = state.position;
      if (pos.i == maxI - 1 && pos.j == maxJ - 1 && state.steps >= minSteps) {
        System.out.println(STR."Solution is \{state.cost}");
        break;
      }
      PositionAndSteps pas = new PositionAndSteps(pos, state.steps);
      if (!seen.add(pas)) {
        continue;
      }
      if (state.steps >= minSteps) {
        Position left = pos.turnLeftAndAdvance();
        if (validPosition(left)) {
          queue.add(new State(state.cost + cells[left.i][left.j].cost, left, 1));
        }
        Position right = pos.turnRightAndAdvance();
        if (validPosition(right)) {
          queue.add(new State(state.cost + cells[right.i][right.j].cost, right, 1));
        }
      }
      if (state.steps < maxSteps) {
        Position forward = pos.advance();
        if (validPosition(forward)) {
          queue.add(new State(state.cost + cells[forward.i][forward.j].cost, forward, state.steps + 1));
        }
      }
    }
  }

  boolean validPosition(Position pos) {
    return pos.i >= 0 && pos.i < maxI && pos.j >= 0 && pos.j < maxJ;
  }

  record Position(int i, int j, Dir dir) {
    Position advance() {
      return new Position(i + dir.deltaI, j + dir.deltaJ, dir);
    }

    Position turnLeftAndAdvance() {
      Dir newDir = dir.turnLeft();
      return new Position(i + newDir.deltaI, j + newDir.deltaJ, newDir);
    }

    Position turnRightAndAdvance() {
      Dir newDir = dir.turnRight();
      return new Position(i + newDir.deltaI, j + newDir.deltaJ, newDir);
    }
  }

  record State(long cost, Position position, int steps) implements Comparable<State> {
    private static final Comparator<State> COMPARATOR = Comparator.comparing(State::cost);

    @Override
    public int compareTo(State that) {
      return COMPARATOR.compare(this, that);
    }
  }

  record PositionAndSteps(Position position, int steps) {}

  enum Dir {
    LEFT(0, -1), RIGHT(0, +1), UP(-1, 0), DOWN(+1, 0);

    private int deltaI;
    private int deltaJ;

    Dir(int deltaI, int deltaJ) {
      this.deltaI = deltaI;
      this.deltaJ = deltaJ;
    }

    Dir turnLeft() {
      return switch (this) {
        case LEFT -> DOWN;
        case DOWN -> RIGHT;
        case RIGHT -> UP;
        case UP -> LEFT;
      };
    }

    Dir turnRight() {
      return switch (this) {
        case LEFT -> UP;
        case UP -> RIGHT;
        case RIGHT -> DOWN;
        case DOWN -> LEFT;
      };
    }
  }

  static class Cell {
    final long cost;

    Cell(long cost) {
      this.cost = cost;
    }
  }

  private static Cell[][] parseCellLines(List<String> lines) {
    Cell[][] cells = new Cell[lines.size()][lines.get(0).length()];
    for (int i = 0; i < lines.size(); i++) {
      String input = lines.get(i);
      Cell[] output = cells[i];
      for (int j = 0; j < input.length(); j++) {
        output[j] = new Cell(input.charAt(j) - '0');
      }
    }
    return cells;
  }
}
