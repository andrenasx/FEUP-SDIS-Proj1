package tasks;

import messages.PutChunkMessage;
import messages.StoredMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PutchunkTask extends Task {
    private final ScheduledThreadPoolExecutor scheduler;

    public PutchunkTask(Peer peer, PutChunkMessage message) {
        super(peer, message);
        this.scheduler = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public void run() {
        //System.out.println(String.format("Received PUTCHUNK: chunk no: %d ; file: %s", this.message.getChunkNo(), this.message.getFileId()));

        // Abort if it was a chunk this peer backed up
        if (this.peer.getStorage().hasSentChunk(this.message.getFileId(), this.message.getChunkNo())) {
            //System.out.println("[BACKUP] Aborting PUTCHUNK, my sent chunk");
            return;
        }
        // Abort if peer has enough space to store chunk
        if (!this.peer.getStorage().hasEnoughSpace(this.message.getBody().length / 1000.0)) {
            System.err.println("[BACKUP] Not enough space to store chunk " + this.message.getFileId() + "_" + this.message.getChunkNo());
            return;
        }

        Chunk chunk;
        // If peer does not have received chunk add it to peer StoredChunk map
        if (!this.peer.getStorage().hasStoredChunk(this.message.getFileId(), this.message.getChunkNo())) {
            chunk = new Chunk(this.message);
            this.peer.getStorage().addStoredChunk(chunk.getUniqueId(), chunk);
        }
        else {
            chunk = this.peer.getStorage().getStoredChunk(this.message.getFileId(), this.message.getChunkNo());

            // If peer has current chunk stored (in map and acknowledged) send STORED message
            if (chunk.isStoredLocally()) {
                StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), chunk.getFileId(), chunk.getChunkNo());
                this.peer.sendControlMessage(message);
                //System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s", c.getChunkNo(), c.getFileId()));
                return;
            }
        }

        // Schedule according to peer % used space if enhanced
        if (this.peer.isEnhanced()) {
            this.scheduler.schedule(() -> this.storeChunkEn(chunk), Utils.getRandomEn(400, this.peer.getStorage().getOccupiedSpace(), this.peer.getStorage().getStorageCapacity()), TimeUnit.MILLISECONDS);
        }
        // Just schedule randomly between 0-400ms if default
        else {
            this.scheduler.schedule(() -> this.storeChunk(chunk), Utils.getRandom(400), TimeUnit.MILLISECONDS);
        }
    }

    private void storeChunkEn(Chunk chunk) {
        // Store chunk only if it still needs replication
        if (chunk.needsReplication()) {
            this.storeChunk(chunk);
        }
        // Else if already replicated remove from peer map
        else {
            this.peer.getStorage().removeStoredChunk(chunk.getUniqueId());
            //System.out.println(String.format("Chunk No: %d of file: %s is already completely replicated", c.getChunkNo(), c.getFileId()));
        }
    }

    private void storeChunk(Chunk chunk) {
        // Check if peer has enough space to store chunk
        if (!this.peer.getStorage().hasEnoughSpace(chunk.getSize())) {
            System.err.println("[BACKUP] Not enough space to store chunk " + chunk.getUniqueId());
            this.peer.getStorage().removeStoredChunk(chunk.getUniqueId());
            return;
        }

        try {
            // Store chunk in file
            this.peer.getStorage().storeChunk(chunk, this.message.getBody());

            // Acknowledge that chunk is stored and add it to peer ack Set
            chunk.setStoredLocally(true);
            chunk.addPeerAck(this.peer.getId());

            // Send stored message
            StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), chunk.getFileId(), chunk.getChunkNo());
            this.peer.sendControlMessage(message);

            //System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s", chunk.getChunkNo(), chunk.getFileId()));
        } catch (IOException e) {
            System.err.printf("Failed to store chunk %s\n", chunk.getUniqueId());
        }
    }
}
