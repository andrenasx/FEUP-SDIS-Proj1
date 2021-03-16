package channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastChannel extends MulticastSocket implements Runnable {
    private InetAddress address;
    private int port;

    public MulticastChannel(String addressString, int port) throws IOException {
        // Create Socket
        super(port);

        this.address = InetAddress.getByName(addressString);
        this.port = port;

        this.setTimeToLive(1);
        this.joinGroup(this.address);
    }

    @Override
    public void run() {
        while (true) {
            byte[] buf = new byte[65000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                this.receive(packet);
                System.out.println("Received packet with length " + packet.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(byte[] message){
        DatagramPacket packet = new DatagramPacket(message, message.length, this.address, this.port);

        try {
            this.send(packet);
            System.out.println("Sent packet with length " + packet.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
