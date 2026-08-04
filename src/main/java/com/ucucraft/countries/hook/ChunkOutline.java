package com.ucucraft.countries.hook;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ucucraft.countries.api.ChunkPos;

/** Turns a set of claimed chunks into flat outlines by tracing the boundary of each contiguous group. */
final class ChunkOutline {

    record Loop(String world, double[] xCorners, double[] zCorners) {}

    private record Point(int x, int z) {}
    private record Edge(Point from, Point to) {}

    private ChunkOutline() {}

    static List<Loop> trace(Set<ChunkPos> chunks) {
        List<Loop> loops = new ArrayList<>();
        Set<ChunkPos> remaining = new HashSet<>(chunks);
        while (!remaining.isEmpty()) {
            ChunkPos seed = remaining.iterator().next();
            loops.addAll(traceIsland(floodFill(seed, chunks, remaining)));
        }
        return loops;
    }

    private static Set<ChunkPos> floodFill(ChunkPos seed, Set<ChunkPos> all, Set<ChunkPos> remaining) {
        Set<ChunkPos> island = new HashSet<>();
        Deque<ChunkPos> queue = new ArrayDeque<>();
        queue.add(seed);
        remaining.remove(seed);
        while (!queue.isEmpty()) {
            ChunkPos pos = queue.poll();
            island.add(pos);
            for (ChunkPos neighbour : pos.neighbours()) {
                if (all.contains(neighbour) && remaining.remove(neighbour)) {
                    queue.add(neighbour);
                }
            }
        }
        return island;
    }

    private static List<Loop> traceIsland(Set<ChunkPos> island) {
        String world = island.iterator().next().world();
        Map<Point, List<Edge>> byStart = new HashMap<>();
        for (ChunkPos pos : island) {
            int x = pos.x();
            int z = pos.z();
            Point nw = new Point(x, z);
            Point ne = new Point(x + 1, z);
            Point se = new Point(x + 1, z + 1);
            Point sw = new Point(x, z + 1);
            addIfBoundary(island, pos, 0, -1, new Edge(nw, ne), byStart);
            addIfBoundary(island, pos, 1, 0, new Edge(ne, se), byStart);
            addIfBoundary(island, pos, 0, 1, new Edge(se, sw), byStart);
            addIfBoundary(island, pos, -1, 0, new Edge(sw, nw), byStart);
        }

        List<Loop> loops = new ArrayList<>();
        while (!byStart.isEmpty()) {
            List<Double> xs = new ArrayList<>();
            List<Double> zs = new ArrayList<>();
            Point start = byStart.keySet().iterator().next();
            Point current = start;
            do {
                List<Edge> edges = byStart.get(current);
                Edge edge = edges.remove(edges.size() - 1);
                if (edges.isEmpty()) {
                    byStart.remove(current);
                }
                xs.add(current.x() * 16.0);
                zs.add(current.z() * 16.0);
                current = edge.to();
            } while (!current.equals(start));

            double[] xArr = new double[xs.size()];
            double[] zArr = new double[zs.size()];
            for (int i = 0; i < xArr.length; i++) {
                xArr[i] = xs.get(i);
                zArr[i] = zs.get(i);
            }
            loops.add(new Loop(world, xArr, zArr));
        }
        return loops;
    }

    private static void addIfBoundary(Set<ChunkPos> island, ChunkPos pos, int dx, int dz, Edge edge,
                                       Map<Point, List<Edge>> byStart) {
        ChunkPos neighbour = new ChunkPos(pos.world(), pos.x() + dx, pos.z() + dz);
        if (!island.contains(neighbour)) {
            byStart.computeIfAbsent(edge.from(), k -> new ArrayList<>()).add(edge);
        }
    }
}
