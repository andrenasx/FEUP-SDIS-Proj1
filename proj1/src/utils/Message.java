package utils;

import java.net.DatagramPacket;
import java.util.Arrays;

public class Message {


    private String type;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNumber;
    private int replicationDegree;
    private byte[] body;

    private DatagramPacket packet;

    public Message(DatagramPacket packet){
        this.packet=packet;
        this.unpackMessage();
    }

    private void unpackMessage(){
        String message = new String(this.packet.getData());
        message = message.substring(0, Math.min(this.packet.getLength(), message.length()));
        String[] parts = message.split("\r\n\r\n", 2);
        String[] header = parts[0].replaceAll("( )+", " ").trim().split(" ");
        this.type = header[0];
        this.version = header[1];
        this.senderId = Integer.parseInt(header[2]);
        this.fileId = header[3];
        this.chunkNumber = Integer.parseInt(header[4]);
        this.replicationDegree = Integer.parseInt(header[5]);

        if (parts.length == 2)
            this.body = Arrays.copyOfRange(this.packet.getData(), parts[0].length() + 4, this.packet.getLength());
    }

    public boolean typePutChunk() { return this.type.equals("PUTCHUNK"); }

    public boolean typeStored() { return this.type.equals("STORED"); }

    public boolean typeGetChunk() { return this.type.equals("GETCHUNK"); }

    public boolean typeChunk() { return this.type.equals("CHUNK"); }

    public boolean typeDelete() { return this.type.equals("DELETE"); }

    public boolean typeRemoved() { return this.type.equals("REMOVED"); }








































    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }


    public DatagramPacket getPacket() {
        return packet;
    }

    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }
}
