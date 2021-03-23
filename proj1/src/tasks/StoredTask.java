package tasks;

import messages.Message;
import peer.Peer;
import storage.Chunk;

public class StoredTask extends Task {
    public StoredTask(Peer peer, Message message) {
        super(peer, message);
    }

    @Override
    public void run() {
        Chunk chunk;

        // Add peer acknowledge to received STORED messages
        if (this.peer.getStorage().hasStoredChunk(this.message.fileId, this.message.chunkNo)) {
            chunk = this.peer.getStorage().getStoredChunk(this.message.fileId, this.message.chunkNo);
            chunk.addPeerAck(this.message.senderId);
            //System.out.println(String.format("Received STORED from peer %d chunk %d of file %s",this.message.senderId,c.getChunkNo(),c.getFileId()));
        }
        else if (this.peer.getStorage().hasSentChunk(this.message.fileId, this.message.chunkNo)) {
            chunk = this.peer.getStorage().getSentChunk(this.message.fileId, this.message.chunkNo);
            chunk.addPeerAck(this.message.senderId);
            //System.out.println(String.format("Received STORED from peer %d chunk %d of file %s",this.message.senderId,c.getChunkNo(),c.getFileId()));
        }
    }
}
