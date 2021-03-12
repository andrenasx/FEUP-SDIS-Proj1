package storage;


import java.io.Serializable;

public class Chunk implements Serializable {


    private String fileId;
    private int chunkNumber;
    private int replicationDegree;
    private byte[] body;



    public Chunk(String fileId, int chunkNumber, int replicationDegree){
        this.fileId = fileId;
        this.chunkNumber=chunkNumber;

        this.replicationDegree = replicationDegree;
    }

    public Chunk(String fileId, int chunkNumber, int replicationDegree,byte[] body){
        this.fileId = fileId;
        this.chunkNumber=chunkNumber;

        this.replicationDegree = replicationDegree;
        this.body = body;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}
