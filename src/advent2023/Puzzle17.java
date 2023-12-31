package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * @author Éamonn McManus
 */
public class Puzzle17 {
  /*
  Dijkstra's Algorithm. We associate a distance estimate d with each node. At any point we have a
  set S of nodes for which we know that d is in fact the minimum distance to the node. At each step
  we add a node n to S for which we have determined that its d is in fact the minimum distance, and
  we update its non-S neighbours so their d is the d from n, if that is less than the previous d
  value. Then we pick a new n as the non-S node with the smallest d value. We know that that d is
  the minimum value from any neighbour in S. If there were a path arriving from a neighbour not in S
  then some point on that path must have a neighbour in S, with a d that is greater than n's. Fuh.
  */
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle17.class.getResourceAsStream("puzzle17-small.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      Cell[][] cells = parseCellLines(lines);
      var puzzle = new Puzzle17(cells, cells.length, cells[0].length);
      puzzle.solve();
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

  void solve() {
    PriorityQueue<Cell> queue = new PriorityQueue<>(Comparator.comparing((Cell c) -> c.d));
    for (int i = 0; i < maxI; i++) {
      for (int j = 0; j < maxJ; j++) {
        Cell cell = cells[i][j];
        cell.d = (i == 0 && j == 0) ? 0 : Long.MAX_VALUE;
        queue.add(cell);
      }
    }
    while (true) {
      Cell cell = queue.remove();
      if (cell.i == maxI - 1 && cell.j == maxJ - 1) {
        break;
      }
      forEachNeighbour(
          cell,
          n -> {
            if (canAdd(cell, n)) {
              long newD = cell.d + n.cost;
              if (newD < n.d) {
                queue.remove(n);
                n.d = newD;
                n.parent = cell;
                queue.add(n);
              }
            }
          }
      );
    }
    Cell goal = cells[maxI - 1][maxJ - 1];
    System.out.println(STR."Purported solution \{goal.d}");
    char[][] matrix = new char[maxI][maxJ];
    for (char[] line : matrix) {
      Arrays.fill(line, '.');
    }
    for (Cell cell = goal; cell != null; cell = cell.parent) {
      matrix[cell.i][cell.j] = '#';
    }
    for (char[] line : matrix) {
      System.out.println(new String(line));
    }
    long total = 0;
    for (Cell cell = goal; cell.i != 0 || cell.j != 0; cell = cell.parent) {
      total += cell.cost;
    }
    System.out.println(STR."Computed cost \{total}");
  }

  boolean canAdd(Cell from, Cell to) {
    return switch (from.consecutive()) {
      case I -> from.i != to.i;
      case J -> from.j != to.j;
      case NONE -> true;
    };
  }

  static class Cell {
    final int i;
    final int j;
    final int cost;
    long d;
    Cell parent;

    Cell(int i, int j, int cost) {
      this.i = i;
      this.j = j;
      this.cost = cost;
    }

    enum Consecutive {I, J, NONE}

    Consecutive consecutive() {
      boolean maybeI = true;
      boolean maybeJ = true;
      int count = 0;
      for (Cell p = parent; p != null && ++count < 4; p = p.parent) {
        maybeI &= p.i == i;
        maybeJ &= p.j == j;
      }
      if (count == 4) {
        if (maybeI) {
          return Consecutive.I;
        } else if (maybeJ) {
          return Consecutive.J;
        }
      }
      return Consecutive.NONE;
      /*
      if (parent != null && parent.parent != null && parent.parent.parent != null) {
        if (i == parent.i && i == parent.parent.i && i == parent.parent.parent.i) {
          return Consecutive.I;
        } else if (j == parent.j && j == parent.parent.j && j == parent.parent.parent.j) {
          return Consecutive.J;
        }
      }
      return Consecutive.NONE;
      */
    }
  }

  void forEachNeighbour(Cell cell, Consumer<Cell> task) {
    if (cell.i > 0) {
      task.accept(cells[cell.i - 1][cell.j]);
    }
    if (cell.i + 1 < maxI) {
      task.accept(cells[cell.i + 1][cell.j]);
    }
    if (cell.j > 0) {
      task.accept(cells[cell.i][cell.j - 1]);
    }
    if (cell.j + 1 < maxJ) {
      task.accept(cells[cell.i][cell.j + 1]);
    }
  }

  enum Dir {LEFT, RIGHT, UP, DOWN}

  private static Cell[][] parseCellLines(List<String> lines) {
    Cell[][] cells = new Cell[lines.size()][lines.get(0).length()];
    for (int i = 0; i < lines.size(); i++) {
      String input = lines.get(i);
      Cell[] output = cells[i];
      for (int j = 0; j < input.length(); j++) {
        output[j] = new Cell(i, j, input.charAt(j) - '0');
      }
    }
    return cells;
  }
}
