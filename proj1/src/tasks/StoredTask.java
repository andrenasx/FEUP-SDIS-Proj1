package tasks;

import messages.StoredMessage;
import peer.Peer;
import storage.Chunk;

public class StoredTask extends Task {
    public StoredTask(Peer peer, StoredMessage message) {
        super(peer, message);
    }

    @Override
    public void run() {
        //System.out.println(String.format("Received STORED from peer %d chunk %d of file %s",this.message.getSenderId(),chunk.getChunkNo(),chunk.getFileId()));

        // Add peer acknowledge to received STORED messages
        if (this.peer.getStorage().hasStoredChunk(this.message.getFileId(), this.message.getChunkNo())) {
            Chunk chunk = this.peer.getStorage().getStoredChunk(this.message.getFileId(), this.message.getChunkNo());
            chunk.addPeerAck(this.message.getSenderId());
            //System.out.println("[BACKUP] Added ack for stored chunk " + this.message.getFileId() + "_" + this.message.getChunkNo());
        }
        else if (this.peer.getStorage().hasSentChunk(this.message.getFileId(), this.message.getChunkNo())) {
            Chunk chunk = this.peer.getStorage().getSentChunk(this.message.getFileId(), this.message.getChunkNo());
            chunk.addPeerAck(this.message.getSenderId());
            //System.out.println("[BACKUP] Added ack for sent chunk " + this.message.getFileId() + "_" + this.message.getChunkNo());
        }
    }
}
