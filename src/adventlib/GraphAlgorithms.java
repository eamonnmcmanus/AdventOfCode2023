package adventlib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Graph;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Éamonn McManus
 */
public class GraphAlgorithms {
  public static <N> ImmutableMap<N, Integer> distances(Graph<N> graph, N start) {
    Map<N, Integer> distances = new LinkedHashMap<>();
    distances(graph, start, 0, distances);
    return ImmutableMap.copyOf(distances);
  }

  private static <N> void distances(
      Graph<N> graph, N start, int distance, Map<N, Integer> distances) {
    if (distances.getOrDefault(start, Integer.MAX_VALUE) <= distance) {
      return;
    }
    distances.put(start, distance);
    for (N succ : graph.successors(start)) {
      distances(graph, succ, distance + 1, distances);
    }
  }

  public static <N> ImmutableList<N> shortestPath(Graph<N> graph, N start, N end) {
    var distances = distances(graph, start);
    List<N> path = new ArrayList<>();
    N cur = end;
    for (int distance = distances.get(end); distance > 0; distance--) {
      path.add(cur);
      int nextDistance = distance - 1;
      var preds = graph.predecessors(cur);
      cur = preds.stream().filter(pred -> distances.get(pred) == nextDistance).findFirst().get();
    }
    return ImmutableList.copyOf(path.reversed());
  }
}
