package mx.kenzie.esrs;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

public class HazyQueue extends ConcurrentLinkedDeque<URI> {

    private final Random random = ThreadLocalRandom.current();

    public URI getAny() {
        if (this.flip()) return this.getFirst();
        return this.getLast();
    }

    public void pushAny(URI uri) {
        if (this.flip()) this.addFirst(uri);
        else this.addLast(uri);
    }

    private boolean flip() {
        return random.nextBoolean();
    }

}
