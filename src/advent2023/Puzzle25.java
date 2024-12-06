package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Éamonn McManus
 */
public class Puzzle25 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle25.class.getResourceAsStream("puzzle25.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      Map<String, Node> nodes = new TreeMap<>();
      List<Edge> edges = new ArrayList<>();
      for (String line : lines) {
        int colon = line.indexOf(':');
        assert colon > 0;
        String fromLabel = line.substring(0, colon);
        Node from = nodes.computeIfAbsent(fromLabel, Node::new);
        String rest = line.substring(colon + 1).trim();
        for (String toLabel : rest.split(" ")) {
          Node to = nodes.computeIfAbsent(toLabel, Node::new);
          Edge edge = new Edge(from, to);
          edges.add(edge);
          from.edges.add(edge);
          to.edges.add(edge);
        }
      }
      Collections.sort(edges);
      System.out.println(nodes.size() + " nodes with " + edges.size() + " edges\n");
      removeDups(edges);
      solve(edges);
    }
  }

  private static void removeDups(List<Edge> edges) {
    Iterator<Edge> it = edges.iterator();
    Edge prev = null;
    while (it.hasNext()) {
      Edge e = it.next();
      if (prev != null && e.compareTo(prev) == 0) {
        System.err.println("Removed dup edge " + e);
        it.remove();
      } else {
        prev = e;
      }
    }
  }

  /*
   * This brute-force solution really shouldn't work but with the input I got it actually completed
   * fairly quickly. (Every AoC user gets different input for each puzzle.) Apparently the very
   * first edge it chose was in fact one of the ones that needed to be cut. So a solution was found
   * in a bit more than a minute. I think the expected run time is more like four days
   * single-threaded, some number of hours multi-threaded.
   *
   * The approach is very naive. Check each combination of three edges, as follows. Traverse the
   * graph from one end of one of the edges, ignoring the three edges (treating them as cut). Stop
   * as soon as the other end of that edge is encountered, or both ends of one of the other two
   * edges. If the traversal is complete without either of those happening then the three edges
   * make up the cut we are looking for.
   *
   * Forum discussion suggests better approaches. One is to choose a large number of random pairs of
   * nodes and compute the paths between them. Then consider cutting the edges that show up most
   * often in those paths. I did not code that.
   */
  private static void solve(List<Edge> edges) {
    long start = System.nanoTime();
    for (int i1 = 0; i1 < edges.size(); i1++) {
      Edge edge1 = edges.get(i1);
      System.out.println(
          "Edge1 "
              + i1
              + 1
              + " of "
              + edges.size()
              + " ("
              + edge1
              + "), elapsed "
              + (System.nanoTime() - start) / 1_000_000_000);
      for (int i2 = i1 + 1; i2 < edges.size(); i2++) {
        if (i2 % 200 == 0) {
          System.out.println(
              "  Edge2 " + i2 + ", elapsed " + (System.nanoTime() - start) / 1_000_000_000);
        }
        Edge edge2 = edges.get(i2);
        for (int i3 = i2 + 1; i3 < edges.size(); i3++) {
          Edge edge3 = edges.get(i3);
          Set<Node> seen = new HashSet<>();
          if (visit(edge1.a, edge1, edge2, edge3, seen)) {
            System.out.println(
                "Success with component of size "
                    + seen.size()
                    + ": "
                    + edge1
                    + ", "
                    + edge2
                    + ", "
                    + edge3);
            return;
          }
        }
      }
    }
  }

  private static boolean visit(Node start, Edge edge1, Edge edge2, Edge edge3, Set<Node> seen) {
    if (!seen.add(start)) {
      return true;
    }
    if ((start == edge1.a && seen.contains(edge1.b))
        || (start == edge1.b && seen.contains(edge1.a))
        || (start == edge2.a && seen.contains(edge2.b))
        || (start == edge2.b && seen.contains(edge2.a))
        || (start == edge3.a && seen.contains(edge3.b))
        || (start == edge3.b && seen.contains(edge3.a))) {
      return false;
    }
    for (Edge edge : start.edges) {
      if (edge == edge1 || edge == edge2 || edge == edge3) {
        continue;
      }
      Node other = (edge.a == start) ? edge.b : edge.a;
      if (!visit(other, edge1, edge2, edge3, seen)) {
        return false;
      }
    }
    return true;
  }

  private static class Node implements Comparable<Node> {
    private final String label;
    final List<Edge> edges = new ArrayList<>();

    Node(String label) {
      this.label = label;
    }

    @Override
    public int compareTo(Node that) {
      return this.label.compareTo(that.label);
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private record Edge(Node a, Node b) implements Comparable<Edge> {
    Edge {
      if (a.compareTo(b) > 0) {
        Node t = a;
        a = b;
        b = t;
      }
    }

    private static final Comparator<Edge> COMPARATOR =
        Comparator.comparing((Edge e) -> e.a).thenComparing(e -> e.b);

    @Override
    public int compareTo(Edge that) {
      return COMPARATOR.compare(this, that);
    }

    @Override
    public String toString() {
      return a + "->" + b;
    }
  }
}
