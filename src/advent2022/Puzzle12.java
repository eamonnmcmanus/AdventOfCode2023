package advent2022;

import static java.lang.Integer.min;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle12 {
  private static final String SAMPLE =
      """
      Sabqponm
      abcryxxl
      accszExk
      acctuvwj
      abdefghi
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle12.class.getResourceAsStream("puzzle12.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        HeightMap map = parseMap(lines);

        // Part 1
        ImmutableList<Node> shortestPath = shortestPath(map.graph, map.start, map.end);
        System.out.println(
            "For " + name + ", shortest path has length " + (shortestPath.size() - 1));

        // Part 2
        int shortest = Integer.MAX_VALUE;
        List<Node> starts = map.graph.nodes().stream().filter(n -> n.height == 'a').toList();
        for (Node start : starts) {
          int length = shortestPath(map.graph, start, map.end).size() - 1;
          if (length > 0) {
            shortest = min(shortest, length);
          }
        }
        System.out.println("For " + name + ", shortest path from any \"a\" is " + shortest);
      }
    }
  }

  private static ImmutableList<Node> shortestPath(Graph<Node> graph, Node start, Node end) {
    // https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
    Map<Node, Integer> distance = new HashMap<>();
    for (Node node : graph.nodes()) {
      distance.put(node, Integer.MAX_VALUE);
    }
    distance.put(start, 0);
    PriorityQueue<Node> unvisited =
        new PriorityQueue<>(Comparator.comparing((Node node) -> distance.get(node)));
    unvisited.addAll(graph.nodes());
    while (true) {
      Node current = unvisited.remove();
      if (current == end) {
        break;
      }
      if (distance.get(current) == Integer.MAX_VALUE) {
        return ImmutableList.of(); // no path from start to end
      }
      int currentDistance = distance.get(current);
      for (Node neighbour : graph.successors(current)) {
        if (distance.get(neighbour) > currentDistance + 1) {
          unvisited.remove(neighbour);
          distance.put(neighbour, currentDistance + 1);
          unvisited.add(neighbour);
        }
      }
    }
    List<Node> path = new ArrayList<>(List.of(end));
    while (true) {
      Node current = path.getLast();
      if (current == start) {
        break;
      }
      Node next =
          graph.predecessors(current).stream()
              .filter(n -> distance.get(n) == distance.get(current) - 1)
              .findFirst()
              .get();
      path.add(next);
    }
    return ImmutableList.copyOf(path.reversed());
  }

  private static HeightMap parseMap(List<String> lines) {
    int height = lines.size();
    int width = lines.get(0).length();
    Node[][] nodes = new Node[height][width];
    Node start = null;
    Node end = null;
    for (int i = 0; i < height; i++) {
      String line = lines.get(i);
      for (int j = 0; j < width; j++) {
        char c = line.charAt(j);
        Node node;
        switch (c) {
          case 'S' -> {
            node = new Node(i, j, 1000);
            start = node;
          }
          case 'E' -> {
            node = new Node(i, j, 'z');
            end = node;
          }
          default -> {
            node = new Node(i, j, c);
          }
        }
        nodes[i][j] = node;
      }
    }
    assert start != null && end != null;
    ImmutableGraph.Builder<Node> builder = GraphBuilder.directed().immutable();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        Node node = nodes[i][j];
        builder.addNode(node);
        List<Node> neighbours = new ArrayList<>();
        if (i > 0) {
          neighbours.add(nodes[i - 1][j]);
        }
        if (i + 1 < height) {
          neighbours.add(nodes[i + 1][j]);
        }
        if (j > 0) {
          neighbours.add(nodes[i][j - 1]);
        }
        if (j + 1 < width) {
          neighbours.add(nodes[i][j + 1]);
        }
        neighbours.stream()
            .filter(n -> n.height - node.height <= 1)
            .forEach(n -> builder.putEdge(node, n));
      }
    }
    return new HeightMap(builder.build(), start, end);
  }

  record Node(int i, int j, int height) {
    @Override
    public String toString() {
      char c = (height == 1000) ? 'S' : (char) height;
      return "(" + i + "," + j + ")" + c;
    }
  }

  record HeightMap(ImmutableGraph<Node> graph, Node start, Node end) {}
}
