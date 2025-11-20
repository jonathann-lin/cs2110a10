package graph;

import graph.Pathfinding.PathEnd;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static graph.SimpleGraph.*;

/**
 * Uses the `SimpleGraph` class to verify the functionality of the `Pathfinding` class.
 */
public class PathfindingTest {

    /*
     * Text graph format ([weight] is optional):
     * Directed edge: tailLabel -> headLabel [weight]
     * Undirected edge (so two directed edges in both directions): tailLabel -- headLabel [weight]
     */

    // a small, strongly-connected graph consisting of three vertices and four directed edges
    public static final String graph1 = """
            A -> B 2
            A -- C 6
            B -> C 3
            """;

    // Example graph from Prof. Myers's notes
    public static final String graph2 = """
            A -> B 9
            A -> C 14
            A -> D 15
            B -> E 23
            C -> E 17
            C -> D 5
            C -> F 30
            D -> F 20
            D -> G 37
            E -> F 3
            E -> G 20
            F -> G 16""";

    @DisplayName("WHEN we compute the `pathInfo` from a vertex `v`, THEN it includes an correct "
            + "entry for each vertex `w` reachable along a non-backtracking path from `v`.")
    @Nested
    class pathInfoTest {

        // Recall that "strongly connected" describes a graph that includes a (directed) path from
        // any vertex to any other vertex
        @DisplayName("In a strongly connected graph with no `previousEdge`.")
        @Test
        void testStronglyConnectedNoPrevious() {
            SimpleGraph g = SimpleGraph.fromText(graph1);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            // compute paths from source vertex "A"
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(0, paths.get(va).distance());
            // since the shortest path A -> A is empty, we can't assert anything about its last edge
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "B"
            paths = Pathfinding.pathInfo(vb, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(9, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(0, paths.get(vb).distance());
            assertEquals(3, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "C"
            paths = Pathfinding.pathInfo(vc, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(6, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(8, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(0, paths.get(vc).distance());
        }

        @DisplayName("In a graph that is *not* strongly connected and `pathInfo` is computed "
                + "starting from a vertex that cannot reach all other vertices.")
        @Test
        void testNotStronglyConnected() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(1, paths.size()); // only va is reachable
            assertTrue(paths.containsKey(va));
            assertFalse(paths.containsKey(vb));
        }

        @DisplayName("In a strongly connected graph with a `previousEdge` that prevents some vertex"
                + "from being reached.")
        @Test
        void testStronglyConnectedPreviousPreventsReaching() {
            SimpleGraph g = SimpleGraph.fromText("""
            A -- B 2
            A -- C 6
            B -> C 3
            B -- D 1
            D -- E 1
            """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            /**
             * Normally, going from A to C is 5 long, (A->B->C), but if (B->A) is the previous edge,
             * then it cannot backtrack and go A->B, so it must go A->C, which is a longer path
             */
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, g.getEdge(vb, va));

            assertEquals(2, paths.size()); // B and its connections are unreachable
            assertEquals(0, paths.get(va).distance());
            // since the shortest path A -> A is empty, we can't assert anything about its last edge

            assertEquals(6, paths.get(vc).distance());
            assertEquals(g.getEdge(va, vc), paths.get(vc).lastEdge());
        }

        @DisplayName("In a graph where the shortest path with backtracking is shorter than the "
                + "shortest non-backtracking path.")
        @Test
        void testBacktrackingShorter() {
            SimpleGraph g = SimpleGraph.fromText("""
            A -- B 2
            A -- C 6
            B -- C 3
            B -- D 1
            D -- E 1
            F -> D 1
            C -> E 6
            C -> D 5
            """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");
            SimpleVertex ve = g.getVertex("E");

            //Cannot go from (A->B) as first step
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, g.getEdge(vb, va));
            assertEquals(5, paths.size());

            assertEquals(0, paths.get(va).distance());
            // since the shortest path A -> A is empty, we can't assert anything about its last edge
            assertEquals(6, paths.get(vc).distance());
            assertEquals(g.getEdge(va, vc), paths.get(vc).lastEdge());
            assertEquals(9, paths.get(vb).distance());
            assertEquals(g.getEdge(vc, vb), paths.get(vb).lastEdge());
            assertEquals(10, paths.get(vd).distance());
            assertEquals(g.getEdge(vb, vd), paths.get(vd).lastEdge());
            assertEquals(11, paths.get(ve).distance());
            assertEquals(g.getEdge(vd, ve), paths.get(ve).lastEdge());
        }

        @DisplayName("In a graph where some shortest non-backtracking path includes at least 3 edges.")
        @Test
        void testLongerPaths() {
            SimpleGraph g = SimpleGraph.fromText("""
            A -- B 2
            A -- C 6
            B -- C 3
            B -- D 1
            D -- E 1
            F -> D 1
            C -- E 6
            C -- D 5
            """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");
            SimpleVertex ve = g.getVertex("E");
            SimpleVertex vf = g.getVertex("F");

            //Paths from vertex E, if (D->E) is last edge
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(ve, g.getEdge(vd, ve));
            assertEquals(5, paths.size());
            assertEquals(11, paths.get(va).distance());
            assertEquals(g.getEdge(vb, va), paths.get(va).lastEdge());
            assertEquals(6, paths.get(vc).distance());
            assertEquals(g.getEdge(ve, vc), paths.get(vc).lastEdge());
            assertEquals(9, paths.get(vb).distance());
            assertEquals(g.getEdge(vc, vb), paths.get(vb).lastEdge());
            assertEquals(10, paths.get(vd).distance());
            assertEquals(g.getEdge(vb, vd), paths.get(vd).lastEdge());
            assertEquals(0, paths.get(ve).distance());


            //Paths form vertex F
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths1 = Pathfinding.pathInfo(vf, null);
            assertEquals(6, paths1.size());
            assertEquals(4, paths1.get(va).distance());
            assertEquals(g.getEdge(vb, va), paths1.get(va).lastEdge());
            assertEquals(5, paths1.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths1.get(vc).lastEdge());
            assertEquals(2, paths1.get(vb).distance());
            assertEquals(g.getEdge(vd, vb), paths1.get(vb).lastEdge());
            assertEquals(1, paths1.get(vd).distance());
            assertEquals(g.getEdge(vf, vd), paths1.get(vd).lastEdge());
            assertEquals(2, paths1.get(ve).distance());
            assertEquals(g.getEdge(vd, ve), paths1.get(ve).lastEdge());
            assertEquals(0, paths1.get(vf).distance());

        }
    }

    /**
     * Ensures `pathEdges` is a well-formed path: the `dst` of each edge equals the `src` of the
     * subsequent edge, and that the ordered list of all vertices in the path equals
     * `expectedVertices`. Requires `path` is non-empty.
     */
    private void assertPathVertices(List<String> expectedVertices, List<SimpleEdge> pathEdges) {
        ArrayList<String> pathVertices = new ArrayList<>();
        pathVertices.add(pathEdges.getFirst().tail().label);
        for (SimpleEdge e : pathEdges) {
            assertEquals(pathVertices.getLast(), e.tail().label);
            pathVertices.add(e.head().label);
        }
        assertIterableEquals(expectedVertices, pathVertices);
    }

    @DisplayName("WHEN a weighted, directed graph is given, THEN `shortestNonBacktracking` returns"
            + "the list of edges in the shortest non-backtracking path from a `src` vertex to a "
            + "`dst` vertex, or null if no such path exists.")
    @Nested
    class testShortestNonBacktrackingPath {

        @DisplayName("When the shortest non-backtracking path consists of multiple edges.")
        @Test
        void testLongPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("G"), null);
            assertNotNull(path);
            assertPathVertices(Arrays.asList("A", "C", "E", "F", "G"), path);


        }

        @DisplayName("When the shortest non-backtracking path consists of a single edge.")
        @Test
        void testOneEdgePath() {
            SimpleGraph g = SimpleGraph.fromText("""
            A -- B 2
            A -- C 6
            B -> C 3
            B -- D 1
            D -- E 1
            """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            /**
             * Normally, going from A to C is 5 long, (A->B->C), but if (B->A) is the previous edge,
             * then it cannot backtrack and go A->B, so it must go A->C, which is a longer path
             */
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va,
                    vc, g.getEdge(vb, va));
            assertNotNull(path);
            assertPathVertices(Arrays.asList("A", "C"), path);
        }

        @DisplayName("Path is empty when `src` and `dst` are the same.")
        @Test
        void testEmptyPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("G"),
                    g.getVertex("G"), g.getEdge(g.getVertex("D"),
                            g.getVertex("G")) );
            assertNotNull(path);
            assertTrue(path.isEmpty());

            path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("G"),
                    g.getVertex("G"), null);
            assertNotNull(path);
            assertTrue(path.isEmpty());
            //Does check to make sure that this holds true regardless of any previous edge
        }

        @DisplayName("Path is null when there is not a path from `src` to `dst` (even without "
                + "accounting for back-tracking.")
        @Test
        void testNoPath() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("B"), null);
            assertNull(path);
        }

        @DisplayName("Path is null when the non-backtracking condition prevents finding a path "
                + "from `src` to `dst`.")
        @Test
        void testNonBacktrackingPreventsPath() {
            SimpleGraph g = SimpleGraph.fromText("""
            A -- B 2
            A -- C 6
            B -> C 3
            B -- D 1
            D -- E 1
            """);

            //Only path from A->B is directly, but without backtracking, there is no valid path
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("B"), g.getEdge(g.getVertex("B"), g.getVertex("A")));
            assertNull(path);

            //Included as sanity check to make sure that it is the no-backtracking condition that
            //prevents a valid path
            List<SimpleEdge> path1 = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("B"),null);
            assertNotNull(path1);
        }

        @DisplayName("When the graph includes multiple shortest paths from `src` to `dst`, one of "
                + "them is returned")
        @Test
        void testMultipleShortestPaths() {
            SimpleGraph g = SimpleGraph.fromText("""
            A -> B 2
            B -> C 6
            A -> D 3
            D -> C 5
            A -> C 8
            """);

            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("C"), null );
            assertNotNull(path);
            assertPathVertices(Arrays.asList("A", "C"), path);

            List<List<String>> valid = Arrays.asList(
                            Arrays.asList("A", "C"),
                            Arrays.asList("A", "B", "C"),
                            Arrays.asList("A", "D", "C")
                    );
            boolean matched = false;
            for (List<String> candidate : valid) {
                try {
                    assertPathVertices(candidate, path);
                    matched = true;
                    break;
                } catch (AssertionError aError) {
                    //ignore failures
                }
            }
            assertTrue(matched);

        }
    }

}
