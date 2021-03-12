package channel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastChannel{
    private InetAddress address;
    private int port;
    private MulticastSocket mcSocket;

    public MulticastChannel(String addressString, int port) {
        try {
            this.address = InetAddress.getByName(addressString);
            this.port = port;

            // Create Socket
            this.mcSocket = new MulticastSocket(this.port);
            this.mcSocket.setTimeToLive(1);
            this.mcSocket.joinGroup(this.address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Channel{" +
                "address=" + address +
                ", port=" + port +
                '}';
    }
}
