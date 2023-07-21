package pbouda.websocket.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        WebsocketServer server = new WebsocketServer();
        server.start()
                .closeFuture()
                .addListener(f -> LOG.info("Websocket closed"))
                .syncUninterruptibly();
    }
}