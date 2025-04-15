package mx.kenzie.esrs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.function.BiConsumer;

public class Main {

    static final HazyQueue backends = new HazyQueue();

    public static void main(String... args) {

    }

    public static void start(String[] backendAddresses) {
        for (String backendAddress : backendAddresses) {
            AccessCheckerTask.scheduleCheckAccess(InetAddress.getLoopbackAddress(), 200,
                addToProxy(new InetSocketAddress(backendAddress, 443).getAddress()));
        }
    }

    private static URI toURI(InetAddress address) {
        return URI.create("https://" + address.getHostAddress() + "/api/");
    }

    private static BiConsumer<Boolean, InetAddress> addToProxy(InetAddress address) {
        return (z, endpoint) -> {
            URI uri = toURI(endpoint);
            backends.remove(uri);
            if (z) backends.pushAny(uri);
        };
    }

}
