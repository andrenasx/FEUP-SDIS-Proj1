package tasks;

import messages.Message;
import peer.Peer;
import storage.Chunk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DeleteTask extends Task{
    public DeleteTask(Peer peer, Message message) {
        super(peer, message);
    }

    @Override
    public void run() {
        System.out.printf("Received DELETE for file %s\n", this.message.fileId);
        ConcurrentHashMap<String, Chunk> chunks = this.peer.getState().getStoredChunks();
        for(Chunk chunk: chunks.values()){
            if(chunk.getFileId().equals(this.message.fileId)){
                this.peer.getState().deleteChunk(chunk);
            }
        }
    }
}
