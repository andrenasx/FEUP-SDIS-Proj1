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
        // If it is a stored chunk and CHUNK message was sent by another peer, acknowledge it so we don't send more CHUNK messages for that chunk
        if (this.peer.getStorage().hasStoredChunk(this.message.fileId, this.message.chunkNo)) {
            Chunk chunk = this.peer.getStorage().getStoredChunk(this.message.fileId, this.message.chunkNo);
            chunk.setSent(true);
            System.out.println("Received CHUNK from  " + this.message.senderId + " for chunk no " + this.message.chunkNo);
        }

        // If it is a sent chunk, add body to the chunk so we can restore information
        else if (this.peer.getStorage().hasSentChunk(this.message.fileId, this.message.chunkNo)) {
            Chunk chunk = this.peer.getStorage().getSentChunk(this.message.fileId, this.message.chunkNo);
            chunk.setBody(message.body);
            System.out.println("Add body to chunk no " + this.message.chunkNo);
        }
    }
}
