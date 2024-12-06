package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

/**
 * @author Éamonn McManus
 */
class CharGrid {
  record Coord(int line, int col) {}

  private final List<String> lines;
  private final int height;
  private final int width;

  CharGrid(List<String> lines) {
    checkArgument(lines != null && !lines.isEmpty());
    this.lines = lines;
    this.height = lines.size();
    this.width = lines.getFirst().length();
    checkArgument(lines.stream().allMatch(line -> line.length() == width));
  }

  int height() {
    return height;
  }

  int width() {
    return width;
  }

  boolean valid(Coord coord) {
    return valid(coord.line, coord.col);
  }

  boolean valid(int line, int col) {
    return line >= 0 && line < height && col >= 0 && col < width;
  }

  char get(Coord coord) {
    return get(coord.line, coord.col);
  }

  char get(int line, int col) {
    if (valid(line, col)) {
      return lines.get(line).charAt(col);
    }
    return ' ';
  }
}
