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
        if (clientConnection == null) throw new IllegalStateException("There is no active connection");
        return clientConnection;
    }

    public void onConnectionAccepted() {
        if (currentPendingConnection == null) throw new IllegalStateException("There is no pending connection");
        if (getClientConnection().accepted.get()) throw new IllegalStateException("Connection already accepted");
        getClientConnection().accepted.set(true);
        currentPendingConnection.complete(null);
        currentPendingConnection = null;
    }

    public void onConnectionRejected() {
        if (currentPendingConnection == null) throw new IllegalStateException("There is no pending connection");
        currentPendingConnection.cancel(true);
        currentPendingConnection = null;
        close();
    }

    public void tryConnect(
            @NotNull String ipAddress,
            int port,
            @NotNull String username,
            @NotNull String serverPassword) {
        if (currentPendingConnection != null) throw new IllegalStateException("There is already a pending connection");

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
                if (!pendingConnection.isCancelled()) {
                    logger.log(Level.FINE,
                               String.format("Connection established with Javuno server %s:%s", ipAddress, port));
                    this.clientConnection = clientConnection;
                }
            } catch (IOException e) {
                if (pendingConnection.isCancelled()) return;

                logger.log(
                        Level.INFO,
                        String.format("Could not establish connection to Javuno server %s:%s", ipAddress, port));

                SwingUtilities.invokeLater(() -> mvc.getView().onConnectionFailed(e.getMessage()));
                if (pendingConnection.equals(currentPendingConnection))
                    cancelPendingConnect();
                else pendingConnection.cancel(true);
            }
        });
    }

    public void cancelPendingConnect() {
        if (currentPendingConnection == null) throw new IllegalStateException("There is no pending connection");

        executor.execute(() -> {
            if (currentPendingConnection.isDone() || currentPendingConnection.isCancelled())
                throw new IllegalStateException("Pending connection already complete/cancelled");
            currentPendingConnection.cancel(true);
            currentPendingConnection = null;
            close();
        });
    }

    public boolean isValid() {
        return clientConnection != null && clientConnection.accepted.get();
    }

    public void close() {
        if (clientConnection != null) {
            if (getClientConnection().isClosed()) throw new IllegalStateException("Connection is already closed");
            getClientConnection().close();
        }
        clientConnection = null;
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
            boolean isValid = isValid();
            clientConnection = null;
            SwingUtilities.invokeLater(() -> {
                mvc.getViewInformation().onDisconnected();
                mvc.getView().onDisconnected(isValid);
                mvc.getAppController().getMVC().getView().showView(MainFrame.ViewType.SERVER_CONNECT);
            });
        }

        @Override
        public void onPacketReceived(@NotNull JServerPacket packet) {
            mvc.getAppController().getGameController().handleGamePacket(packet);
        }
    }
}
