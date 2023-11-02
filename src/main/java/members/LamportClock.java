package members;

class LamportClock {
    int time = 0;
    public LamportClock() {}
    public synchronized void increment() { time++; }
    public synchronized void update(int val) { time = Math.max(time, val) + 1; }
    public synchronized int get() { return time; }

}
