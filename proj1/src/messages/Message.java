package messages;

import peer.Peer;

import java.net.DatagramPacket;
import java.util.Arrays;

public abstract class Message {
    public String protocolVersion;
    protected String messageType;
    public int senderId;
    public String fileId;
    public int chunkNo;
    public int replicationDeg;
    public byte[] body;

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
                return new PutChunkMessage(protocolVersion, senderId, fileId, chunkNo, replicationDeg, body);
            case "STORED":
                chunkNo = Integer.parseInt(header[4]);
                return new StoredMessage(protocolVersion, senderId, fileId, chunkNo);
            case "GETCHUNK":
                chunkNo = Integer.parseInt(header[4]);
                return new GetChunkMessage(protocolVersion, senderId, fileId, chunkNo);
            case "CHUNK":
                chunkNo = Integer.parseInt(header[4]);
                return new ChunkMessage(protocolVersion, senderId, fileId, chunkNo, body);
            case "DELETE":
                return new DeleteMessage(protocolVersion, senderId, fileId);
            case "REMOVED":
                chunkNo = Integer.parseInt(header[4]);
                return new RemovedMessage(protocolVersion, senderId, fileId, chunkNo);
            default:
                throw new Exception("Unknown message type");
        }

    }

    public abstract byte[] encode();

    public abstract void submitTask(Peer peer);

    public boolean messageOwner(int peerId) {
        return peerId == this.senderId;
    }


}