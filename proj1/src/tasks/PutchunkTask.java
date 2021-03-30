package tasks;

import messages.PutChunkMessage;
import messages.StoredMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

import java.io.IOException;

public class PutchunkTask extends Task {
    public PutchunkTask(Peer peer, PutChunkMessage message) {
        super(peer, message);
    }

    @Override
    public void run() {
        //System.out.println(String.format("Received PUTCHUNK: chunk no: %d ; file: %s", this.message.chunkNo, this.message.fileId));

        Chunk chunk;
        // If peer does not have received chunk add it to peer StoredChunk map
        if (!this.peer.getStorage().hasStoredChunk(this.message.fileId, this.message.chunkNo)) {
            chunk = new Chunk(this.message);
            this.peer.getStorage().addStoredChunk(chunk.getUniqueId(), chunk);
        }
        else {
            chunk = this.peer.getStorage().getStoredChunk(this.message.fileId, this.message.chunkNo);

            // If peer has current chunk stored (in map and acknowledged) send STORED message
            if (chunk.isStoredLocally()) {
                StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), chunk.getFileId(), chunk.getChunkNo());
                this.peer.sendControlMessage(message);
                //System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s", c.getChunkNo(), c.getFileId()));
                return;
            }
        }

        // Sleep between 0-400 ms to avoid collisions
        Utils.sleepRandom();

        // If received chunk still needs replication add it to peer map and acknowledge it
        if (chunk.needsReplication()) {
            // Check if peer has enough space to store chunk
            if (!this.peer.getStorage().hasEnoughSpace(chunk.getSize())) {
                System.out.println("Not enough space to store chunk " + chunk.getUniqueId());
                return;
            }

            try {
                // Store chunk in file
                this.peer.getStorage().storeChunk(chunk, this.message.body);

                // Acknowledge that chunk is stored and add it to peer ack Set
                chunk.setStoredLocally(true);
                chunk.addPeerAck(this.peer.getId());

                // Send stored message
                StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), chunk.getFileId(), chunk.getChunkNo());
                this.peer.sendControlMessage(message);
                //System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s", chunk.getChunkNo(), chunk.getFileId()));
            } catch (IOException e) {
                System.out.printf("Failed to store chunk %s\n", chunk.getUniqueId());
            }
        }
        // Else if already replicated remove from peer map
        else {
            this.peer.getStorage().removeStoredChunk(chunk.getUniqueId());
            //System.out.println(String.format("Chunk No: %d of file: %s is already completely replicated", c.getChunkNo(), c.getFileId()));
        }
    }
}
