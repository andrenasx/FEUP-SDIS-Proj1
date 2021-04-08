package tasks;

import messages.WakeyMessage;
import peer.Peer;
import workers.DeleteFileWorker;

import java.util.Map;
import java.util.Set;

public class WakeyTask extends Task {
    public WakeyTask(Peer peer, WakeyMessage message) {
        super(peer, message);
    }

    @Override
    public void run() {
        System.out.printf("[WAKEY] Received WakeyWakey from %d\n", this.message.getSenderId());

        // Send DELETE message for files that the peer that woke up had backed up
        for (Map.Entry<String, Set<Integer>> entry : this.peer.getStorage().getDeletedFilesMap().entrySet()) {
            if (entry.getValue().contains(this.message.getSenderId())) {
                // Submit delete worker for this file
                DeleteFileWorker worker = new DeleteFileWorker(this.peer, entry.getKey());
                this.peer.submitControlThread(worker);

                System.out.printf("[DELETION] Submitted delete for file: %s\n", entry.getKey());
            }
        }
    }
}
