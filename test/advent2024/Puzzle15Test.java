package advent2024;

import static advent2024.Puzzle15.Contents.BOX_LEFT;
import static advent2024.Puzzle15.Contents.BOX_RIGHT;
import static advent2024.Puzzle15.Contents.EMPTY;
import static advent2024.Puzzle15.Contents.ROBOT;
import static advent2024.Puzzle15.Contents.WALL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.Truth.assertThat;

import advent2024.Puzzle15.Contents;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * @author Éamonn McManus
 */
public class Puzzle15Test {
  record Example(String before, Dir move, String after) {}

  private static final ImmutableList<Example> EXAMPLES =
      ImmutableList.of(
          new Example(
              """
              ...@..
              ......
              """,
              Dir.E,
              """
              ....@.
              ......
              """),
          new Example(
              """
              .@[]..
              ......
              """,
              Dir.E,
              """
              ..@[].
              ......
              """),
          new Example(
              """
              .@[][]..
              ........
              """,
              Dir.E,
              """
              ..@[][].
              ........
              """),
          new Example(
              """
              .[][]@..
              ........
              """,
              Dir.W,
              """
              [][]@...
              ........
              """),
          new Example(
              """
              #[][]@..
              ........
              """,
              Dir.W,
              """
              #[][]@..
              ........
              """),
          new Example(
              """
              ........
              [][][][]
              .[][][].
              ..[][]..
              ...[]...
              ...@....
              """,
              Dir.N,
              """
              [][][][]
              .[][][].
              ..[][]..
              ...[]...
              ...@....
              ........
              """),
          new Example(
              """
              ........
              [][][][]
              .[][][].
              ..[][]..
              ...[]...
              ....@...
              """,
              Dir.N,
              """
              [][][][]
              .[][][].
              ..[][]..
              ...[]...
              ....@...
              ........
              """),
          new Example(
              """
              ....##..
              [][]..[]
              .[][][].
              ..[][]..
              ...[]...
              ....@...
              """,
              Dir.N,
              """
              [][]##[]
              .[][][].
              ..[][]..
              ...[]...
              ....@...
              ........
              """)
          );

  @Test
  public void checkExamples() {
    for (var example : EXAMPLES) {
      Input input = makeMap(example.before);
      var gridMap = input.gridMap;
      var unused = Puzzle15.part2Move(gridMap, input.robot, example.move);
      assertThat(mapToString(gridMap)).isEqualTo(example.after.strip());
    }
  }

  record Input(Map<Coord, Contents> gridMap, Coord robot) {}

  private static Input makeMap(String s) {
    List<String> lines = Splitter.on('\n').omitEmptyStrings().splitToList(s);
    checkArgument(lines.stream().allMatch(line -> line.length() == lines.get(0).length()), "%s", lines);
    Map<Coord, Contents> gridMap = new LinkedHashMap<>();
    Coord robot = null;
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      for (int j = 0; j < line.length(); j++) {
        Coord coord = new Coord(i, j);
        Contents contents =
            switch (line.charAt(j)) {
              case '[' -> BOX_LEFT;
              case ']' -> BOX_RIGHT;
              case '.' -> EMPTY;
              case '#' -> WALL;
              case '@' -> {
                checkState(robot == null);
                robot = coord;
                yield ROBOT;
              }
              default -> throw new AssertionError(line.charAt(j));
            };
        gridMap.put(coord, contents);
      }
    }
    checkNotNull(robot);
    return new Input(gridMap, robot);
  }

  private static String mapToString(Map<Coord, Contents> gridMap) {
    StringBuilder sb = new StringBuilder();
    gridMap.forEach(
      (coord, contents) -> {
        if (coord.col() == 0 && !sb.isEmpty()) {
          sb.append('\n');
        }
        sb.append(contents.toChar());
      }
    );
    return sb.toString();
  }
}