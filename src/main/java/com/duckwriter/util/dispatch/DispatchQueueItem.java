package com.duckwriter.util.dispatch;

final class DispatchQueueItem extends Object {

    final Runnable task;
    DispatchQueueItem next;

    DispatchQueueItem(Runnable task) {
        super();
        this.task = task;
        this.next = null;
    }

}
