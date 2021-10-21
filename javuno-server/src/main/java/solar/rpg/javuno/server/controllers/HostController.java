package solar.rpg.javuno.server.controllers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import solar.rpg.javuno.mvc.IController;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.views.MainFrame;
import solar.rpg.jserver.connection.handlers.packet.JServerHost;
import solar.rpg.jserver.packet.JServerPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HostController implements IController {

    @NotNull
    private final Logger logger;
    @NotNull
    private final JMVC<MainFrame, HostController> mvc;
    @NotNull
    private final ExecutorService executor;
    @Nullable
    private JavunoServerHost serverHost;
    @NotNull
    private String serverPassword;

    public HostController(@NotNull Logger logger) {
        this.logger = logger;
        mvc = new JMVC<>();
        executor = Executors.newCachedThreadPool();
        serverPassword = "";
    }

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

    @NotNull
    public JavunoServerHost getServerHost() {
        assert serverHost != null : "Expected active server";
        return serverHost;
    }

    @NotNull
    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(@NotNull String serverPassword) {
        this.serverPassword = serverPassword;
    }

    @Override
    public JMVC<MainFrame, HostController> getMVC() {
        return mvc;
    }

    public final class JavunoServerHost extends JServerHost {

        public JavunoServerHost(
                @NotNull InetAddress bindAddr,
                int port,
                @NotNull ExecutorService executor,
                @NotNull Logger logger) throws IOException {
            super(bindAddr, port, executor, logger);
        }

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

            mvc.getView().getMVC().getController().getGameController().handleDisconnect(originAddress);
        }

        @Override
        public void onPacketReceived(@NotNull JServerPacket packet) {
            mvc.getView().getMVC().getController().getGameController().handleGamePacket(packet);
        }
    }
}
