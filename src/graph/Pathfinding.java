package graph;

import a9.MinPQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pathfinding {

    /**
     * Represents a path ending at `lastEdge.end()` along with its length (distance).
     */
    record PathEnd<E extends WeightedEdge<?>>(double distance, E lastEdge) {

    }

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

        MinPQueue<V> frontier = new MinPQueue<>();
        pathInfo.put(src, new PathEnd<>(0, previousEdge));
        frontier.addOrUpdate(src, 0);

        while (!frontier.isEmpty()) {
            V vertex = frontier.remove(); //takes vertex on frontier with lowest priority (closest)
            PathEnd<E> end = pathInfo.get(vertex); //gets path info of lowest priority vertex

            for (E e : vertex.outgoingEdges()) {
                //If the last edge is not null and it backtracks along the previous edge, then skip it
                if (end.lastEdge() != null && end.lastEdge().tail().equals(e.head())) {
                    continue;
                }

                V neighbor = e.head();
                double newDistance = end.distance + e.weight();

                //If pathInfo does not contain this vertex, or the new distance is shorter than the
                // current distance mapped to this vertex, then add/update the shortest path to this vertex
                if (!pathInfo.containsKey(neighbor)
                        || pathInfo.get(neighbor).distance > newDistance) {
                    pathInfo.put(neighbor, new PathEnd<>(newDistance, e));
                    frontier.addOrUpdate(neighbor, newDistance);
                }

            }
        }

        return pathInfo;
    }

    /**
     * Return the list of edges in the shortest non-backtracking path from `src` to `dst`, as
     * summarized by the given `pathInfo` map. Requires `pathInfo` conforms to the specification as
     * documented by the `pathInfo` method; it must contain the last edge on the shortest
     * non-backtracking simple paths from `src` to all reachable vertices.
     */
    static <V, E extends WeightedEdge<V>> List<E> pathTo(Map<V, PathEnd<E>> pathInfo, V src,
            V dst) {
        List<E> ret = new ArrayList<>();

        V currentV = dst;

        while (!currentV.equals(src)) {
            E prevEdge = pathInfo.get(currentV).lastEdge;
            currentV = prevEdge.tail();
            ret.add(0, prevEdge);
        }
        return ret;

    }
}
