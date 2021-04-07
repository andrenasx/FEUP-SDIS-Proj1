package storage;

import messages.Message;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Chunk implements Serializable {
    private final String fileId;
    private final int chunkNo;
    private final String id;
    private final int replicationDegree;
    private byte[] body;
    private double size = 0;
    private final Set<Integer> peersAcks = ConcurrentHashMap.newKeySet();

    private boolean storedLocally = false;
    private boolean sent = false;

    public Chunk(Message message) {
        this(message.getFileId(), message.getChunkNo(), message.getReplicationDeg(), null);
        if (message.getBody() != null) this.size = message.getBody().length / 1000.0;
    }

    public Chunk(String fileId, int chunkNo, int replicationDegree, byte[] body) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.id = fileId + "_" + chunkNo;
        this.replicationDegree = replicationDegree;
        this.body = body;
        if (body != null) this.size = body.length / 1000.0;
    }

    public void addPeerAck(int peerId) {
        this.peersAcks.add(peerId);
    }

    public void removePeerAck(int peerId) {
        this.peersAcks.remove(peerId);
    }

    public int getNumberPeersAcks() {
        return this.peersAcks.size();
    }

    public boolean needsReplication() {
        return this.peersAcks.size() < this.replicationDegree;
    }

    public boolean isOverReplicated() {
        return this.peersAcks.size() > this.replicationDegree;
    }

    public void clearBody() {
        this.body = null;
    }

    public String getUniqueId() {
        return this.id;
    }

    public String getFileId() {
        return fileId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getDesiredReplicationDegree() {
        return replicationDegree;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public double getSize() {
        return size;
    }

    public boolean isStoredLocally() {
        return storedLocally;
    }

    public void setStoredLocally(boolean storedLocally) {
        this.storedLocally = storedLocally;
    }

    public boolean getSent() {
        return this.sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk that = (Chunk) o;
        return this.chunkNo == that.chunkNo && this.fileId.equals(that.fileId);
    }

    public String toStringSent() {
        return "CHUNK -> id: " + id + " ; perceived replication degree: " + peersAcks.size();
    }

    public String toStringStored() {
        return "CHUNK -> id: " + id + " ; size: " + size + " KBytes ; desired replication degree: " + replicationDegree + " ; perceived replication degree: " + peersAcks.size();
    }
}
