These are my solutions to [Advent of Code 2023](https://adventofcode.com/2023).
They are all coded in Java, needing Java 21 and `--enable-preview` (for string templates).

# Index

* [Day 1](https://adventofcode.com/2023/day/1): [solution](src/advent2023/Puzzle1.java) (extracting integers from strings).
* [Day 2](https://adventofcode.com/2023/day/2): [solution](src/advent2023/Puzzle2.java) (possible draws from a bag of coloured cubes).
* [Day 3](https://adventofcode.com/2023/day/3): [solution](src/advent2023/Puzzle3.java) (extracting integers from a grid with adjacent characters).
* [Day 4](https://adventofcode.com/2023/day/4): [solution](src/advent2023/Puzzle4.java) (evaluating scratch cards).
* [Day 5](https://adventofcode.com/2023/day/5): [solution](src/advent2023/Puzzle5.java) (tracing number ranges through a sequence of maps).
* [Day 6](https://adventofcode.com/2023/day/6): [solution](src/advent2023/Puzzle6.java) (timing races).
* [Day 7](https://adventofcode.com/2023/day/7): [solution](src/advent2023/Puzzle7.java) (evaluating poker hands).
* [Day 8](https://adventofcode.com/2023/day/8): [solution](src/advent2023/Puzzle8.java) (sequences of left-right choices through a graph).
* [Day 9](https://adventofcode.com/2023/day/9): [solution](src/advent2023/Puzzle9.java) (successive differences).
* [Day 10](https://adventofcode.com/2023/day/10): [solution](src/advent2023/Puzzle10.java) (length and enclosed area of a path).
* [Day 11](https://adventofcode.com/2023/day/11): [solution](src/advent2023/Puzzle11.java) (cosmic expansion).
* [Day 12](https://adventofcode.com/2023/day/12): [solution](src/advent2023/Puzzle12.java) (matching wildcards against counts).
* [Day 13](https://adventofcode.com/2023/day/13): [solution](src/advent2023/Puzzle13.java) (searching for reflections).
* [Day 14](https://adventofcode.com/2023/day/14): [solution](src/advent2023/Puzzle14.java) (cycles of rolling rocks).
* [Day 15](https://adventofcode.com/2023/day/15): [solution](src/advent2023/Puzzle15.java) (hashing).
* [Day 16](https://adventofcode.com/2023/day/16): [solution](src/advent2023/Puzzle16.java) (tracing a beam with splitting).
* [Day 17](https://adventofcode.com/2023/day/17): [solution](src/advent2023/Puzzle17.java) (least-cost path with constraints).
* [Day 18](https://adventofcode.com/2023/day/18): [solution](src/advent2023/Puzzle18.java) (area enclosed by a path).
* [Day 19](https://adventofcode.com/2023/day/19): [solution](src/advent2023/Puzzle19.java) (DFA with conditions on 4 variables).
* [Day 20](https://adventofcode.com/2023/day/20): [solution](src/advent2023/Puzzle20.java) (cycling behaviour of circuits).
* [Day 21](https://adventofcode.com/2023/day/21): [solution](src/advent2023/Puzzle21.java) (counting paths in a graph).
* [Day 22](https://adventofcode.com/2023/day/22): [solution](src/advent2023/Puzzle22.java) (3D bricks falling vertically).
* [Day 23](https://adventofcode.com/2023/day/23): [solution](src/advent2023/Puzzle23.java) (longest path through a graph).
* [Day 24](https://adventofcode.com/2023/day/24): [solution](src/advent2023/Puzzle24.java) (intersections of 3D lines).
* [Day 25](https://adventofcode.com/2023/day/25): [solution](src/advent2023/Puzzle25.java) (finding a 3-cut in a large graph).

# Acknowledgements

All solutions are entirely the work of
[Éamonn McManus](https://github.com/eamonnmcmanus) except as noted below.

* [Day 17](https://adventofcode.com/2023/day/17) (least-cost path with constraints on moves)

  I gave up before finding the right Dynamic Programming approach. I ended up
  copying my approach from
  [David Brownman](https://advent-of-code.xavd.id/writeups/2023/day/17/).

* [Day 20](https://adventofcode.com/2023/day/20) (pulse propagation)

  I had a strong suspicion of what the right approach might be for Part 2 but
  would have needed to investigate in detail to confirm. Instead I looked online
  and found
  [this description](https://colab.sandbox.google.com/github/derailed-dash/Advent-of-Code/blob/master/src/AoC_2023/Dazbo%27s_Advent_of_Code_2023.ipynb#scrollTo=EFS4IeuPndFb)
  by Dazbo, which confirmed my suspicion. Then solving was straightforward.

* [Day 21](https://adventofcode.com/2023/day/21) (counting paths in an infinite graph)

  I was not really motivated to put in the work for Part 2 so I outright cheated, by copying
  [this solution](https://github.com/ash42/adventofcode/tree/main/adventofcode2023/src/nl/michielgraat/adventofcode2023/day21)
  by Michiel Graat.

* [Day 24](https://adventofcode.com/2023/day/21) (intersections of 3-dimensional lines)

  This curious puzzle was entirely solvable with algebra, the computer only
  serving to solve simultaneous equations. I did the first part myself, but did
  not find the right approach for the second part on my own. The excellent
  [explanation and solution](https://github.com/dirk527/aoc2021/blob/main/src/aoc2023/Day24.java)
  by [@dirk527](https://github.com/dirk527) showed me the right path, but then
  there wasn't much for me to write.
