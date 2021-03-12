package storage;

import java.util.ArrayList;

public class StorageFile {


    private String senderId;
    private String fileName;
    private ArrayList<Chunk> chunks;
    private int replicationDegree;
    private static int CHUNK_SIZE = 64000;

    public StorageFile(String senderId, String fileName, ArrayList<Chunk> chunks, int replicationDegree) {
        this.senderId = senderId;
        this.fileName = fileName;
        this.chunks = chunks;
        this.replicationDegree = replicationDegree;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}
