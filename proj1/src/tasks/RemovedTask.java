package tasks;

import messages.Message;
import peer.Peer;
import storage.Chunk;

public class RemovedTask extends Task {
    public RemovedTask(Peer peer, Message message) {
        super(peer, message);
    }

    @Override
    public void run() {
        System.out.println(String.format("Received REMOVED: chunk no: %d ; file: %s", this.message.chunkNo, this.message.fileId));

        // Add peer acknowledge to received STORED messages
        if (this.peer.getStorage().hasStoredChunk(this.message.fileId, this.message.chunkNo)) {
            Chunk chunk = this.peer.getStorage().getStoredChunk(this.message.fileId, this.message.chunkNo);
            chunk.removePeerAck(this.message.senderId);
            System.out.println("Removed ack for stored chunk no: " + this.message.chunkNo);
        }
        else if (this.peer.getStorage().hasSentChunk(this.message.fileId, this.message.chunkNo)) {
            Chunk chunk = this.peer.getStorage().getSentChunk(this.message.fileId, this.message.chunkNo);
            chunk.removePeerAck(this.message.senderId);
            System.out.println("Removed ack for sent chunk no: " + this.message.chunkNo);
        }

        // TODO Check if the replication degree is below desired to initiate Backup Subprotocol
    }
}
