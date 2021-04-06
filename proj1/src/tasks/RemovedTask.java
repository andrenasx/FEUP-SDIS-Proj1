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
        System.out.println(String.format("Received REMOVED: chunk no: %d ; file: %s", this.message.chunkNo, this.message.fileId));

        Chunk chunk = null;
        // Remove peer acknowledge to received chunk
        if (this.peer.getStorage().hasStoredChunk(this.message.fileId, this.message.chunkNo)) {
            chunk = this.peer.getStorage().getStoredChunk(this.message.fileId, this.message.chunkNo);
            System.out.println("Removed ack for stored chunk no: " + this.message.chunkNo);
        }
        else if (this.peer.getStorage().hasSentChunk(this.message.fileId, this.message.chunkNo)) {
            chunk = this.peer.getStorage().getSentChunk(this.message.fileId, this.message.chunkNo);
            System.out.println("Removed ack for sent chunk no: " + this.message.chunkNo);
        }

        if (chunk != null) {
            chunk.removePeerAck(this.message.senderId);

            System.out.println(chunk.needsReplication() && chunk.isStoredLocally());
            if (chunk.needsReplication() && chunk.isStoredLocally()) {
                Utils.sleepRandom();
                if (chunk.needsReplication()) {
                    try {
                        chunk.setBody(this.peer.getStorage().restoreChunkBody(chunk.getUniqueId()));
                    } catch (IOException e) {
                        System.err.println("Couldn't restore chunk body");
                    }

                    BackupChunkWorker worker = new BackupChunkWorker(this.peer, chunk);
                    this.peer.submitBackupThread(worker);
                    System.out.printf("Submitted chunk %d of file %s\n", chunk.getChunkNo(), chunk.getFileId());

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.out.println("Can't sleep");
                    }

                    StoredMessage message = new StoredMessage(this.peer.getProtocolVersion(), this.peer.getId(), chunk.getFileId(), chunk.getChunkNo());
                    this.peer.sendControlMessage(message);
                }
            }
        }
    }
}
