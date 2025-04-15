package mx.kenzie.esrs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mx.kenzie.hypertext.PageWriter;
import mx.kenzie.hypertext.element.Page;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static mx.kenzie.hypertext.element.StandardElements.*;

public class WebProxy implements HttpHandler {

    protected final boolean protectedRoute;
    protected final Function<InetAddress, Boolean> accessRule;
    protected final URI internalRoute;

    public WebProxy(boolean protectedRoute, Function<InetAddress, Boolean> accessRule, URI internalRoute) {
        this.protectedRoute = protectedRoute;
        this.accessRule = accessRule;
        this.internalRoute = internalRoute;
    }

    private URI getInternalRoute() {
        if (internalRoute != null)
            return internalRoute;
        return Main.backends.getAny();
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (protectedRoute) {
                InetAddress address = exchange.getRemoteAddress().getAddress();
                exchange.getRemoteAddress();
                if (!accessRule.apply(address)) {
                    exchange.sendResponseHeaders(403, -1);
                    try (OutputStream stream = exchange.getResponseBody();
                         PageWriter writer = new PageWriter(stream)) {
                        writer.write(error403);
                        stream.flush();
                    }
                    return;
                }
            }
            URI target = exchange.getRequestURI().relativize(this.getInternalRoute());

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                    .headers(exchange.getRequestHeaders().entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                        .toArray(String[]::new))
                    .uri(target)
                    .build();
                HttpResponse<InputStream> send = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream response = send.body()) {
                    response.transferTo(exchange.getResponseBody());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static final Page error403 = new Page(DOCTYPE_HTML,
        HTML.child(
            HEAD.child(
                TITLE.write("403: Forbidden"),
                META.set("name", "description").set("content", "No access.")
            ),
            BODY.child(
                H1.write("Error 403"),
                P.write("You do not have permission to access this resource.")
            )
        )
    );

}
