package net.dixq.unlimiteddiary.common;

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
