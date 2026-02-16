package org.example;

import java.util.Comparator;
import java.util.List;

/*
Name: Christian Evans
Course: CNT 4714 Spring 2026
Assignment title: Project 2 – Multi-threaded programming in Java
Date: February 15, 2026
Class: Enterprise Computing
Description: Prints the final status report.
*/

public class ReportPrinter {
    public static void print(List<TrainStatus> statuses) {
        System.out.println();
        System.out.println("FINAL STATUS REPORT");
        System.out.printf("%-10s %-13s %-14s %-8s %-8s %-8s %-12s %-14s %-9s%n",
                "Train", "Inbound", "Outbound", "S1", "S2", "S3", "Hold", "Dispatched", "Sequence");

        statuses.stream()
                .sorted(Comparator.comparingInt(ts -> ts.trainId))
                .forEach(ts -> System.out.printf("%-10d %-13d %-14d %-8d %-8d %-8d %-12b %-14b %-9d%n",
                        ts.trainId, ts.inbound, ts.outbound, ts.switch1, ts.switch2, ts.switch3,
                        ts.onHold, ts.dispatched, ts.dispatchSequence));
    }
}
