package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.views.JavunoClientMVC;
import solar.rpg.javuno.client.views.ServerConnectView;
import solar.rpg.javuno.model.packets.JavunoPacketInServerConnect;
import solar.rpg.javuno.model.packets.JavunoPacketOutConnectionAccepted;
import solar.rpg.javuno.model.packets.JavunoPacketOutConnectionRejected;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.jserver.connection.handlers.packet.JServerClient;
import solar.rpg.jserver.packet.JServerPacket;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavunoClientConnectionController implements IController {

    @NotNull
    private final JavunoClientMVC<ServerConnectView, JavunoClientConnectionController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ExecutorService executor;
    @Nullable
    private JavunoClientConnection clientConnection;
    @Nullable
    private CompletableFuture<Void> currentPendingConnection;

    public JavunoClientConnectionController(@NotNull Logger logger) {
        this.mvc = new JavunoClientMVC<>();
        this.logger = logger;
        executor = Executors.newFixedThreadPool(1);
    }

    @Override
    public JavunoClientMVC<ServerConnectView, JavunoClientConnectionController> getMVC() {
        return mvc;
    }

    public void handleConnectionAccepted(@NotNull JavunoPacketOutConnectionAccepted acceptedPacket) {
        assert clientConnection != null : "Expected established socket connection";
        assert currentPendingConnection != null : "Expected existing pending connection";

        SwingUtilities.invokeLater(() -> {
            getMVC().writeClientEvent(String.format(">> %s", "Connection successful!"));
            getMVC().getView().showErrorDialog("Connection successful!", "It's a success!");
            currentPendingConnection.cancel(true);
        });
    }

    public void handleConnectionRejected(@NotNull JavunoPacketOutConnectionRejected rejectionPacket) {
        assert clientConnection != null : "Expected established socket connection";
        assert currentPendingConnection != null : "Expected existing pending connection";

        SwingUtilities.invokeLater(() -> {
            String errorMsg = "";
            switch (rejectionPacket.getRejectionReason()) {
                case INCORRECT_PASSWORD -> errorMsg = "Incorrect server password.";
                case USERNAME_ALREADY_TAKEN -> errorMsg = "That username is already taken.";
            }

            if (!errorMsg.isEmpty()) {
                getMVC().writeClientEvent(String.format(">> %s", errorMsg));
                getMVC().getView().showErrorDialog("Unable to connect to server", errorMsg);
                getMVC().getView().reset();
                currentPendingConnection.cancel(true);
            }
        });

        clientConnection.close();
    }

    public void tryConnect(
            @NotNull String ipAddress,
            int port,
            @NotNull String username,
            @NotNull String serverPassword) {
        assert clientConnection == null : "Client connection already established";
        mvc.writeClientEvent(String.format(">> Attempting to connect to server at %s:%s", ipAddress, port));
        currentPendingConnection = new CompletableFuture<>();
        final CompletableFuture<Void> pendingConnection = currentPendingConnection;
        executor.execute(() -> {
            try {
                JavunoClientConnection clientConnection = new JavunoClientConnection(
                        InetAddress.getByName(ipAddress),
                        port,
                        username,
                        serverPassword,
                        executor,
                        logger);
                if (!pendingConnection.isCancelled()) this.clientConnection = clientConnection;
            } catch (IOException e) {
                logger.log(
                        Level.WARNING,
                        String.format("Could not establish connection to Javuno server %s:%s", ipAddress, port));

                if (pendingConnection.isCancelled()) return;

                SwingUtilities.invokeLater(() -> getMVC().getView().reset());
                pendingConnection.cancel(true);

                mvc.writeClientEvent(String.format(">> Connection failed: %s", e.getMessage()));
                getMVC().getView().showErrorDialog(
                        "Could not establish connection",
                        String.format("Could not establish connection to server %s:%s: %s",
                                      ipAddress,
                                      port,
                                      e.getMessage()));
            }
        });
    }

    public void cancelPendingConnect() {
        assert currentPendingConnection != null : "Pending connection does not exist";

        executor.execute(() -> {
            assert !currentPendingConnection.isDone() : "Pending connection already complete";
            assert !currentPendingConnection.isCancelled() : "Pending connection already cancelled";
            mvc.writeClientEvent(">> Connection cancelled!");
            currentPendingConnection.cancel(true);
        });
    }

    private final class JavunoClientConnection extends JServerClient {

        @NotNull
        private final String username;
        @NotNull
        private final String serverPassword;

        public JavunoClientConnection(
                @NotNull InetAddress hostAddr,
                int port,
                @NotNull String username,
                @NotNull String serverPassword,
                @NotNull ExecutorService executor,
                @NotNull Logger logger) throws IOException {
            super(hostAddr, port, executor, logger);
            this.username = username;
            this.serverPassword = serverPassword;

            tryConnect();
        }

        @Override
        public void onNewConnection(@NotNull InetSocketAddress originAddress) {
            try {
                JavunoPacketInServerConnect connectPacket = new JavunoPacketInServerConnect(username, serverPassword);
                writePacket(connectPacket);
            } catch (IOException e) {
                getMVC().getView().showErrorDialog("Connection error", "Could not retrieve game data from server");
            }
        }

        @Override
        public void onSocketClosed(@NotNull InetSocketAddress originAddress) {

        }

        @Override
        public void onPacketReceived(@NotNull InetSocketAddress originAddress, @NotNull JServerPacket packet) {
            if (packet instanceof JavunoPacketOutConnectionRejected)
                handleConnectionRejected((JavunoPacketOutConnectionRejected) packet);
            else if (packet instanceof JavunoPacketOutConnectionAccepted)
                handleConnectionAccepted((JavunoPacketOutConnectionAccepted) packet);
        }
    }
}
