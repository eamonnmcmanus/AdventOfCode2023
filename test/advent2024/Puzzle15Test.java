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
              """),
          new Example(
              /*
              pushNorthSouth move=N west=(7,3) east=(7,4)
              pushNorthSouth move=N west=(6,4) east=(6,5)
              pushNorthSouth move=N west=(5,3) east=(5,6)
              pushNorthSouth move=N west=(4,2) east=(4,7)
              pushNorthSouth move=N west=(3,2) east=(3,8)
              pushNorthSouth move=N west=(2,3) east=(2,6)
              pushNorthSouth move=N west=(1,4) east=(1,5)
              */
              """
              #............[]
              ....[].[][][][]
              #..[][]..##[]..
              ..[]...[]......
              ..[][][].[]..##
              ...[][]........
              ....[].[]....##
              ...[]##....[]..
              ....@...[]...[]
              """,
              Dir.N,
              /*
              #...[].......[]
              ...[][][][][][]
              #.[]...[]##[]..
              ..[][][].......
              ...[][]..[]..##
              ....[].........
              ...[]..[]....##
              ....@##....[]..
              ........[]...[]
              */
              """
              #...[].......[]
              ...[]..[][][][]
              #.[].[][]##[]..
              ..[][][].......
              ...[][]..[]..##
              ....[].........
              ...[]..[]....##
              ....@##....[]..
              ........[]...[]
              """));

  @Test
  public void checkExamples() {
    for (var example : EXAMPLES) {
      Input input = makeMap(example.before);
      var gridMap = input.gridMap;
      var unused = Puzzle15.part2Move(gridMap, input.robot, example.move);
      if (!mapToString(gridMap).equals(example.after.strip())) {
        System.err.println("For example: " + example);
        System.err.println("expected");
        System.err.println(example.after.strip());
        System.err.println("actual");
        System.err.println(mapToString(gridMap));
      }
      assertThat(mapToString(gridMap)).isEqualTo(example.after.strip());
    }
  }

  record Input(Map<Coord, Contents> gridMap, Coord robot) {}

  private static Input makeMap(String s) {
    List<String> lines = Splitter.on('\n').omitEmptyStrings().splitToList(s);
    checkArgument(
        lines.stream().allMatch(line -> line.length() == lines.get(0).length()), "%s", lines);
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
        });
    return sb.toString();
  }
}