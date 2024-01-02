package advent2023;

import static java.lang.Integer.max;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Éamonn McManus
 */
public class Puzzle23 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle23.class.getResourceAsStream("puzzle23.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      char[][] cells = new char[lines.size()][];
      for (int i = 0; i < lines.size(); i++) {
        cells[i] = lines.get(i).toCharArray();
      }
      new Puzzle23(cells, false).solve();
      new Puzzle23(cells, true).solve();
    }
  }

  /*
   * The main difficulty here was just parsing the text input into the correct graph. Once that was
   * done it was pretty trivial to find the longest path through the graph; brute force sufficed,
   * giving an immediate solution for Part 1 and taking about 30 seconds for Part 2.
   * Part 2 basically just changed the graph from directed to undirected so coding it was almost trivial.
   * (Almost, because I also had to add a `visited` set that was not needed with the DAG from Part 1.)
   */

  private final char[][] cells;
  private final MutableValueGraph<Node, Integer> graph;
  private final Node startNode;
  private final Node endNode;

  Puzzle23(char[][] cells, boolean part2) {
    this.cells = cells;

    String topLine = new String(cells[0]);
    int topJ = topLine.indexOf('.');
    assert topJ >= 0 && topJ == topLine.lastIndexOf('.');
    this.startNode = new Node(1, topJ);
    String botLine = new String(cells[cells.length - 1]);
    int botJ = botLine.indexOf('.');
    assert botJ >= 0 && botJ == botLine.lastIndexOf('.');
    this.endNode = new Node(cells.length - 1, botJ);

    this.graph =
        (part2 ? ValueGraphBuilder.<Node, Integer>undirected() : ValueGraphBuilder.<Node, Integer>directed())
        .build();
    this.graph.addNode(startNode);
    this.graph.addNode(endNode);
  }

  void solve() {
    buildGraph(startNode, new HashSet<>(List.of(startNode, endNode)));
    List<List<Node>> allPaths = allPaths(List.of(startNode));
    System.out.println(STR."Found \{allPaths.size()} paths");
    int longest = Integer.MIN_VALUE;
    for (List<Node> path : allPaths) {
      int len = pathLength(path);
      longest = max(longest, len);
    }
    System.out.println(STR."Longest is \{longest}");
  }

  String pathToString(List<Node> path) {
    StringBuilder sb = new StringBuilder().append(path.getFirst());
    for (int i = 1; i < path.size(); i++) {
      Node prev = path.get(i - 1);
      Node cur = path.get(i);
      int len = graph.edgeValue(prev, cur).get();
      sb.append(STR." -> \{cur}[\{len}]");
    }
    return sb.toString();
  }

  int pathLength(List<Node> path) {
    int len = 0;
    for (int i = 1; i < path.size(); i++) {
      len += graph.edgeValue(path.get(i - 1), path.get(i)).get();
    }
    return len;
  }

  List<List<Node>> allPaths(List<Node> incoming) {
    Node last = incoming.getLast();
    if (last.equals(endNode)) {
      return List.of(incoming);
    }
    List<List<Node>> paths = new ArrayList<>();
    for (Node next : graph.successors(last)) {
      if (!incoming.contains(next)) {
        List<Node> newPath = ImmutableList.<Node>builder().addAll(incoming).add(next).build();
        paths.addAll(allPaths(newPath));
      }
    }
    return paths;
  }


  /*
     It looks as if the path are designed to be directed graphs: there are no forks where you can
     go forward or backward, so the <>^v mark the ends of edges. Therefore each <>^v can be an edge,
     and the cell it points can be a node. There can be more than one edge leading to a given node.
     A fork is always a . that is itself a node, that therefore has more than one edge leading out.
     So the recursive plan is that we have a graph-so-far and a . cell, which is either the start of
     a path or a fork. If a path, we follow it to the end and recur with the cell after the <>^v.
     If a fork, we recur with each end of the fork.
  */

  void buildGraph(Node start, Set<Node> seen) {
    List<Move> pathMoves = new ArrayList<>();
    List<Node> forkNodes = new ArrayList<>();
    for (Move move : Move.validMoves(start, cells)) {
      // We're assuming that the end node is at the end of a path, so we won't encounter it here.
      Move moved = move.move();
      char c = cells[moved.i][moved.j];
      if (c == '.') {
        pathMoves.add(move);
      } else {
        forkNodes.add(moved.move().toNode());
      }
    }
    assert pathMoves.isEmpty() != forkNodes.isEmpty();
    assert pathMoves.size() <= 1; // not guaranteed by the problem spec, but true in the examples

    for (Node forkNode : forkNodes) {
      Integer old = graph.putEdgeValue(start, forkNode, 2);
      assert old == null;
      if (seen.add(forkNode)) {
        buildGraph(forkNode, seen);
      }
    }

    for (Move pathMove : pathMoves) {
      int len = 3;
      Set<Node> pathNodes = new HashSet<>(Set.of(start, pathMove.move().toNode()));
      Move move = pathMove;
      while (true) {
        Node current = move.move().toNode();
        Node next = null;
        Move moveToNext = null;
        for (Move nextMove : Move.validMoves(current, cells)) {
          Node target = nextMove.move().toNode();
          if (pathNodes.add(target)) {
            assert next == null;
            next = target;
            moveToNext = nextMove;
          }
        }
        assert next != null;
        if (next.equals(endNode)) {
          Integer old = graph.putEdgeValue(start, next, len);
          assert old == null;
          break;
        }
        if (cells[next.i][next.j] != '.') {
          Node target = moveToNext.move().move().toNode();
          Integer old = graph.putEdgeValue(start, target, len);
          assert old == null;
          if (seen.add(target)) {
            buildGraph(target, seen);
          }
          break;
        }
        move = moveToNext;
        len++;
      }
    }

  }

  record Move(int i, int j, Dir dir) {
    Move move() {
      return new Move(i + dir.deltaI, j + dir.deltaJ, dir);
    }

    Node toNode() {
      return new Node(i, j);
    }

    boolean isValid(char[][] cells) {
      int newI = i + dir.deltaI;
      int newJ = j + dir.deltaJ;
      if (newI > 0 && newI < cells.length && newJ > 0 && newJ < cells[0].length) {
        char c = cells[newI][newJ];
        return c == '.' || c == dir.arrow;
      }
      return false;
    }

    static List<Move> validMoves(Node start, char[][] cells) {
      return Dir.VALUES.stream()
          .map(dir -> new Move(start.i, start.j, dir))
          .filter(move -> move.isValid(cells))
          .toList();
    }
  }

  record Node(int i, int j) {
    @Override public String toString() {
      return STR."(\{i},\{j})";
    }
  }

  static final String ARROWS = "<>^v";

  enum Dir {
    LEFT(0, -1, '<'), RIGHT(0, +1, '>'), UP(-1, 0, '^'), DOWN(+1, 0, 'v');

    final int deltaI;
    final int deltaJ;
    final char arrow;

    Dir(int deltaI, int deltaJ, char arrow) {
      this.deltaI = deltaI;
      this.deltaJ = deltaJ;
      this.arrow = arrow;
    }

    static final Set<Dir> VALUES = EnumSet.allOf(Dir.class);
  }
}
