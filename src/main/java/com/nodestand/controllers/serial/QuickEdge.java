package com.nodestand.controllers.serial;

public class QuickEdge {
    private long start;
    private long end;

    public QuickEdge(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
