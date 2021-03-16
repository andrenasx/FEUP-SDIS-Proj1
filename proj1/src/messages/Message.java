package messages;

import java.net.DatagramPacket;
import java.util.Arrays;

abstract class Message {
    protected String protocolVersion;
    protected String messageType;
    protected int senderId;
    protected String fileId;
    protected int chunkNo;
    protected int replicationDeg;
    protected byte[] body;

    Message(String protocolVersion, String messageType, int senderId, String fileId, int chunkNo, int replicationDeg, byte[] body) {
        this.protocolVersion = protocolVersion;
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.body = body;
    }

    public static Message create(DatagramPacket packet) throws Exception {
        String message = new String(packet.getData());
        message = message.substring(0, Math.min(packet.getLength(), message.length()));

        // Split Header and Body from <CRLF><CRLF>
        String[] parts = message.split("\r\n\r\n", 2);

        // Get header bytes before removing spaces so we store the real value
        int headerBytes = parts[0].length();

        // Remove trailing, leading and extra spaces between fields and split header fields
        String[] header = parts[0].trim().replaceAll("( )+", " ").split(" ");


        String protocolVersion = header[0];
        String messageType = header[1];
        int senderId = Integer.parseInt(header[2]);
        String fileId = header[3];

        byte[] body = new byte[0];
        if (parts.length == 2)
            body = Arrays.copyOfRange(packet.getData(), headerBytes + 4, packet.getLength());

        // Estes campos podem nao existir
        int chunkNo;
        int replicationDeg;

        switch (messageType) {
            case "PUTCHUNK":
                chunkNo = Integer.parseInt(header[4]);
                replicationDeg = Integer.parseInt(header[5]);
                return new PUTCHUNK(protocolVersion, senderId, fileId, chunkNo, replicationDeg, body);
            case "STORED":
                chunkNo = Integer.parseInt(header[4]);
                return new STORED(protocolVersion, senderId, fileId, chunkNo);
            case "GETCHUNK":
                chunkNo = Integer.parseInt(header[4]);
                return new GETCHUNK(protocolVersion, senderId, fileId, chunkNo);
            case "CHUNK":
                chunkNo = Integer.parseInt(header[4]);
                return new CHUNK(protocolVersion, senderId, fileId, chunkNo, body);
            case "DELETE":
                return new DELETE(protocolVersion, senderId, fileId);
            case "REMOVED":
                chunkNo = Integer.parseInt(header[4]);
                return new REMOVED(protocolVersion, senderId, fileId, chunkNo);
            default:
                throw new Exception("Unknown message type");
        }
    }

    public abstract byte[] encode();
}