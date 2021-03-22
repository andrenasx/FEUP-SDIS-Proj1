cd src

javac channel/*.java client/*.java messages/*.java peer/*.java storage/*.java

start rmiregistry

start cmd /k java peer.Peer 1.0 1 peer1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
start cmd /k java peer.Peer 1.0 2 peer2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
start cmd /k java peer.Peer 1.0 3 peer3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
start cmd /k java peer.Peer 1.0 4 peer4 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003