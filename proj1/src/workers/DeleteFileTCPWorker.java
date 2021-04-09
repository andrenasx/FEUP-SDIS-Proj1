package workers;

import messages.DeleteMessage;
import peer.Peer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class DeleteFileTCPWorker implements Runnable {
    private final Peer peer;
    private final String fileId;
    private final String connection;

    public DeleteFileTCPWorker(Peer peer, String fileId, String connection) {
        this.peer = peer;
        this.fileId = fileId;
        this.connection = connection;
    }

    @Override
    public void run() {
        DeleteMessage deleteMessage = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.fileId);

        String[] parts = this.connection.split(":");
        try {
            Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));

            OutputStream out = socket.getOutputStream();
            out.write(deleteMessage.encode());
            System.out.printf("Sent DELETE for file %s\n", this.fileId);

            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error in TCP socket");
        }
    }
}
