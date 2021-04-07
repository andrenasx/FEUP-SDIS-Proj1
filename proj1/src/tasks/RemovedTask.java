package tasks;

import messages.Message;
import messages.StoredMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;
import workers.BackupChunkWorker;

import java.io.IOException;

public class RemovedTask extends Task {
    public RemovedTask(Peer peer, Message message) {
        super(peer, message);
    }

    @Override
    public void run() {
        //System.out.println(String.format("Received REMOVED: chunk no: %d ; file: %s", this.message.getChunkNo(), this.message.getFileId()));

        // Remove peer acknowledge to received chunk
        if (this.peer.getStorage().hasSentChunk(this.message.getFileId(), this.message.getChunkNo())) {
            Chunk chunk = this.peer.getStorage().getSentChunk(this.message.getFileId(), this.message.getChunkNo());
            chunk.removePeerAck(this.message.getSenderId());
            //System.out.println("[RECLAIMING] Removed ack from peer " + this.message.getFileId() + " for sent chunk: " + this.message.getFileId() + "_" + this.message.getChunkNo());
        }
        else if (this.peer.getStorage().hasStoredChunk(this.message.getFileId(), this.message.getChunkNo())) {
            Chunk chunk = this.peer.getStorage().getStoredChunk(this.message.getFileId(), this.message.getChunkNo());
            chunk.removePeerAck(this.message.getSenderId());
            //System.out.println("[RECLAIMING] Removed ack from peer " + this.message.getFileId() +" for stored chunk: " + this.message.getFileId() + "_" + this.message.getChunkNo());

            // Check if this peer has this chunk and it needs replication
            if (chunk.needsReplication() && chunk.isStoredLocally()) {

                // Sleep to avoid collision in case another peer already replicated it
                Utils.sleepRandom();

                // If chunk still needs replication restore chunk body and start backup subprotocol
                if (chunk.needsReplication()) {
                    try {
                        chunk.setBody(this.peer.getStorage().restoreChunkBody(chunk.getUniqueId()));
                    } catch (IOException e) {
                        System.err.println("Couldn't restore chunk body");
                    }

                    System.out.printf("[RECLAIMING] Chunk %s needs replication\n", chunk.getUniqueId());

                    BackupChunkWorker worker = new BackupChunkWorker(this.peer, chunk);
                    this.peer.submitBackupThread(worker);
                    System.out.printf("[BACKUP] Submitted backup for chunk: %s\n", chunk.getUniqueId());

                    // Sleep to make sure peer stores chunk before receiving this peer STORED message
                    Utils.sleep(50);

                    StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), chunk.getFileId(), chunk.getChunkNo());
                    this.peer.sendControlMessage(message);
                }
            }
        }
    }
}
