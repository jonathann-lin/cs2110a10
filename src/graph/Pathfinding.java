package graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pathfinding {

    /**
     * Represents a path ending at `lastEdge.end()` along with its length (distance).
     */
    record PathEnd<E extends WeightedEdge<?>>(double distance, E lastEdge) { }

    /**
     * Returns a list of `E` edges comprising the shortest non-backtracking simple path from vertex
     * `src` to vertex `dst`. A non-backtracking path never contains two consecutive edges between
     * the same two vertices (e.g., v -> w -> v). As a part of this requirement, the first edge in
     * the returned path cannot back-track `previousEdge` (when `previousEdge` is not null). If
     * there is not a non-backtracking path from `src` to `dst`, then null is returned. Requires
     * that if `previousEdge != null` then `previousEdge.head().equals(src)`.
     */
    public static <V extends Vertex<E>, E extends WeightedEdge<V>> List<E> shortestNonBacktrackingPath(
            V src, V dst, E previousEdge) {

        Map<V, PathEnd<E>> paths = pathInfo(src, previousEdge);
        return paths.containsKey(dst) ? pathTo(paths, src, dst) : null;
    }

    /**
     * Returns a map that associates each vertex reachable from `src` along a non-backtracking path
     * with a `PathEnd` object. The `PathEnd` object summarizes relevant information about the
     * shortest non-backtracking simple path from `src` to that vertex. A non-backtracking path
     * never contains two consecutive edges between the same two vertices (e.g., v -> w -> v). As a
     * part of this requirement, the first edge in the returned path cannot backtrack `previousEdge`
     * (when `previousEdge` is not null). Requires that if `previousEdge != null` then
     * `previousEdge.head().equals(src)`.
     */
    static <V extends Vertex<E>, E extends WeightedEdge<V>> Map<V, PathEnd<E>> pathInfo(V src,
            E previousEdge) {

        assert previousEdge == null || previousEdge.head().equals(src);

        // Associate vertex labels with info about the shortest-known path from `start` to that
        // vertex.  Populated as vertices are discovered (not as they are settled).
        Map<V, PathEnd<E>> pathInfo = new HashMap<>();

        // TODO 4a: Complete the implementation of this method according to its specification. Your
        //  implementation should make use of Dijkstra's algorithm (modified to prevent path back-
        //  tracking), creating a `MinPQueue` to manage the "frontier" set of vertices, and settling
        //  the vertices in this frontier in increasing distance order.

        return pathInfo;
    }

    /**
     * Return the list of edges in the shortest non-backtracking path from `src` to `dst`, as
     * summarized by the given `pathInfo` map. Requires `pathInfo` conforms to the specification as
     * documented by the `pathInfo` method; it must contain the last edge on the shortest
     * non-backtracking simple paths from `src` to all reachable vertices.
     */
    static <V, E extends WeightedEdge<V>> List<E> pathTo(Map<V, PathEnd<E>> pathInfo, V src, V dst) {
        // TODO 4b: Complete this implementation of this method according to its specification.
        throw new UnsupportedOperationException();
    }
}
