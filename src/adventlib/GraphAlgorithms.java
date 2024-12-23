package adventlib;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Graph;
import com.google.common.graph.PredecessorsFunction;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.ValueGraph;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Éamonn McManus
 */
public class GraphAlgorithms {
  public static <N> ImmutableMap<N, Integer> distances(SuccessorsFunction<N> successors, N start) {
    Map<N, Integer> distances = new LinkedHashMap<>();
    distances(successors, start, 0, distances);
    return ImmutableMap.copyOf(distances);
  }

  private static <N> void distances(
      SuccessorsFunction<N> successors, N start, int distance, Map<N, Integer> distances) {
    if (distances.getOrDefault(start, Integer.MAX_VALUE) <= distance) {
      return;
    }
    distances.put(start, distance);
    for (N succ : successors.successors(start)) {
      distances(successors, succ, distance + 1, distances);
    }
  }

  public static <N> ImmutableList<N> shortestPath(Graph<N> graph, N start, N end) {
    return shortestPath(graph, graph, start, end);
  }

  public static <N> ImmutableList<N> shortestPath(ValueGraph<N, ?> graph, N start, N end) {
    return shortestPath(graph, graph, start, end);
  }

  public static <N> ImmutableList<N> shortestPath(
      SuccessorsFunction<N> successors, PredecessorsFunction<N> predecessors, N start, N end) {
    var distances = distances(successors, start);
    List<N> path = new ArrayList<>();
    N cur = end;
    for (int distance = distances.get(end); distance > 0; distance--) {
      path.add(cur);
      int nextDistance = distance - 1;
      var preds = predecessors.predecessors(cur);
      boolean found = false;
      for (var pred : preds) {
        if (distances.get(pred) == nextDistance) {
          cur = pred;
          found = true;
          break;
        }
      }
      checkState(found);
    }
    return ImmutableList.copyOf(path.reversed());
  }
}
