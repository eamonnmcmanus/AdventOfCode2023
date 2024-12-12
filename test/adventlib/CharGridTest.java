package adventlib;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import adventlib.CharGrid.Coord;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;

/**
 * @author Éamonn McManus
 */
public class CharGridTest {
  private static final CharGrid EXAMPLE = new CharGrid(ImmutableList.of("abcd", "efgh", "ijkl"));

  @Test
  public void constructorValidation() {
    assertThrows(IllegalArgumentException.class, () -> new CharGrid(null));
    assertThrows(IllegalArgumentException.class, () -> new CharGrid(List.of()));
  }

  @Test
  public void heightWidth() {
    assertThat(EXAMPLE.height()).isEqualTo(3);
    assertThat(EXAMPLE.width()).isEqualTo(4);
  }

  @Test
  public void valid() {
    assertThat(EXAMPLE.valid(0, 0)).isTrue();
    assertThat(EXAMPLE.valid(2, 3)).isTrue();
    assertThat(EXAMPLE.valid(0, -1)).isFalse();
    assertThat(EXAMPLE.valid(-1, 0)).isFalse();
    assertThat(EXAMPLE.valid(-1, -1)).isFalse();
    assertThat(EXAMPLE.valid(3, 0)).isFalse();
    assertThat(EXAMPLE.valid(0, 4)).isFalse();

    assertThat(EXAMPLE.valid(new Coord(0, 0))).isTrue();
    assertThat(EXAMPLE.valid(new Coord(2, 3))).isTrue();
    assertThat(EXAMPLE.valid(new Coord(0, -1))).isFalse();
    assertThat(EXAMPLE.valid(new Coord(-1, 0))).isFalse();
    assertThat(EXAMPLE.valid(new Coord(-1, -1))).isFalse();
    assertThat(EXAMPLE.valid(new Coord(3, 0))).isFalse();
    assertThat(EXAMPLE.valid(new Coord(0, 4))).isFalse();
  }

  @Test
  public void get() {
    assertThat(EXAMPLE.get(0, 0)).isEqualTo('a');
    assertThat(EXAMPLE.get(-1, 0)).isEqualTo(' ');
    assertThat(EXAMPLE.get(2, 3)).isEqualTo('l');

    assertThat(EXAMPLE.get(new Coord(0, 0))).isEqualTo('a');
    assertThat(EXAMPLE.get(new Coord(-1, 0))).isEqualTo(' ');
    assertThat(EXAMPLE.get(new Coord(2, 3))).isEqualTo('l');
  }

  @Test
  public void firstMatch() {
    assertThat(EXAMPLE.firstMatch(c -> c == 'g')).hasValue(new Coord(1, 2));
    assertThat(EXAMPLE.firstMatch(c -> c == ' ')).isEmpty();
  }

  @Test
  public void withChange() {
    var changed = EXAMPLE.withChange(new Coord(1, 1), 'x');
    assertThat(changed.height()).isEqualTo(EXAMPLE.height());
    assertThat(changed.width()).isEqualTo(EXAMPLE.width());
    for (int i = 0; i < changed.height(); i++) {
      for (int j = 0; j < changed.width(); j++) {
        if (i == 1 && j == 1) {
          assertThat(changed.get(i, j)).isEqualTo('x');
        } else {
          assertThat(changed.get(i, j)).isEqualTo(EXAMPLE.get(i, j));
        }
      }
    }
  }

  @Test
  public void coords() {
    assertThat(Joiner.on("").join(EXAMPLE.coords()))
        .isEqualTo("(0,0)(0,1)(0,2)(0,3)(1,0)(1,1)(1,2)(1,3)(2,0)(2,1)(2,2)(2,3)");
  }

  @Test
  public void testToString() {
    assertThat(EXAMPLE.toString())
        .isEqualTo(
            """
            abcd
            efgh
            ijkl\
            """);
  }

  @Test
  public void coord() {
    var c12 = new Coord(1, 2);
    var c34 = new Coord(3, 4);
    assertThat(c12.plus(c34)).isEqualTo(new Coord(4, 6));
    assertThat(c12.minus(c34)).isEqualTo(new Coord(-2, -2));
    assertThat(c12.toString()).isEqualTo("(1,2)");
  }

  @Test
  public void toGraphOrthogonal() {
    var graph = EXAMPLE.toGraph(Dir.NEWS, Coord::toString);
    assertThat(graph.nodes()).hasSize(12);
    assertThat(graph.nodes()).containsAtLeast("(0,0)", "(0,1)", "(2,3)");
    assertThat(graph.nodes()).doesNotContain("(3,3)");
    assertThat(graph.successors("(0,0)")).containsExactly("(0,1)", "(1,0)");
    assertThat(graph.successors("(1,1)"))
        .containsExactly("(0,1)", "(1,0)", "(1,2)", "(2,1)");
  }

  @Test
  public void toGraphWithDiagonals() {
    var graph = EXAMPLE.toGraph(EnumSet.allOf(Dir.class), Coord::toString);
    assertThat(graph.nodes()).hasSize(12);
    assertThat(graph.nodes()).containsAtLeast("(0,0)", "(0,1)", "(2,3)");
    assertThat(graph.nodes()).doesNotContain("(3,3)");
    assertThat(graph.successors("(0,0)")).containsExactly("(0,1)", "(1,0)", "(1,1)");
    assertThat(graph.successors("(1,1)"))
        .containsExactly("(0,0)", "(0,1)", "(0,2)", "(1,0)", "(1,2)", "(2,0)", "(2,1)", "(2,2)");
  }
}