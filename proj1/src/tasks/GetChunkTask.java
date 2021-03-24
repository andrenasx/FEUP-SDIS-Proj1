package tasks;

import messages.ChunkMessage;
import messages.GetChunkMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

import java.io.IOException;

public class GetChunkTask extends Task {
    public GetChunkTask(Peer peer, GetChunkMessage message) {
        super(peer, message);
    }

    @Override
    public void run() {
        System.out.println("Received GETCHUNK from " + message.senderId + " for chunk no " + message.chunkNo);

        String chunkId = this.message.fileId + "_" + this.message.chunkNo;
        Chunk chunk = this.peer.getStorage().getStoredChunk(this.message.fileId + "_" + this.message.chunkNo);

        if (chunk == null || !chunk.isStoredLocally()) {
            System.out.println("Don't have chunk, id: " + chunkId);
            return;
        }

        chunk.setSent(false);

        Utils.sleepRandom();

        if (!chunk.getSent()) {
            try {
                byte[] body = this.peer.getStorage().restoreChunk(chunkId);

                ChunkMessage chunkMessage = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.message.fileId, this.message.chunkNo, body);
                this.peer.sendRestoreMessage(chunkMessage);
                System.out.println("Sent CHUNK message, id:" + chunkId);
            } catch (IOException e) {
                System.out.println("Unable to restore chunk, id:" + chunkId);
            }
        }
    }
}
