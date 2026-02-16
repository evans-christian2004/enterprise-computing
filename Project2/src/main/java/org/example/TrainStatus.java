package org.example;

/*
Name: Christian Evans
Course: CNT 4714 Spring 2026
Assignment title: Project 2 – Multi-threaded programming in Java
Date: February 15, 2026
Class: Enterprise Computing
Description: Immutable snapshot of a train's end-of-run status for the final report.
*/

public class TrainStatus {
    public final int trainId;
    public final int inbound;
    public final int outbound;
    public final int switch1;
    public final int switch2;
    public final int switch3;
    public final boolean onHold;
    public final boolean dispatched;
    public final int dispatchSequence;

    public TrainStatus(int trainId, int inbound, int outbound, int switch1, int switch2, int switch3,
                       boolean onHold, boolean dispatched, int dispatchSequence) {
        this.trainId = trainId;
        this.inbound = inbound;
        this.outbound = outbound;
        this.switch1 = switch1;
        this.switch2 = switch2;
        this.switch3 = switch3;
        this.onHold = onHold;
        this.dispatched = dispatched;
        this.dispatchSequence = dispatchSequence;
    }
}
