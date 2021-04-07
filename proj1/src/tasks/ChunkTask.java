package tasks;

import messages.ChunkMessage;
import peer.Peer;
import storage.Chunk;

public class ChunkTask extends Task {
    public ChunkTask(Peer peer, ChunkMessage message) {
        super(peer, message);
    }

    @Override
    public void run() {
        // If it is a stored chunk and CHUNK message was sent by another peer, acknowledge it (set sent to true) so we don't send more CHUNK messages for that chunk
        if (this.peer.getStorage().hasStoredChunk(this.message.getFileId(), this.message.getChunkNo())) {
            Chunk chunk = this.peer.getStorage().getStoredChunk(this.message.getFileId(), this.message.getChunkNo());
            chunk.setSent(true);
            System.out.println("Received CHUNK from  " + this.message.getSenderId() + " for chunk " + this.message.getFileId() + "_" + this.message.getChunkNo());
        }

        // If it is a sent chunk, add body to the chunk so we can restore information
        else if (this.peer.getStorage().hasSentChunk(this.message.getFileId(), this.message.getChunkNo())) {
            Chunk chunk = this.peer.getStorage().getSentChunk(this.message.getFileId(), this.message.getChunkNo());
            chunk.setBody(message.getBody());
            System.out.println("Add body to chunk " + this.message.getFileId() + "_" + this.message.getChunkNo());
        }
    }
}
