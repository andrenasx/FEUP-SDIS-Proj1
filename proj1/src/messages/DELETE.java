package messages;

import java.nio.charset.StandardCharsets;

public class DELETE extends Message{
    DELETE(String protocolVersion, int senderId, String fileId) {
        super(protocolVersion, "DELETE", senderId, fileId, -1, 0, new byte[0]);
    }

    @Override
    public byte[] encode() {
        return String.format("%s %s %d %s \r\n\r\n",
                this.protocolVersion,
                this.messageType,
                this.senderId,
                this.fileId).getBytes(StandardCharsets.UTF_8);
    }
}