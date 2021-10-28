package solar.rpg.javuno.server.models;

import org.jetbrains.annotations.NotNull;
import solar.rpg.javuno.models.packets.common.AbstractJavunoPlayerPacket;
import solar.rpg.javuno.models.packets.common.IJavunoDistributePacket;
import solar.rpg.javuno.models.packets.common.IJavunoTimeLimitedPacket;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutChatMessage;
import solar.rpg.javuno.models.packets.in.JavunoPacketInOutPlayerReadyChanged;
import solar.rpg.javuno.models.packets.in.JavunoPacketInServerConnect;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionAccepted;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutConnectionRejected;
import solar.rpg.javuno.models.packets.out.JavunoPacketOutPlayerConnect;
import solar.rpg.javuno.mvc.JMVC;
import solar.rpg.javuno.server.controllers.HostController;
import solar.rpg.javuno.server.controllers.ServerGameController;
import solar.rpg.javuno.server.views.MainFrame;
import solar.rpg.jserver.connection.handlers.packet.JServerHost;
import solar.rpg.jserver.packet.JServerPacket;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * {@code JavunoServerPacketValidatorHandler} is a delegate class of {@link ServerGameController} and has access to
 * its {@code JMVC} object to make the appropriate controllers.
 * Using this it is able to validate that any received packet contains valid data, from a valid source.
 * If an error is found, {@link JavunoBadPacketException} is thrown; this can be used to relay information back
 * to the origin address. Some more serious violations may result in a game kick.
 *
 * @author jskinner
 * @since 1.0.0
 */
public final class JavunoServerPacketValidatorHandler {

    @NotNull
    private final JMVC<MainFrame, ServerGameController> mvc;
    @NotNull
    private final HashMap<Class<? extends JServerPacket>, Long> lastReceivedTimeMap;

    public JavunoServerPacketValidatorHandler(@NotNull JMVC<MainFrame, ServerGameController> mvc) {
        this.mvc = mvc;
        lastReceivedTimeMap = new HashMap<>();
    }

    public void handlePacket(@NotNull JServerPacket packet) throws JavunoBadPacketException {
        if (packet instanceof IJavunoTimeLimitedPacket) validateTimeLimitedPacket()
        if (packet instanceof AbstractJavunoPlayerPacket playerPacket) validatePlayerPacket(playerPacket);

        if (packet instanceof JavunoPacketInOutChatMessage chatPacket) handleChatPacket(chatPacket);
        else if (packet instanceof JavunoPacketInServerConnect connectPacket) handleConnectPacket(connectPacket);
        else if (packet instanceof JavunoPacketInOutPlayerReadyChanged readyChangedPacket)
            handlePlayerReadyChanged(readyChangedPacket);
        else if (!(packet instanceof IJavunoDistributePacket))
            throw new JavunoBadPacketException(
                    String.format("Unexpected packet type %s", packet.getClass().getSimpleName()),
                    true);

        if (packet instanceof IJavunoDistributePacket) redistributePacket(packet);
    }

    public void validateTimeLimitedPacket(@NotNull JServerPacket timeLimitedPacket) {
        if (!(timeLimitedPacket instanceof IJavunoTimeLimitedPacket))
            throw new IllegalArgumentException("Packet is not time limited");

        Class<? extends JServerPacket> packetClass = timeLimitedPacket.getClass();
        if (lastReceivedTimeMap.containsKey(packetClass) ||
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastReceivedTimeMap.get(packetClass))
    }

    /**
     * All instances of {@link AbstractJavunoPlayerPacket} must be checked to ensure that the player associated with the
     * origin address also matches the player in the packet data.
     *
     * @param playerPacket The player packet to validate.
     * @throws JavunoBadPacketException Player name in packet did not match origin address associated player.
     * @see AbstractJavunoPlayerPacket#getPlayerName()
     */
    private void validatePlayerPacket(@NotNull AbstractJavunoPlayerPacket playerPacket) throws JavunoBadPacketException {
        try {
            InetSocketAddress originAddress = getModel().getOriginAddress(playerPacket.getPlayerName());
            if (!originAddress.equals(playerPacket.getOriginAddress()))
                throw new IllegalArgumentException(
                        String.format("This packet was not expected from %s", playerPacket.getOriginAddress()));
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new JavunoBadPacketException(
                    String.format("Unable to process player packet: %s", e.getMessage()),
                    e instanceof IllegalArgumentException);
        }
    }

    /**
     * Distributes an incoming chat packet back out to all clients, except for the sender.
     *
     * @param distributePacket Chat packet to distribute.
     */
    private void redistributePacket(@NotNull JServerPacket distributePacket) {
        if (!(distributePacket instanceof IJavunoDistributePacket))
            throw new IllegalArgumentException("Packet is not distributable");

        getHostController().getServerHost().writePacketAllExcept(distributePacket, distributePacket.getOriginAddress());
    }

    private void handleConnectPacket(@NotNull JavunoPacketInServerConnect connectPacket) {
        HostController hostController = getHostController();
        JServerHost serverHost = hostController.getServerHost();
        String serverPassword = hostController.getServerPassword();
        String playerName = connectPacket.getWantedPlayerName();
        InetSocketAddress originAddress = connectPacket.getOriginAddress();

        boolean closeSocket = false;
        JServerPacket packetToWrite;
        if (!serverPassword.isEmpty() && !serverPassword.equals(connectPacket.getServerPassword())) {
            packetToWrite = new JavunoPacketOutConnectionRejected(JavunoPacketOutConnectionRejected.ConnectionRejectionReason.INCORRECT_PASSWORD);
            closeSocket = true;
        } else if (getModel().doesPlayerExist(playerName)) {
            packetToWrite = new JavunoPacketOutConnectionRejected(JavunoPacketOutConnectionRejected.ConnectionRejectionReason.USERNAME_ALREADY_TAKEN);
            closeSocket = true;
        } else {
            getModel().addPlayer(playerName, originAddress);
            packetToWrite = new JavunoPacketOutConnectionAccepted(getModel().getLobbyPlayerNames(),
                                                                  getModel().getReadyPlayerNames());
            serverHost.writePacketAllExcept(new JavunoPacketOutPlayerConnect(playerName), originAddress);
        }

        serverHost.writePacket(connectPacket.getOriginAddress(), packetToWrite);
        if (closeSocket) serverHost.closeSocket(originAddress);
    }

    private void handlePlayerReadyChanged(
            @NotNull JavunoPacketInOutPlayerReadyChanged readyChangedPacket) throws JavunoBadPacketException {
        String playerName = readyChangedPacket.getPlayerName();
        if (getModel().isPlayerReady(playerName) == readyChangedPacket.isReady())
            throw new JavunoBadPacketException(
                    String.format("Player %s is already in the specified ready state", playerName),
                    true);

        if (readyChangedPacket.isReady())
            getModel().markPlayerReady(playerName);
        else
            getModel().unmarkPlayerReady(playerName);
    }

    private void handleChatPacket(@NotNull JavunoPacketInOutChatMessage chatPacket) throws JavunoBadPacketException {
        try {
            InetSocketAddress originAddress = getModel().getOriginAddress(chatPacket.getSenderName());
            if (!originAddress.equals(chatPacket.getOriginAddress()))
                throw new IllegalArgumentException(
                        String.format("This packet was not expected from %s", chatPacket.getOriginAddress()));
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new JavunoBadPacketException(
                    String.format("Unable to process chat packet: %s", e.getMessage()),
                    false);
        }
    }

    @NotNull
    private ServerGameLobbyModel getModel() {
        return mvc.getController().getGameLobbyModel();
    }

    @NotNull
    private HostController getHostController() {
        return mvc.getView().getMVC().getController().getHostController();
    }
}
