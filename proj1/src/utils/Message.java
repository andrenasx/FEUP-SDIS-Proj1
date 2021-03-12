package utils;

public class Message {

    private String version;
    private String type;
    private int senderId;
    private String fileId;
    private int chunkNumber;
    private int replicationDegree;
    private byte[] body;

    private String header;


    public Message(String version, String type, int senderId, String fileId, int chunkNumber, int replicationDegree, byte[] body){

        this.version = version;
        this.type = type;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.replicationDegree = replicationDegree;
        this.body = body;
    }



    public boolean isPutchunk() { return this.type.equals("PUTCHUNK"); }

    public boolean isStored() { return this.type.equals("STORED"); }

    public boolean isGetChunk() { return this.type.equals("GETCHUNK"); }

    public boolean isChunk() { return this.type.equals("CHUNK"); }

    public boolean isDelete() { return this.type.equals("DELETE"); }

    public boolean isRemoved() { return this.type.equals("REMOVED"); }






































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


}
