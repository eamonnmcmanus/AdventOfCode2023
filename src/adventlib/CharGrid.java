package adventlib;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * @author Éamonn McManus
 */
public class CharGrid {
  public record Coord(int line, int col) {
    public Coord plus(Coord that) {
      return new Coord(this.line + that.line, this.col + that.col);
    }

    public Coord minus(Coord that) {
      return new Coord(this.line - that.line, this.col - that.col);
    }

    @Override
    public String toString() {
      // This is (y,x) order, of course.
      return "(" + line + "," + col + ")";
    }
  }

  private final List<String> lines;
  private final int height;
  private final int width;

  public CharGrid(List<String> lines) {
    checkArgument(lines != null && !lines.isEmpty());
    this.lines = lines;
    this.height = lines.size();
    this.width = lines.getFirst().length();
    checkArgument(lines.stream().allMatch(line -> line.length() == width));
  }

  public int height() {
    return height;
  }

  public int width() {
    return width;
  }

  public int size() {
    return height * width;
  }

  public boolean valid(Coord coord) {
    return valid(coord.line, coord.col);
  }

  public boolean valid(int line, int col) {
    return line >= 0 && line < height && col >= 0 && col < width;
  }

  public char get(Coord coord) {
    return get(coord.line, coord.col);
  }

  public char get(int line, int col) {
    if (valid(line, col)) {
      return lines.get(line).charAt(col);
    }
    return ' ';
  }

  public Optional<Coord> firstMatch(IntPredicate predicate) {
    for (int line = 0; line < height; line++) {
      for (int col = 0; col < width; col++) {
        if (predicate.test(get(line, col))) {
          return Optional.of(new Coord(line, col));
        }
      }
    }
    return Optional.empty();
  }

  public CharGrid withChange(Coord coord, char c) {
    List<String> newLines = new ArrayList<>(lines);
    char[] changed = newLines.get(coord.line()).toCharArray();
    changed[coord.col()] = c;
    newLines.set(coord.line(), new String(changed));
    return new CharGrid(newLines);
  }

  public Iterable<Coord> coords() {
    return () ->
        new Iterator<Coord>() {
          private int row = 0;
          private int col = 0;

          @Override
          public boolean hasNext() {
            return row < height;
          }

          @Override
          public Coord next() {
            var result = new Coord(row, col);
            if (++col >= width) {
              col = 0;
              ++row;
            }
            return result;
          }
        };
  }

  public <N> ImmutableGraph<N> toGraph(Set<Dir> adjacentDirs, Function<Coord, N> nodeFactory) {
    Map<Coord, N> coordToNode = new LinkedHashMap<>();
    ImmutableGraph.Builder<N> builder = GraphBuilder.undirected().<N>immutable();
    for (Coord coord : coords()) {
      N node = nodeFactory.apply(coord);
      coordToNode.put(coord, node);
      builder.addNode(node);
    }
    for (Coord coord : coords()) {
      N coordNode = coordToNode.get(coord);
      for (Dir dir : adjacentDirs) {
        Coord adjacent = dir.move(coord, 1);
        if (valid(adjacent)) {
          builder.putEdge(coordNode, coordToNode.get(adjacent));
        }
      }
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return String.join("\n", lines);
  }
}
