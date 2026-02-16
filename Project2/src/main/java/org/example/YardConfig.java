package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*
Name: Christian Evans
Course: CNT 4714 Spring 2026
Assignment title: Project 2 – Multi-threaded programming in Java
Date: February 15, 2026
Class: Enterprise Computing
Description: Loads yard configuration mapping inbound/outbound tracks to the three required switches.
*/

public class YardConfig {
    public static class Route {
        final int inbound;
        final int switch1;
        final int switch2;
        final int switch3;
        final int outbound;

        public Route(int inbound, int switch1, int switch2, int switch3, int outbound) {
            this.inbound = inbound;
            this.switch1 = switch1;
            this.switch2 = switch2;
            this.switch3 = switch3;
            this.outbound = outbound;
        }
    }

    private final Map<String, Route> routes = new HashMap<>();
    private int maxSwitchId = 0;

    public Optional<Route> findRoute(int inbound, int outbound) {
        return Optional.ofNullable(routes.get(key(inbound, outbound)));
    }

    public int getMaxSwitchId() {
        return maxSwitchId;
    }

    public static YardConfig load(Path path) throws IOException {
        YardConfig config = new YardConfig();
        try (var lines = Files.lines(path)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(config::addRoute);
        }
        return config;
    }

    private void addRoute(String csvLine) {
        String[] tokens = csvLine.split(",");
        if (tokens.length != 5) {
            return; // skip malformed line
        }
        try {
            int inbound = Integer.parseInt(tokens[0].trim());
            int s1 = Integer.parseInt(tokens[1].trim());
            int s2 = Integer.parseInt(tokens[2].trim());
            int s3 = Integer.parseInt(tokens[3].trim());
            int outbound = Integer.parseInt(tokens[4].trim());
            maxSwitchId = Math.max(maxSwitchId, Math.max(s1, Math.max(s2, s3)));
            routes.put(key(inbound, outbound), new Route(inbound, s1, s2, s3, outbound));
        } catch (NumberFormatException ignored) {
            // ignore malformed line
        }
    }

    private String key(int inbound, int outbound) {
        return inbound + "->" + outbound;
    }
}
