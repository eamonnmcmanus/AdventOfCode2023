package advent2020;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle7 {
  private static final String SAMPLE =
      """
      light red bags contain 1 bright white bag, 2 muted yellow bags.
      dark orange bags contain 3 bright white bags, 4 muted yellow bags.
      bright white bags contain 1 shiny gold bag.
      muted yellow bags contain 2 shiny gold bags, 9 faded blue bags.
      shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
      dark olive bags contain 3 faded blue bags, 4 dotted black bags.
      vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
      faded blue bags contain no other bags.
      dotted black bags contain no other bags.
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle7.class.getResourceAsStream("puzzle7.txt")));

  private static final Pattern FROM_PATTERN = Pattern.compile("^([a-z]+ [a-z]+) bags contain ");

  private static final Pattern TO_PATTERN = Pattern.compile("(\\d+) ([a-z]+ [a-z]+) bag");

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        MutableValueGraph<String, Integer> graph =
            ValueGraphBuilder.<String, Integer>directed().build();
        for (String line : lines) {
          Matcher matcher = FROM_PATTERN.matcher(line);
          checkState(matcher.find());
          String from = matcher.group(1);
          matcher = TO_PATTERN.matcher(line);
          while (matcher.find()) {
            int n = Integer.parseInt(matcher.group(1));
            String to = matcher.group(2);
            graph.putEdgeValue(from, to, n);
          }
        }
        Set<String> preds = predecessors(graph, "shiny gold");
        System.out.printf(
            "For %s, number of bags that can contain a shiny gold bag is %d\n", name, preds.size());
        System.out.printf(
            "For %s, number of bags that a shiny bag contains is %d\n",
            name, containment(graph, "shiny gold") - 1);
      }
    }
  }

  private static <N> long containment(ValueGraph<N, Integer> graph, N node) {
    long n = 1;
    for (N succ : graph.successors(node)) {
      long multiplier = graph.edgeValue(node, succ).get();
      n += multiplier * containment(graph, succ);
    }
    return n;
  }

  private static <N> Set<N> predecessors(ValueGraph<N, ?> graph, N node) {
    Set<N> preds = new LinkedHashSet<>();
    predecessors(graph, node, preds);
    return preds;
  }

  private static <N> void predecessors(ValueGraph<N, ?> graph, N node, Set<N> preds) {
    for (N pred : graph.predecessors(node)) {
      if (preds.add(pred)) {
        predecessors(graph, pred, preds);
      }
    }
  }
}