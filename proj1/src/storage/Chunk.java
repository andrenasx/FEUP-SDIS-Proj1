package storage;


import messages.Message;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Chunk implements Serializable {


    private String fileId;
    private int chunkNo;
    private int replicationDegree = 0;
    private byte[] body;
    private final Set<Integer> peersAcks = ConcurrentHashMap.newKeySet();

    private boolean storedLocally = false;

    public Chunk (Message m){
        this(m.fileId, m.chunkNo, m.replicationDeg, m.body);
    }


    public Chunk(String fileId, int chunkNo, int replicationDegree,byte[] body){
        this.fileId = fileId;
        this.chunkNo=chunkNo;
        this.replicationDegree = replicationDegree;
        this.body = body;
    }

    public void addPeer(int peerId){
        this.peersAcks.add(peerId);
    }

    public int getNumberPeers(){
        return this.peersAcks.size();
    }

    public boolean needsReplication(){
        return this.peersAcks.size()<this.replicationDegree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk that = (Chunk) o;
        return this.chunkNo == that.chunkNo && this.fileId.equals(that.fileId);
    }


    public void print(){
        System.out.println(String.format("Chunk: No %d; Size %d; File %s; In %d out of %d peers",this.chunkNo,this.body.length, this.fileId,this.peersAcks.size(),this.replicationDegree));
    }

    public void clearBody(){
        this.body=null;
    }

    public String getUniqueId(){return this.fileId + "_" + this.chunkNo;}

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getChunkNo() {return chunkNo; }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    public byte[] getBody() { return body; }

    public boolean isStoredLocally() {
        return storedLocally;
    }

    public void setStoredLocally(boolean storedLocally) {
        this.storedLocally = storedLocally;
    }
}
