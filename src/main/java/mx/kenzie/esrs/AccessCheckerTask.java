package mx.kenzie.esrs;

import mx.kenzie.clockwork.io.IOQueue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class AccessCheckerTask implements Runnable {

    static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    static final ExecutorService executor = Executors.newCachedThreadPool();
    static final IOQueue queue = new IOQueue();
    private static final int TIME_OUT = 300;


    protected final InetAddress address;
    protected final BiConsumer<Boolean, InetAddress> resultTask;

    public static void scheduleCheckAccess(InetAddress address, int frequency, BiConsumer<Boolean, InetAddress> resultTask) {
        scheduler.scheduleAtFixedRate(new AccessCheckerTask(address, resultTask), frequency, frequency, TimeUnit.SECONDS);
    }

    public AccessCheckerTask(InetAddress address, BiConsumer<Boolean, InetAddress> resultTask) {
        this.address = address;
        this.resultTask = resultTask;
    }

    @Override
    public void run() {
        boolean checked = this.checkRouteAccessible();

        queue.queue(() -> this.resultTask.accept(checked, address));
    }

    private boolean checkRouteAccessible() {
        return ping(address.getHostAddress(), 443)
            || ping(address.getHostAddress(), 80);
    }

    private static boolean ping(String host, int port) {
        new InetSocketAddress(host, port);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), AccessCheckerTask.TIME_OUT);
            return true;
        } catch (IOException _) {
            return false;
        }
    }

}
