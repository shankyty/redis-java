package com.github.shankyty.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HashWheelTimer {
    private final int tickTime;
    private final int numSlots;
    private final List<Queue<Task>> slots;
    private int currentSlot = 0;
    private long lastTickTime = System.currentTimeMillis();

    public interface Task {
        void execute();
    }

    public HashWheelTimer(int tickTime, int numSlots) {
        this.tickTime = tickTime;
        this.numSlots = numSlots;
        slots = new ArrayList<>(numSlots);
        for (int i = 0; i < numSlots; i++) {
            slots.add(new ConcurrentLinkedQueue<>());
        }
    }

    public void addTimer(long expiry, Task task) {
        long currentTime = System.currentTimeMillis();
        long delay = expiry - currentTime;
        int slot = (currentSlot + (int) (delay / tickTime)) % numSlots;
        slots.get(slot).offer(task);
        System.out.println("[" + System.currentTimeMillis() + "] added " + task);
    }

    public void tick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTickTime >= tickTime) {
            int ticks = (int) ((currentTime - lastTickTime) / tickTime);
            for (int i = 0; i < ticks; i++) {
                int tickSlot = (currentSlot + i) % numSlots;
                Queue<Task> currentSlotTasks = slots.get(tickSlot);
                int k  = 0;
                while (!currentSlotTasks.isEmpty()
                && ((ExpiredKeyCleanupTask) currentSlotTasks.peek()).getExpiry() <= currentTime) {
                    ExpiredKeyCleanupTask task = (ExpiredKeyCleanupTask) currentSlotTasks.poll();
                    task.execute();
                    System.out.println("[" + System.currentTimeMillis() + "] cleaned " + task);
                    k++;
                }
                if(k > 0)
                    System.out.println("cleaned up " + k + " tasks");
            }
            currentSlot = (currentSlot + ticks) % numSlots;
            lastTickTime += ((long) ticks) * tickTime;
        }
    }
}