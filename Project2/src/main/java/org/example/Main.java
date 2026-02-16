package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
Name: Christian Evans
Course: CNT 4714 Spring 2026
Assignment title: Project 2 – Multi-threaded programming in Java
Date: February 15, 2026
Class: Enterprise Computing
Description: Entry point for the multi-threaded train yard simulator.
*/

public class Main {
    private static final int MAX_TRAINS = 30;
    private static final String FLEET_FILE = "theFleetFile.csv";
    private static final String YARD_FILE = "theYardFile.csv";

    public static void main(String[] args) {
        System.out.println("$$$TRAIN MOVEMENT SIMULATION BEGINS$$$");

        try {
            YardConfig yardConfig = YardConfig.load(Path.of(YARD_FILE));

            List<FleetEntry> fleet = loadFleet(Path.of(FLEET_FILE));
            if (fleet.isEmpty()) {
                System.out.println("No trains scheduled. Exiting simulation.");
                return;
            }

            SwitchBoard switchBoard = new SwitchBoard(Math.max(yardConfig.getMaxSwitchId(), 10));
            ExecutorService pool = Executors.newFixedThreadPool(Math.min(MAX_TRAINS, fleet.size()));
            List<TrainStatus> statuses = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger dispatchCounter = new AtomicInteger(0);

            for (FleetEntry entry : fleet) {
                Optional<YardConfig.Route> route = yardConfig.findRoute(entry.inbound, entry.outbound);
                pool.submit(new Train(entry.trainId, entry.inbound, entry.outbound,
                        route.orElse(null), switchBoard, statuses, dispatchCounter));
            }

            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.MINUTES);
            System.out.println("$$$SIMULATION ENDS$$$");
            ReportPrinter.print(statuses);
        } catch (IOException e) {
            System.out.println("Failed to read input files: " + e.getMessage());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.out.println("Simulation interrupted.");
        }
    }

    private static List<FleetEntry> loadFleet(Path file) throws IOException {
        List<FleetEntry> fleet = new ArrayList<>();
        try (var lines = Files.lines(file)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(line -> {
                        String[] tokens = line.split(",");
                        if (tokens.length == 3) {
                            try {
                                int id = Integer.parseInt(tokens[0].trim());
                                int inbound = Integer.parseInt(tokens[1].trim());
                                int outbound = Integer.parseInt(tokens[2].trim());
                                if (fleet.size() < MAX_TRAINS) {
                                    fleet.add(new FleetEntry(id, inbound, outbound));
                                }
                            } catch (NumberFormatException ignored) {
                                // ignore malformed record
                            }
                        }
                    });
        }
        return fleet;
    }

    private record FleetEntry(int trainId, int inbound, int outbound) { }
}
