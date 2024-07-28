package com.yqs112358;

import java.util.PriorityQueue;

public class TaskScheduler {

    private record DelayedTask(long executeAtTick, Runnable task) implements Comparable<DelayedTask> {
    @Override
        public int compareTo(DelayedTask other) {
            return Long.compare(this.executeAtTick, other.executeAtTick);
        }
    }

    public long tickCount = 0;
    private final PriorityQueue<DelayedTask> taskQueue = new PriorityQueue<>();

    public void tick() {
        ++tickCount;
        while (!taskQueue.isEmpty() && taskQueue.peek().executeAtTick() <= tickCount) {
            DelayedTask task = taskQueue.poll();
            task.task().run();
        }
    }

    public void addDelayedTask(int delayInTicks, Runnable task) {
        long executeAtTick = tickCount + delayInTicks;
        taskQueue.add(new DelayedTask(executeAtTick, task));
    }
}
