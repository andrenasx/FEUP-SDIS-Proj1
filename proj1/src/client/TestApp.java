package client;

import peer.PeerInit;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }

        String serviceAccessPoint = args[0];
        String protocol = args[1];
        PeerInit peer;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            peer = (PeerInit) registry.lookup(serviceAccessPoint);

            switch (protocol) {
                case "BACKUP": {
                    if (args.length != 4) {
                        System.out.println("Usage: java TestApp <peer_ap> BACKUP <file_path> <replication_degree>");
                        return;
                    }

                    String filepath = args[2];
                    int replicationDeg = Integer.parseInt(args[3]);
                    peer.backup(filepath, replicationDeg);

                    break;
                }
                case "RESTORE": {
                    if (args.length != 3) {
                        System.out.println("Usage: java TestApp <peer_ap> RESTORE <file_path>");
                        return;
                    }

                    String filepath = args[2];

                    try {
                        peer.restore(filepath);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case "DELETE": {
                    if (args.length != 3) {
                        System.out.println("Usage: java TestApp <peer_ap> DELETE <file_path>");
                        return;
                    }

                    String filepath = args[2];

                    try {
                        peer.delete(filepath);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case "RECLAIM": {
                    if (args.length != 3) {
                        System.out.println("Usage: java TestApp <peer_ap> RECLAIM <disk_space>");
                        return;
                    }

                    int diskspace = Integer.parseInt(args[2]);

                    try {
                        peer.reclaim(diskspace);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case "STATE": {
                    if (args.length != 2) {
                        System.out.println("Usage: java TestApp <peer_ap> STATE");
                        return;
                    }

                    try {
                        System.out.println(peer.state());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                default:
                    throw new Exception("Unknown protocol");
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
