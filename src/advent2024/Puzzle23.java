package advent2024;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle23 {
  private static final String SAMPLE =
      """
      kh-tc
      qp-kh
      de-cg
      ka-co
      yn-aq
      qp-ub
      cg-tb
      vc-aq
      tb-ka
      wh-tc
      yn-cg
      kh-ub
      ta-co
      de-co
      tc-td
      tb-wq
      wh-td
      ta-ka
      td-qp
      aq-cg
      wq-ub
      ub-vc
      de-ta
      wq-aq
      wq-vc
      wh-yn
      ka-de
      kh-ta
      co-tc
      wh-qp
      tb-vc
      td-yn
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle23.class.getResourceAsStream("puzzle23.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        Splitter splitter = Splitter.on('-');
        ImmutableGraph.Builder<String> builder = GraphBuilder.undirected().<String>immutable();
        for (String line : lines) {
          List<String> parts = splitter.splitToList(line);
          checkArgument(parts.size() == 2);
          checkArgument(parts.get(0).length() == 2 && parts.get(1).length() == 2);
          builder.putEdge(parts.get(0), parts.get(1));
        }
        ImmutableGraph<String> graph = builder.build();
        Set<Set<String>> triples = new LinkedHashSet<>();
        for (String node : graph.nodes()) {
          if (node.startsWith("t")) {
            List<String> next = new ArrayList<>(graph.successors(node));
            for (int i = 0; i < next.size(); i++) {
              for (int j = i + 1; j < next.size(); j++) {
                if (graph.hasEdgeConnecting(next.get(i), next.get(j))) {
                  triples.add(ImmutableSet.of(node, next.get(i), next.get(j)));
                }
              }
            }
          }
        }
        System.out.printf("For %s, number of 3-cliques is %d\n", name, triples.size());
        List<String> sortedNodes = graph.nodes().stream().sorted().toList();
        List<Set<String>> cliques = new ArrayList<>();
        for (String node : sortedNodes) {
          cliques.add(new TreeSet<>(Set.of(node)));
        }
        boolean more;
        do {
          more = false;
          for (String node : sortedNodes) {
            for (Set<String> clique : cliques) {
              if (clique.contains(node)) {
                break;
              }
              boolean connected = clique.stream().allMatch(n -> graph.hasEdgeConnecting(n, node));
              if (connected) {
                clique.add(node);
                more = true;
              }
            }
          }
          cliques = new ArrayList<>(new LinkedHashSet<>(cliques));
        } while (more);
        Collections.sort(cliques, Comparator.comparing(Set::size));
        Set<String> largest = cliques.getLast();
        System.out.printf("For %s, largest clique is %s\n", name, String.join(",", largest));
      }
    }
  }
}