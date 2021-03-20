package tasks;

import messages.Message;
import messages.StoredMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

public class PutchunkTask extends Task {
    public PutchunkTask(Peer peer, Message message) {
        super(peer, message);
    }

    @Override
    public void run() {
        System.out.println(String.format("Received PUTCHUNK: chunk no: %d ; file: %s", this.message.chunkNo, this.message.fileId));

        Chunk c;
        // If peer does not have received chunk add it to peer StoredChunk map
        if (!this.peer.hasStoredChunk(this.message.fileId, this.message.chunkNo)) {
            c = new Chunk(this.message);
            this.peer.addStoredChunk(c.getUniqueId(), c);
        }

        else {
            c = this.peer.getStoredChunk(this.message.fileId, this.message.chunkNo);

            // If peer has current chunk stored (in map and acknowledged) send STORED message
            if (c.isStoredLocally()) {
                StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), c.getFileId(), c.getChunkNo());
                this.peer.sendControlMessage(message);
                //System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s", c.getChunkNo(), c.getFileId()));
                return;
            }
        }

        // Sleep between 0-400 ms to avoid collisions
        Utils.sleepRandom();


        // If received chunk still needs replication add it to peer map and acknowledge it
        if (c.needsReplication()) {
            c.setStoredLocally(true);
            c.addPeer(this.peer.getId());
            this.peer.storeChunk(c);

            StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), c.getFileId(), c.getChunkNo());
            this.peer.sendControlMessage(message);
            //System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s", c.getChunkNo(), c.getFileId()));
        }
        // Else if already replicated remove from peer map
        else {
            this.peer.removeStoredChunk(c.getUniqueId());
            //System.out.println(String.format("Chunk No: %d of file: %s is already completely replicated", c.getChunkNo(), c.getFileId()));
        }
    }
}
