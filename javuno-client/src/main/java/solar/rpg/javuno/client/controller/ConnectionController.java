package solar.rpg.javuno.client.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.client.mvc.JavunoClientMVC;
import solar.rpg.javuno.client.views.MainFrame;
import solar.rpg.javuno.client.views.ViewServerConnect;
import solar.rpg.javuno.models.packets.JavunoPacketInServerConnect;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionController implements IController {

    @NotNull
    private final JavunoClientMVC<ViewServerConnect, ConnectionController> mvc;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ExecutorService executor;
    @Nullable
    private JavunoClientConnection clientConnection;
    @Nullable
    private CompletableFuture<Void> currentPendingConnection;

    public ConnectionController(@NotNull Logger logger) {
        this.mvc = new JavunoClientMVC<>();
        this.logger = logger;
        executor = Executors.newCachedThreadPool();
    }

    @NotNull
    public JavunoClientConnection getClientConnection() {
        assert clientConnection != null : "Expected active connection";
        return clientConnection;
    }

    public void onConnectionAccepted() {
        assert clientConnection != null : "Expected established socket connection";
        assert currentPendingConnection != null : "Expected existing pending connection";
        assert !clientConnection.accepted.get() : "Existing connection already accepted";
        clientConnection.accepted.set(true);
        currentPendingConnection.cancel(true);
    }

    public void onConnectionRejected() {
        assert clientConnection != null : "Expected established socket connection";
        assert currentPendingConnection != null : "Expected existing pending connection";
        clientConnection.close();
        SwingUtilities.invokeLater(() -> getMVC().getView().setFormEntryEnabled(true));
        currentPendingConnection.cancel(true);
    }

    public void tryConnect(
            @NotNull String ipAddress,
            int port,
            @NotNull String username,
            @NotNull String serverPassword) {
        assert clientConnection == null : "Client connection already established";
        mvc.logClientEvent(String.format("> Attempting to connect to server at %s:%s", ipAddress, port));
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

                SwingUtilities.invokeLater(() -> getMVC().getView().setFormEntryEnabled(true));
                pendingConnection.cancel(true);

                mvc.logClientEvent(String.format("> Connection failed: %s", e.getMessage()));
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
            mvc.logClientEvent(">> Connection cancelled!");
            currentPendingConnection.cancel(true);
        });
    }

    public boolean isValid() {
        return clientConnection != null && clientConnection.accepted.get();
    }

    @Override
    public JavunoClientMVC<ViewServerConnect, ConnectionController> getMVC() {
        return mvc;
    }

    public final class JavunoClientConnection extends JServerClient {

        @NotNull
        private final String username;
        @NotNull
        private final String serverPassword;
        @NotNull
        private final AtomicBoolean accepted;

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
            accepted = new AtomicBoolean(false);

            tryConnect();
        }

        @Override
        public void onNewConnection(@NotNull InetSocketAddress originAddress) {
            JavunoPacketInServerConnect connectPacket = new JavunoPacketInServerConnect(username, serverPassword);
            writePacket(connectPacket);
        }

        @Override
        public void onSocketClosed(@NotNull InetSocketAddress originAddress) {
            assert clientConnection != null : "Expect existing client connection";
            clientConnection = null;
            SwingUtilities.invokeLater(() -> {
                mvc.getViewInformation().onDisconnected();
                mvc.getView().reset();
                mvc.getAppController().getMVC().getView().showView(MainFrame.ViewType.SERVER_CONNECT);
            });
        }

        @Override
        public void onPacketReceived(@NotNull JServerPacket packet) {
            mvc.getAppController().getGameController().handleGamePacket(packet);
        }
    }
}
