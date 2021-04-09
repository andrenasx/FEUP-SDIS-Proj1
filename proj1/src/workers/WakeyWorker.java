package workers;

import messages.DeleteMessage;
import messages.WakeyMessage;
import peer.Peer;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class WakeyWorker implements Runnable {
    private final Peer peer;

    public WakeyWorker(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;

        try {
            // Create ServerSocket in a new port, 2secs to timeout
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(3000);
        } catch (IOException e) {
            System.err.println("Error in TCP socket");
        }

        // Get connection ports and create byte array to send in Wakey message
        String connection = serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort();
        byte[] content = connection.getBytes(StandardCharsets.UTF_8);

        WakeyMessage wakeyMessage = new WakeyMessage(this.peer.getProtocolVersion(), this.peer.getId(), content);
        this.peer.sendControlMessage(wakeyMessage);

        try {
            // Create socket and read Delete message sent by TCP
            Socket socket = serverSocket.accept();
            InputStream in = socket.getInputStream();
            byte[] data = in.readAllBytes();
            in.close();
            socket.close();

            DeleteMessage deleteMessage = DeleteMessage.create(data);
            // System.out.println("Received delete for " + deleteMessage.getFileId());
            deleteMessage.submitTask(this.peer);
        } catch (IOException e) {
            System.out.println("[WAKEY] No DELETEs received");
        }
    }
}