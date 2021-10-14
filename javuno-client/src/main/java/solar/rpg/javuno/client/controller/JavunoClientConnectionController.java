package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.views.MainView;
import solar.rpg.javuno.client.views.ServerConnectView;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.jserver.connection.handlers.packet.JServerClient;
import solar.rpg.jserver.packet.JServerPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavunoClientConnectionController implements IController {

    @NotNull
    private final JMVC<ServerConnectView, JavunoClientConnectionController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ExecutorService executor;
    @Nullable
    private JavunoClientConnection clientConnection;

    public JavunoClientConnectionController(@NotNull Logger logger) {
        this.mvc = new JMVC<>();
        this.logger = logger;
        executor = Executors.newFixedThreadPool(8);
    }

    @Override
    public JMVC<ServerConnectView, JavunoClientConnectionController> getMVC() {
        return mvc;
    }

    public void tryConnect(
            @NotNull String ipAddress,
            int port,
            @NotNull String username,
            @NotNull String serverPassword) {
        assert clientConnection == null : "Client connection already established";
        executor.execute(() -> {
            try {
                clientConnection = new JavunoClientConnection(InetAddress.getByName(ipAddress), port, executor, logger);
            } catch (IOException e) {
                logger.log(
                        Level.WARNING,
                        String.format("Could not establish connection to Javuno server %s:%s", ipAddress, port));
                getMVC().getView().showErrorDialog(
                        "Could not establish connection",
                        String.format("Could not establish connection to server %s:%s: %s", ipAddress, port, e.getMessage()));
            } finally {
                getMVC().getView().reset();
            }
        });
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
