package messages;

import peer.Peer;
import tasks.WakeyTask;

import java.nio.charset.StandardCharsets;

public class WakeyMessage extends Message {
    public WakeyMessage(String protocolVersion, int senderId, byte[] content) {
        super(protocolVersion, "WakeyWakey", senderId, "wakey", -1, 0, content);
    }

    @Override
    public byte[] encode() {
        // Create Header in the specified format
        byte[] header = String.format("%s %s %d \r\n\r\n",
                this.protocolVersion,
                this.messageType,
                this.senderId).getBytes(StandardCharsets.UTF_8);

        // Create Message array
        byte[] message = new byte[header.length + this.body.length];
        System.out.println(this.body.length);

        // Copy Header and Body to Message array
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(this.body, 0, message, header.length, body.length);

        System.out.println("Message length");
        return message;
    }

    @Override
    public void submitTask(Peer peer) {
        // Peer doesn't know what is Wakey Wakey if it isn't enhanced, ignore
        if (!peer.isEnhanced()) return;

        WakeyTask task = new WakeyTask(peer, this);
        peer.submitControlThread(task);
    }
}
