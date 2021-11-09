package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.models.packets.JavunoBadPacketException;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutServerMessage;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.models.JavunoPacketTimeoutException;
import solar.rpg.javuno.server.views.MainFrame;
import solar.rpg.jserver.connection.handlers.packet.JServerHost;
import solar.rpg.jserver.packet.JServerPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller is responsible for maintaining an instance of the Javuno server host that all clients can
 * connect to and correspond with. Packets are sent out through this controller, and incoming packets are sent
 * to the {@link ServerGameController} to be handled.
 *
 * @author jskinner
 * @see ServerGameController
 * @since 1.0.0
 */
public final class HostController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainFrame, HostController> mvc;
    @NotNull
    private final ExecutorService executor;
    @Nullable
    private JavunoServerHost serverHost;

    /**
     * Server password. This is required upon connection if provided.
     */
    @NotNull
    private String serverPassword;

    /**
     * Constructs a new {@code HostController} instance.
     *
     * @param executor Concurrent executor service.
     * @param logger   Logging object.
     */
    public HostController(@NotNull ExecutorService executor, @NotNull Logger logger) {
        this.executor = executor;
        this.logger = logger;
        mvc = new JMVC<>();
        serverPassword = "";
    }

    /**
     * Creates a new {@code JavunoServerHost} instance at the given address and port.
     *
     * @param bindAddr The internet address to bind to.
     * @param port     The port.
     */
    public void startHost(InetAddress bindAddr, int port) {
        assert serverHost == null : "Server host is already active";
        try {
            serverHost = new JavunoServerHost(bindAddr, port, executor, logger);
        } catch (IOException e) {
            getMVC().getView().showErrorDialog(
                    "Unable to establish server host",
                    String.format("Could not establish server host on %s:%s:\n%s", bindAddr, port, e.getMessage()));
            System.exit(0);
        }
    }

    /**
     * @return Active instance of the {@code JavunoServerHost}.
     * @throws IllegalStateException Server host does not yet exist.
     */
    @NotNull
    public JavunoServerHost getServerHost() {
        if (serverHost == null) throw new IllegalStateException("Server is not active");
        return serverHost;
    }

    /**
     * @return The server password.
     */
    @NotNull
    public String getServerPassword() {
        return serverPassword;
    }

    /**
     * Sets the server password.
     *
     * @param serverPassword The new server password.
     */
    public void setServerPassword(@NotNull String serverPassword) {
        this.serverPassword = serverPassword;
    }

    /**
     * @return MVC relationship.
     */
    @Override
    public JMVC<MainFrame, HostController> getMVC() {
        return mvc;
    }

    /**
     * {@code JavunoServerHost} is a delegate class of {@code HostController} that represents the active
     * host listening for incoming connections and packets, as well as sending outgoing packets to specific
     * origin addresses.
     *
     * @author jskinner
     * @since 1.0.0
     */
    public final class JavunoServerHost extends JServerHost {

        /**
         * Constructs a new {@code JavunoServerHost} instance.
         *
         * @param bindAddr The internet address for the server host to bind to.
         * @param port     The server port.
         * @param executor Asynchronous executor service.
         * @param logger   Logging object.
         * @throws IOException I/O exception while creating server host.
         */
        public JavunoServerHost(
                @NotNull InetAddress bindAddr,
                int port,
                @NotNull ExecutorService executor,
                @NotNull Logger logger) throws IOException {
            super(bindAddr, port, executor, logger);
        }

        //TODO: Make the accept/reject functionality generic code.
        @Override
        public void onNewConnection(@NotNull InetSocketAddress originAddress) {
        }

        @Override
        public void onSocketClosed(@NotNull InetSocketAddress originAddress) {
            logger.log(Level.FINER,
                       String.format("Socket closed to player %s (%s)",
                                     mvc.getView().getMVC().getController().getGameController().getGameLobbyModel()
                                             .getPlayerNameWithDefault(originAddress, "N/A"),
                                     originAddress));

            getMVC().getView().getMVC().getController().getGameController().onPlayerDisconnect(originAddress);
        }

        @Override
        public void onPacketReceived(@NotNull JServerPacket packet) {
            try {
                getMVC().getView().getMVC().getController().getGameController().getPacketHandler().handlePacket(packet);
            } catch (JavunoPacketTimeoutException e) {
                getServerHost().writePacket(
                        packet.getOriginAddress(),
                        new JavunoPacketOutServerMessage("You are doing that too quickly! Please slow down."));
            } catch (Exception e) {
                //TODO: Handle packet
                e.printStackTrace();
            }
        }
    }
}
