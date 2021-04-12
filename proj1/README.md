# SDIS 2020/2021: Project 1 - Distributed Backup Service

## Compiling
- To compile all the java code go to the `src` folder, open a terminal and run `sh ../scripts/compile.sh`
- After that a `build` folder with all .class files will be created inside that ``src`` folder

## Running
Inside the `src/build` folder:

1. Open a terminal and run `rmiregistry` to start the RMI needed for the TestApp
2. Open as many terminals needed for the number of peers and run `sh ../../scripts/peer.sh <version> <peer_id> <svc_access_point> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>`
    - version: Protocol version (1.0 for standard protocol, 2.0 for enhanced protocol)
    - peer_id: Peer's unique identifier.
    - svc_access_point: Access point for RMI object.
    - mc_addr: IP address for Multicast Control channel.
    - mc_port: Port for Multicast Control channel.
    - mdb_addr: IP address for Multicast Data Backup channel.
    - mdb_port: Port for Multicast Data Backup channel.
    - mdr_addr: IP address for Multicast Data Recovery channel.
    - mdr_port: Port for Multi-cast Data Recovery channel.
3. Open a terminal for the TestApp and run `sh ../../scripts/test.sh <peer_ap> BACKUP|RESTORE|DELETE|RECLAIM|STATE [<opnd_1> [<optnd_2]]`
    - peer_ap: Is the peer's access point for RMI object.
    - opnd_1: Is either the path name of the file to BACKUP/RESTORE/DELETE, for the respective 3 subprotocols, or, in the case of RECLAIM the maximum amount of disk space (in KByte) that the service can use to store the chunks.
    - opnd_2: This operand is an integer that specifies the desired replication degree and applies only to the BACKUP protocol (or its enhancement)
4. Finally, if you want to clean the created PeerStorage for a given peer, open a new terminal and run `sh ../../scripts/cleanup.sh <peer_id>`

### Testing Example
1. Open 1 terminal (TERMINAL1) in `src` folder
2. In TERMINAL1: `sh ../scripts/compile.sh`
3. Open 6 terminals (TERMINAL2, TERMINAL3, TERMINAL4, TERMINAL5, TERMINAL6, TERMINAL7) inside `src/build` folder
4. In TERMINAL2: `rmiregistry`
5. In TERMINAL3: `sh ../../scripts/peer.sh 1.0 1 peer1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003`
6. In TERMINAL4: `sh ../../scripts/peer.sh 1.0 2 peer2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003`
7. In TERMINAL5: `sh ../../scripts/peer.sh 1.0 3 peer3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003`
8. In TERMINAL6: `sh ../../scripts/peer.sh 1.0 4 peer4 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003`
9. In TERMINAL7:
    - `sh ../../scripts/test.sh peer1 BACKUP ../../files/1mb.jpg 2`
    - `sh ../../scripts/test.sh peer2 STATE`
    - `sh ../../scripts/test.sh peer1 RESTORE ../../files/1mb.jpg`
    - `sh ../../scripts/test.sh peer1 DELETE ../../files/1mb.jpg`
    - `sh ../../scripts/test.sh peer2 STATE`
    - `sh ../../scripts/test.sh peer2 RECLAIM 500`
    - `sh ../../scripts/test.sh peer2 STATE`
    - `sh ../../scripts/cleanup.sh 1`
    - `sh ../../scripts/cleanup.sh 2`
    - `sh ../../scripts/cleanup.sh 3`
    - `sh ../../scripts/cleanup.sh 4`