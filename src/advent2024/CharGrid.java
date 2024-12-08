package advent2024;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;

/**
 * @author Éamonn McManus
 */
class CharGrid {
  record Coord(int line, int col) {
    Coord plus(Coord that) {
      return new Coord(this.line + that.line, this.col + that.col);
    }

    Coord minus(Coord that) {
      return new Coord(this.line - that.line, this.col - that.col);
    }
  }

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

  Optional<Coord> firstMatch(IntPredicate predicate) {
    for (int line = 0; line < height; line++) {
      for (int col = 0; col < width; col++) {
        if (predicate.test(get(line, col))) {
          return Optional.of(new Coord(line, col));
        }
      }
    }
    return Optional.empty();
  }

  CharGrid withChange(Coord coord, char c) {
    List<String> newLines = new ArrayList<>(lines);
    char[] changed = newLines.get(coord.line()).toCharArray();
    changed[coord.col()] = c;
    newLines.set(coord.line(), new String(changed));
    return new CharGrid(newLines);
  }

  @Override
  public String toString() {
    return String.join("\n", lines);
  }
}
