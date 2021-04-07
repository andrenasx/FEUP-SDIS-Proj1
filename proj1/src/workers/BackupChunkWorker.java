package workers;

import messages.PutChunkMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

public class BackupChunkWorker implements Runnable {
    private final Peer peer;
    private final Chunk chunk;

    public BackupChunkWorker(Peer peer, Chunk chunk) {
        this.peer = peer;
        this.chunk = chunk;
    }

    @Override
    public void run() {
        // Add chunk to sent chunk map
        if (!this.peer.getStorage().hasSentChunk(this.chunk.getUniqueId())) {
            this.peer.getStorage().addSentChunk(this.chunk);
        }

        PutChunkMessage putChunkMessage = new PutChunkMessage(this.peer, this.chunk);

        // Try to send PUTCHUNK message max 5 times or until Replication degree is met
        int attempt = 0;
        do {
            this.peer.sendBackupMessage(putChunkMessage);
            //System.out.println(String.format("Sent PUTCHUNK: chunk no: %d ; file: %s", putChunkMessage.chunkNo, putChunkMessage.fileId));

            int wait = (int) Math.pow(2, attempt) * 1000;
            Utils.sleep(wait);
        } while (++attempt < Utils.MAX_5_ATTEMPTS && this.chunk.needsReplication());

        this.chunk.clearBody();
    }
}
