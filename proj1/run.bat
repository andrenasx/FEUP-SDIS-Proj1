start cmd /k java -jar McastSnooper.jar 224.0.0.15:8001 224.0.0.16:8002 224.0.0.17:8003

cd src
sh ../scripts/compile.sh

cd build
start rmiregistry

start cmd /k sh ../../scripts/peer.sh 1.0 1 peer1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
start cmd /k sh ../../scripts/peer.sh 1.0 2 peer2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
start cmd /k sh ../../scripts/peer.sh 1.0 3 peer3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
start cmd /k sh ../../scripts/peer.sh 1.0 4 peer4 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003