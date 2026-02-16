package org.example;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/*
Name: Christian Evans
Course: CNT 4714 Spring 2026
Assignment title: Project 2 – Multi-threaded programming in Java
Date: February 15, 2026
Class: Enterprise Computing
Description: A Train runnable that attempts to acquire its required switches in order and move through the yard.
*/

public class Train implements Runnable {
    private static final ReentrantLock MOVEMENT_LOCK = new ReentrantLock(true);
    private final int id;
    private final int inbound;
    private final int outbound;
    private final YardConfig.Route route;
    private final SwitchBoard switches;
    private final List<TrainStatus> statusSink;
    private final AtomicInteger dispatchCounter;
    private final Random random = new Random();

    public Train(int id, int inbound, int outbound, YardConfig.Route route,
                 SwitchBoard switches, List<TrainStatus> statusSink, AtomicInteger dispatchCounter) {
        this.id = id;
        this.inbound = inbound;
        this.outbound = outbound;
        this.route = route;
        this.switches = switches;
        this.statusSink = statusSink;
        this.dispatchCounter = dispatchCounter;
    }

    @Override
    public void run() {
        if (route == null) {
            System.out.println("*************");
            System.out.printf("Train %d is on permanent hold and cannot be dispatched.%n", id);
            System.out.println("*************");
            statusSink.add(new TrainStatus(id, inbound, outbound, 0, 0, 0, true, false, 0));
            return;
        }

        boolean dispatched = false;
        while (!dispatched) {
            boolean got1 = switches.tryLock(route.switch1);
            if (!got1) {
                System.out.printf("Train %d: UNABLE TO LOCK first required switch: Switch %d. Train will wait...%n", id, route.switch1);
                waitRandom();
                continue;
            }
            System.out.printf("Train %d: HOLDS LOCK on Switch %d.%n", id, route.switch1);

            boolean got2 = switches.tryLock(route.switch2);
            if (!got2) {
                System.out.printf("Train %d: UNABLE TO LOCK second required switch: Switch %d.%n", id, route.switch2);
                System.out.printf("Train %d: Releasing lock on first required switch: Switch %d. Train will wait...%n", id, route.switch1);
                switches.unlock(route.switch1);
                waitRandom();
                continue;
            }
            System.out.printf("Train %d: HOLDS LOCK on Switch %d.%n", id, route.switch2);

            boolean got3 = switches.tryLock(route.switch3);
            if (!got3) {
                System.out.printf("Train %d: UNABLE TO LOCK third required switch: Switch %d.%n", id, route.switch3);
                System.out.printf("Train %d: Releasing locks on first and second required switches: Switch %d and Switch %d. Train will wait...%n",
                        id, route.switch1, route.switch2);
                switches.unlock(route.switch2);
                switches.unlock(route.switch1);
                waitRandom();
                continue;
            }
            System.out.printf("Train %d: HOLDS LOCK on Switch %d.%n", id, route.switch3);
            System.out.printf("Train %d: HOLDS ALL NEEDED SWITCH LOCKS - Train movement begins.%n", id);

            MOVEMENT_LOCK.lock();
            try {
                simulateMovement();
            } finally {
                MOVEMENT_LOCK.unlock();
            }

            System.out.printf("Train %d: Clear of yard control.%n", id);
            System.out.printf("Train %d: Releasing all switch locks.%n", id);
            switches.unlock(route.switch3);
            System.out.printf("Train %d: Unlocks/releases lock on Switch %d.%n", id, route.switch3);
            switches.unlock(route.switch2);
            System.out.printf("Train %d: Unlocks/releases lock on Switch %d.%n", id, route.switch2);
            switches.unlock(route.switch1);
            System.out.printf("Train %d: Unlocks/releases lock on Switch %d.%n", id, route.switch1);
            System.out.printf("Train %d: Has been dispatched and moves on down the line out of yard control into CTC.%n", id);
            System.out.printf("@@@ TRAIN %d: DISPATCHED @@@%n", id);

            int sequence = dispatchCounter.incrementAndGet();
            statusSink.add(new TrainStatus(id, inbound, outbound, route.switch1, route.switch2, route.switch3,
                    false, true, sequence));
            dispatched = true;
        }
    }

    private void waitRandom() {
        try {
            Thread.sleep(100 + random.nextInt(400));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateMovement() {
        try {
            Thread.sleep(300 + random.nextInt(700));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
