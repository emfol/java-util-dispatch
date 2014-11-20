package com.duckwriter.util.dispatch;

final class Task extends Object {

    final Runnable runnable;
    Task next;

    Task(Runnable runnable) {
        super();
        this.runnable = runnable;
        this.next = null;
    }

}
