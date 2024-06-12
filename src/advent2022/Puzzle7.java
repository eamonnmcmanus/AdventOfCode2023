package advent2022;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle7 {
  private static final String SAMPLE = """
      $ cd /
      $ ls
      dir a
      14848514 b.txt
      8504156 c.dat
      dir d
      $ cd a
      $ ls
      dir e
      29116 f
      2557 g
      62596 h.lst
      $ cd e
      $ ls
      584 i
      $ cd ..
      $ cd ..
      $ cd d
      $ ls
      4060174 j
      8033020 d.log
      5626152 d.ext
      7214296 k
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle7.class.getResourceAsStream("puzzle7.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        DirNode root = buildHierarchy(lines);
        List<Long> sizes = allSizes(root);
        long smallSum = sizes.stream().mapToLong(x -> x).filter(x -> x <= 100_000).sum();
        System.out.println("For " + name + ", small sum is " + smallSum);
        long free = 70_000_000 - totalSize(root);
        long needed = 30_000_000 - free;
        long bigSum = sizes.stream().mapToLong(x -> x).filter(x -> x >= needed).min().getAsLong();
        System.out.println("For " + name + ", big sum is " + bigSum);
      }
    }
  }

  // I screwed up the recursive calculation for Part 1 somehow and wasted a lot of time before
  // eventually just doing this stupid thing of making a big list of all the sizes and using that.

  private static List<Long> allSizes(DirNode root) {
    List<Long> sizes = new ArrayList<>(List.of(totalSize(root)));
    for (var node : root.contents.values()) {
      if (node instanceof DirNode dir) {
        sizes.addAll(allSizes(dir));
      }
    }
    return sizes;
  }

  private static long totalSize(DirNode root) {
    return root.contents.values().stream().mapToLong(
        node -> switch (node) {
          case DirNode dir -> totalSize(dir);
          case FileNode file -> file.size;
        })
        .sum();
  }

  private static DirNode buildHierarchy(List<String> lines) {
    DirNode root = new DirNode(null);
    DirNode current = root;
    Pattern fileEntryPattern = Pattern.compile("(\\d+) (.*)");
    for (String line : lines) {
      if (line.equals("$ cd /")) {
        current = root;
      } else if (line.equals("$ cd ..")) {
        current = current.parent;
      } else if (line.equals("$ ls")) {
      } else if (line.startsWith("$ cd ")) {
        String newDir = line.substring(5);
        current = (DirNode) current.contents.get(newDir);
      } else if (line.startsWith("dir ")) {
        String dirName = line.substring(4);
        if (current.contents.containsKey(dirName)) {
          System.out.println("Already saw dir " + dirName);
        } else {
          current.contents.put(dirName, new DirNode(current));
        }
      } else {
        Matcher matcher = fileEntryPattern.matcher(line);
        checkState(matcher.matches(), line);
        int size = Integer.parseInt(matcher.group(1));
        String fileName = matcher.group(2);
        if (current.contents.containsKey(fileName)) {
          System.out.println("Already saw file " + fileName + ";");
        } else {
          current.contents.put(fileName, new FileNode(size));
        }
      }
    }
    return root;
  }

  sealed interface FsNode {}

  static final class DirNode implements FsNode {
    final DirNode parent;
    final Map<String, FsNode> contents = new TreeMap<>();

    DirNode(DirNode parent) {
      this.parent = parent;
    }
  }

  static final class FileNode implements FsNode {
    final int size;

    FileNode(int size) {
      this.size = size;
    }
  }
}
