package solar.rpg.javuno.models.packets;

public interface IJavunoDistributedPacket {

    /**
     * @return True, if this packet should also be distributed back to the sender.
     */
    boolean distributeToSender();
}
