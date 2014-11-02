package com.duckwriter.util.dispatch;

final class DispatchQueueItem extends Object {

    final Runnable task;
    DispatchQueueItem next;

    public DispatchQueueItem(Runnable task) {
        super();
        this.task = task;
        this.next = null;
    }

}
