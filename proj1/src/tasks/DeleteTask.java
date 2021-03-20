package tasks;

import messages.Message;
import peer.Peer;
import storage.Chunk;

public class DeleteTask extends Task{
    public DeleteTask(Peer peer, Message message) {
        super(peer, message);
    }

    @Override
    public void run() {

    }
}
