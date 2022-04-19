package udpdemo;


public class ThreadUdp extends java.lang.Thread {
    private String threadName;
    protected int timeout;
    public ThreadUdp(String nome){
        threadName = nome;
    }
    public void run(){
        timeout = 0;
        try {
            Thread.sleep(10000);
            timeout=1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
