package net.dixq.unlimiteddiary.utils;

public class StopWatch {
    private long _startTime;
    public StopWatch(){
        start();
    }
    public void start(){
        _startTime = System.currentTimeMillis();
    }
    public int getDiff(){
        return (int)(System.currentTimeMillis()-_startTime);
    }
}
