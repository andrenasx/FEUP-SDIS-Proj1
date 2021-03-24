package protocol;

import messages.GetChunkMessage;
import peer.Peer;
import storage.Chunk;
import tasks.Task;

import java.util.concurrent.Callable;

public class RestoreProtocol implements Callable<Chunk> {
    private final Peer peer;
    private final Chunk chunk;

    public RestoreProtocol(Peer peer, Chunk chunk) {
        this.peer = peer;
        this.chunk = chunk;
    }

    @Override
    public Chunk call() {
        GetChunkMessage getChunkMessage = new GetChunkMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.chunk.getFileId(), this.chunk.getChunkNo());

        // Try to send GETCHUNK message max 5 times or until current chunk body is set
        int attempt = 0;
        do {
            this.peer.sendControlMessage(getChunkMessage);
            System.out.println("Sent GETCHUNK chunk no " + this.chunk.getChunkNo());
            int wait = (int) Math.pow(2, attempt) * 1000;

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (++attempt < Task.MAX_ATTEMPTS && this.chunk.getBody() == null);

        return this.chunk;
    }
}
