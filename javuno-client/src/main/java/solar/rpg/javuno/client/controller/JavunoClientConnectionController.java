package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import solar.rpg.jserver.connection.handlers.packet.JServerClient;
import solar.rpg.jserver.packet.JServerPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class JavunoClientConnectionController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final ExecutorService executor;

    public JavunoClientConnectionController(
            @NotNull Logger logger) throws IOException {
        this.logger = logger;
        executor = Executors.newFixedThreadPool(8);
    }

    private final class JavunoClientConnection extends JServerClient {

        public JavunoClientConnection(
                @NotNull InetAddress hostAddr,
                int port,
                @NotNull ExecutorService executor,
                @NotNull Logger logger) throws IOException {
            super(hostAddr, port, executor, logger);
        }

        @Override
        public void onNewConnection(@NotNull InetSocketAddress originAddress) {

        }

        @Override
        public void onSocketClosed(@NotNull InetSocketAddress originAddress) {

        }

        @Override
        public void onPacketReceived(@NotNull JServerPacket packet) {

        }
    }
}
