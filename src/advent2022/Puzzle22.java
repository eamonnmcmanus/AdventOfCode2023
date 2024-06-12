package advent2022;

import static advent2022.Puzzle22.Corner.BL;
import static advent2022.Puzzle22.Corner.BR;
import static advent2022.Puzzle22.Corner.TL;
import static advent2022.Puzzle22.Corner.TR;
import static advent2022.Puzzle22.Dir.DOWN;
import static advent2022.Puzzle22.Dir.LEFT;
import static advent2022.Puzzle22.Dir.RIGHT;
import static advent2022.Puzzle22.Dir.UP;
import static java.lang.Integer.max;
import static java.lang.Integer.min;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle22 {
  private static final String SAMPLE =
      """
              ...#
              .#..
              #...
              ....
      ...#.......#
      ........#...
      ..#....#....
      ..........#.
              ...#....
              .....#..
              .#......
              ......#.

      10R5L5R10L4R5L5
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle22.class.getResourceAsStream("puzzle22.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        String directionString = lines.getLast();
        List<Action> actions = parseDirections(directionString);
        List<String> gridLines = lines.stream()
            .limit(lines.size() - 2)
            .map(s -> s.replaceFirst("\\s+$", ""))
            .toList();
        part1(name, actions, gridLines);
        part2(name, actions, gridLines);
      }
    }
  }

  private static void part1(String name, List<Action> actions, List<String> gridLines) {
    Node topLeft = buildPart1Graph(gridLines);
    traverse(name + " part 1", topLeft, actions);
  }

  private static void part2(String name, List<Action> actions, List<String> gridLines) {
    Node[][] nodes = buildNodes(gridLines);
    fold(name, nodes);
    Node topLeft = null;
    for (Node[] node : nodes) {
      if (node[0] != null) {
        topLeft = node[0];
        break;
      }
    }
    traverse(name + " part 2", topLeft, actions);
  }

  private static void traverse(String what, Node topLeft, List<Action> actions) {
    DirPos dirPos = new DirPos(RIGHT, topLeft);
    for (Action action : actions) {
      dirPos = action.apply(dirPos);
    }
    Node pos = dirPos.pos;
    int password = 1000 * (pos.y + 1) + 4 * (pos.x + 1) + dirPos.dir.facing();
    System.out.println(what + " ended at " + dirPos + " => " + password);
  }

  record DirPos(Dir dir, Node pos) {}

  sealed interface Action {
    DirPos apply(DirPos dirPos);
  }
  
  enum TurnLeft implements Action {
    LEFT;

    @Override
    public DirPos apply(DirPos dirPos) {
      return new DirPos(dirPos.dir.turnLeft(), dirPos.pos);
    }
  }

  enum TurnRight implements Action {
    RIGHT;

    @Override
    public DirPos apply(DirPos dirPos) {
      return new DirPos(dirPos.dir.turnRight(), dirPos.pos);
    }
  }

  record Move(int n) implements Action {
    @Override
    public DirPos apply(DirPos dirPos) {
      Dir dir = dirPos.dir;
      Node pos = dirPos.pos;
      for (int i = 0; i < n; i++) {
        DirPos next = pos.next.get(dir);
        if (next == null) {
          break;
        }
        pos = next.pos;
        dir = next.dir;
      }
      return new DirPos(dir, pos);
    }
  }

  enum Dir {
    RIGHT, DOWN, LEFT, UP;

    Dir turnLeft() {
      return switch (this) {
        case RIGHT -> UP;
        case DOWN -> RIGHT;
        case LEFT -> DOWN;
        case UP -> LEFT;
      };
    }

    Dir turnRight() {
      return switch (this) {
        case RIGHT -> DOWN;
        case DOWN -> LEFT;
        case LEFT -> UP;
        case UP -> RIGHT;
      };
    }

    Dir opposite() {
      return turnLeft().turnLeft(); // :-)
    }

    int facing() {
      return ordinal();
    }
  }

  static Node buildPart1Graph(List<String> gridLines) {
    Node[][] nodes = buildNodes(gridLines);
    int width = nodes.length;
    int height = nodes[0].length;
    int firstX[] = new int[height];
    Arrays.fill(firstX, Integer.MAX_VALUE);
    int lastX[] = new int[height];
    Arrays.fill(lastX, Integer.MIN_VALUE);
    int firstY[] = new int[width];
    Arrays.fill(firstY, Integer.MAX_VALUE);
    int lastY[] = new int[width];
    Arrays.fill(lastY, Integer.MIN_VALUE);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (nodes[x][y] != null) {
          firstX[y] = min(firstX[y], x);
          firstY[x] = min(firstY[x], y);
          lastX[y] = max(lastX[y], x);
          lastY[x] = max(lastY[x], y);
        }
      }
    }
    // Link the top of each column to the bottom.
    for (int x = 0; x < width; x++) {
      Node top = nodes[x][firstY[x]];
      Node bottom = nodes[x][lastY[x]];
      if (top.free && bottom.free) {
        top.putNext(UP, new DirPos(UP, bottom));
        bottom.putNext(DOWN, new DirPos(DOWN, top));
      }
    }
    // Link the left of each row to the right.
    for (int y = 0; y < height; y++) {
      Node left = nodes[firstX[y]][y];
      Node right = nodes[lastX[y]][y];
      if (left.free && right.free) {
        left.putNext(LEFT, new DirPos(LEFT, right));
        right.putNext(RIGHT, new DirPos(RIGHT, left));
      }
    }

    return nodes[firstX[0]][0];
  }

  static Node[][] buildNodes(List<String> gridLines) {
    int height = gridLines.size();
    int width = gridLines.stream().mapToInt(String::length).max().getAsInt();
    Node[][] nodes = new Node[width][height]; // index is [x][y]
    for (int y = 0; y < height; y++) {
      String line = gridLines.get(y);
      for (int x = 0; x < line.length(); x++) {
        char c = line.charAt(x);
        if (c != ' ') {
          boolean free = switch (c) {
            case '.' -> true;
            case '#' -> false;
            default -> throw new AssertionError(line.charAt(x));
          };
          nodes[x][y] = new Node(x, y, free);
        }
      }
    }
    // Link each node to the ones adjacent to it in the map, without taking wrapping or folding
    // into account. Don't link to walls.
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Node node = nodes[x][y];
        if (node == null || !node.free) {
          continue;
        }
        Node right = (x + 1 < width) ? nodes[x + 1][y] : null;
        if (right != null && right.free) {
          node.putNext(RIGHT, new DirPos(RIGHT, right));
          right.putNext(LEFT, new DirPos(LEFT, node));
        }
        Node down = (y + 1 < height) ? nodes[x][y + 1] : null;
        if (down != null && down.free) {
          node.putNext(DOWN, new DirPos(DOWN, down));
          down.putNext(UP, new DirPos(UP, node));
        }
      }
    }
    return nodes;
  }

  static void fold(String name, Node[][] nodes) {
    switch (name) {
      case "sample" -> foldSample(nodes);
      case "problem" -> foldProblem(nodes);
      default -> throw new AssertionError(name);
    }
  }

  // This was all extremely time-consuming and the result is not great. An ideal solution would
  // figure out the folding and edge-joining for itself. This is not that solution. I laboriously
  // worked out the two folds (one for the sample and a different one for the problem) and manually
  // coded the implied joins. The final code here is cleaned up quite a bit from my first working
  // solution.

  // Here is what the sample map looks like, using the same numbering as in the problem description:
  //    ┏━━━━━┓
  //    ┃    ┏━┓
  //    ┃  ┏━┃1┃━━━━┓
  //    ┃  ┃ ┗━┛    ┃
  //   ┏━┓┏━┓┏━┓    ┃
  // ┏━┃2┃┃3┃┃4┃━┓  ┃
  // ┃ ┗━┛┗━┛┗━┛ ┃  ┃
  // ┃  ┃  ┃ ┏━┓┏━┓ ┃
  // ┃  ┃  ┗━┃5┃┃6┃━┛
  // ┃  ┃    ┗━┛┗━┛
  // ┃  ┗━━━━━┛  ┃
  // ┗━━━━━━━━━━━┛
  // Cube faces that are adjacent on the map remain adacent, and nothing special needs to be done
  // when crossing from one to another. But when crossing an edge in the map, we must jump to the
  // appropriate position in the joined face, and we must change direction as indicated. If we
  // imagine 4 as the top of the folded cube, with 5 facing towards us, then 6 is the right face,
  // with its top left corner touching the bottom right corner of 4, its top right touching the
  // bottom right of 1, and its bottom left touching the bottom left of 2. (For this last one,
  // consider that 2 is the bottom face, folded twice under 4, which means that its left on the map
  // is on the right side of the cube. That's where 6 touches it.)
  // Looking at 3, its top right touches the bottom left of 1 and its bottom right touches the top
  // left of 5.
  // That leaves 2: we've seen that its bottom left touches the bottom left of 6. Its bottom right
  // touches the bottom left of 5. Its top left touches the top right of 1.
  static void foldSample(Node[][] nodes) {
    FaceFactory factory = new FaceFactory(4, nodes);
    Face face1 = factory.newFace(2, 0);
    Face face2 = factory.newFace(0, 1);
    Face face3 = factory.newFace(1, 1);
    Face face4 = factory.newFace(2, 1);
    Face face5 = factory.newFace(2, 2);
    Face face6 = factory.newFace(3, 2);
    face1.join(BL, UP, face3, TR, LEFT);
    face1.join(BR, UP, face6, TR, DOWN);
    face1.join(TR, LEFT, face2, TL, RIGHT);
    face2.join(BL, UP, face6, BL, RIGHT);
    face2.join(BR, LEFT, face5, BL, RIGHT);
    face3.join(BR, LEFT, face5, TL, DOWN);
    face6.join(TL, RIGHT, face4, BR, UP);
  }

  // The problem input uses a different folding:
  // ┏━━━━━━┓  ┏━━━━┓
  // ┃     ┏━┓┏━┓   ┃
  // ┃ ┏━━━┃1┃┃2┃━┓ ┃
  // ┃ ┃   ┗━┛┗━┛ ┃ ┃
  // ┃ ┃   ┏━┓ ┃  ┃ ┃
  // ┃ ┃ ┏━┃3┃━┛  ┃ ┃
  // ┃ ┃ ┃ ┗━┛    ┃ ┃
  // ┃ ┃┏━┓┏━┓    ┃ ┃
  // ┃ ┗┃4┃┃5┃━━━━┛ ┃
  // ┃  ┗━┛┗━┛      ┃
  // ┃  ┏━┓ ┃       ┃
  // ┗━━┃6┃━┛       ┃
  //    ┗━┛         ┃
  //     ┗━━━━━━━━━━┛
  // Clearly the bottom left of 2 going right matches the top right of 3 going down, which we can
  // write as:
  // BL2(R) = TR3(D)
  // The other easy cases:
  // TL3(D) = TL4(R)
  // BL5(R) = TR6(D)
  // For the others, we imagine 3 as the top of the folded cube, with 2 on the right.
  // BR2(U) = TR5(D)
  // BL1(U) = TL4(D)
  // Now if we run down the edge from BL1(U) = TL4(D) we arrive at TL1 and BL4. BL4 touches TL6 so:
  // TL6(D) = TL1(R)
  // If we continue on that new edge we arrive at BL6 and TR1. TR1 touches TL2 so:
  // BL6(R) = TL2(R)
  private static void foldProblem(Node[][] nodes) {
    FaceFactory factory = new FaceFactory(50, nodes);
    Face face1 = factory.newFace(1, 0);
    Face face2 = factory.newFace(2, 0);
    Face face3 = factory.newFace(1, 1);
    Face face4 = factory.newFace(0, 2);
    Face face5 = factory.newFace(1, 2);
    Face face6 = factory.newFace(0, 3);
    face2.join(BL, RIGHT, face3, TR, DOWN);
    face3.join(TL, DOWN, face4, TL, RIGHT);
    face5.join(BL, RIGHT, face6, TR, DOWN);
    face2.join(BR, UP, face5, TR, DOWN);
    face1.join(BL, UP, face4, TL, DOWN);
    face6.join(TL, DOWN, face1, TL, RIGHT);
    face6.join(BL, RIGHT, face2, TL, RIGHT);
  }

  enum Corner {TL, TR, BL, BR}

  record FaceFactory(int n, Node[][] nodes) {
    Face newFace(int topLeftUnitX, int topLeftUnitY) {
      return new Face(topLeftUnitX * n, topLeftUnitY * n, n, nodes);
    }
  }

  record Face(int topLeftX, int topLeftY, int length, Node[][] nodes) {
    // If we are joining for example our BL going up [BL(U)] to their TR going left, then we are setting
    // our LEFT edges to enter the other face going DOWN and their UP edges to enter this face going RIGHT.
    // We are setting our:
    //    LEFT edges for TL(D) or BL(U)
    //    RIGHT edges for TR(D) or BR(U)
    //    UP edges for TL(R) or TR(L)
    //    DOWN edges for BL(R) or BR(L)
    // We are entering the other face in the opposite direction of these, for example if
    // otherCornerDir is TL(D) then we are entering RIGHT.
    void join(Corner ourCorner, Dir ourDir, Face other, Corner theirCorner, Dir theirDir) {
      Dir ourLeave = leave(ourCorner, ourDir);
      Dir theirLeave = leave(theirCorner, theirDir);
      Dir ourEnter = ourLeave.opposite();
      Dir theirEnter = theirLeave.opposite();
      Coord ourCoord = corner(ourCorner);
      Coord theirCoord = other.corner(theirCorner);
      for (int i = 0; i < length; i++) {
        Node us = nodes[ourCoord.x][ourCoord.y];
        Node them = nodes[theirCoord.x][theirCoord.y];
        if (us.free && them.free) {
          us.putNext(ourLeave, new DirPos(theirEnter, them));
          them.putNext(theirLeave, new DirPos(ourEnter, us));
        }
        ourCoord = ourCoord.move(ourDir);
        theirCoord = theirCoord.move(theirDir);
      }
    }

    record Coord(int x, int y) {
      Coord move(Dir dir) {
        int dx = switch (dir) {
          case RIGHT -> +1;
          case LEFT -> -1;
          case UP, DOWN -> 0;
        };
        int dy = switch (dir) {
          case DOWN -> +1;
          case UP -> -1;
          case LEFT, RIGHT -> 0;
        };
        return new Coord(x + dx, y + dy);
      }
    }

    Coord corner(Corner corner) {
      int x = switch (corner) {
        case TL, BL -> topLeftX;
        case TR, BR -> topLeftX + length - 1;
      };
      int y = switch (corner) {
        case TL, TR -> topLeftY;
        case BL, BR -> topLeftY + length - 1;
      };
      return new Coord(x, y);
    }

    record CornerDir(Corner corner, Dir dir) {}

    private static final Map<CornerDir, Dir> LEAVE_MAP = ImmutableMap.of(
        new CornerDir(TL, DOWN), LEFT,
        new CornerDir(BL, UP), LEFT,
        new CornerDir(TR, DOWN), RIGHT,
        new CornerDir(BR, UP), RIGHT,
        new CornerDir(TL, RIGHT), UP,
        new CornerDir(TR, LEFT), UP,
        new CornerDir(BL, RIGHT), DOWN,
        new CornerDir(BR, LEFT), DOWN);

    static Dir leave(Corner corner, Dir dir) {
      return LEAVE_MAP.get(new CornerDir(corner, dir));
    }
  }

  static List<Action> parseDirections(String directions) {
    Pattern pattern = Pattern.compile("(\\d+)([LR]?)");
    Matcher matcher = pattern.matcher(directions);
    ImmutableList.Builder<Action> builder = ImmutableList.builder();
    matcher.results().forEach(
        matchResult -> {
          int moveAmount = Integer.parseInt(matchResult.group(1));
          builder.add(new Move(moveAmount));
          switch (matchResult.group(2)) {
            case "L" -> builder.add(TurnLeft.LEFT);
            case "R" -> builder.add(TurnRight.RIGHT);
            case "" -> {} // end
            default -> throw new AssertionError(matchResult.group(2));
          }
        });
    return builder.build();
  }

  static class Node {
    final int x, y;
    final boolean free;
    final Map<Dir, DirPos> next = new EnumMap<>(Dir.class);

    Node(int x, int y, boolean free) {
      this.x = x;
      this.y = y;
      this.free = free;
    }

    void putNext(Dir dir, DirPos dirPos) {
      Object old = next.put(dir, dirPos);
      assert old == null : this + " " + dir + " " + old + " -> " + dirPos;
    }

    @Override
    public String toString() {
      return free ? "." : "#" + "(" + x + "," + y + ")";
    }
  }
}
