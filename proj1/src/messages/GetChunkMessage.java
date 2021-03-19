package messages;

import peer.Peer;

import java.nio.charset.StandardCharsets;

public class GETCHUNK extends Message {
    public GETCHUNK(String protocolVersion, int senderId, String fileId, int chunkNo) {
        super(protocolVersion, "GETCHUNK", senderId, fileId, chunkNo, 0, new byte[0]);
    }

    @Override
    public byte[] encode() {
        return String.format("%s %s %d %s %d \r\n\r\n",
                this.protocolVersion,
                this.messageType,
                this.senderId,
                this.fileId,
                this.chunkNo).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void submitTask(Peer peer) {

    }
}
