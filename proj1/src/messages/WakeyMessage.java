package messages;

import peer.Peer;
import tasks.WakeyTask;

import java.nio.charset.StandardCharsets;

public class WakeyMessage extends Message{
    public WakeyMessage(String protocolVersion, int senderId) {
        super(protocolVersion, "WakeyWakey", senderId, "wakey", -1, 0, null);
    }

    @Override
    public byte[] encode() {
        return String.format("%s %s %d \r\n\r\n",
                this.protocolVersion,
                this.messageType,
                this.senderId).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void submitTask(Peer peer) {
        WakeyTask task = new WakeyTask(peer, this);
        peer.submitControlThread(task);
    }
}
