package messages;

import java.nio.charset.StandardCharsets;

public class STORED extends Message{
    STORED(String protocolVersion, int senderId, String fileId, int chunkNo) {
        super(protocolVersion, "STORED", senderId, fileId, chunkNo, 0, new byte[0]);
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
}
