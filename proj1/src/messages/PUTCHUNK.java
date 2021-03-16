package messages;

import java.nio.charset.StandardCharsets;

public class PUTCHUNK extends Message{
    PUTCHUNK(String protocolVersion, int senderId, String fileId, int chunkNo, int replicationDeg, byte[] body) {
        super(protocolVersion, "PUTCHUNK", senderId, fileId, chunkNo, replicationDeg, body);
    }

    @Override
    public byte[] encode() {
        // Create Header in the specified format
        byte[] header = String.format("%s %s %d %s %d %d \r\n\r\n",
                this.protocolVersion,
                this.messageType,
                this.senderId,
                this.fileId,
                this.chunkNo,
                this.replicationDeg).getBytes(StandardCharsets.UTF_8);

        // Create Message array
        byte[] message = new byte[header.length + this.body.length];

        // Copy Header and Body to Message array
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(this.body, 0, message, header.length, body.length);

        return message;
    }
}
