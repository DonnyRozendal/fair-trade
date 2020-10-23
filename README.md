# FairTrade
FairTrade is Hyperledger Fabric application written in Kotlin.
This project contains three folders:
- `chaincode` contains the chaincode
- `client` contains the server application
- `test-network` contains the Hyperledger Fabric network
- 
#### Setup
Prerequisites:
- Postman (download the collection [here](https://www.getpostman.com/collections/a49b05be5fb36c2b0163))
- [Docker](https://www.docker.com/)
- [Hyperledger Fabric binaries](https://hyperledger-fabric.readthedocs.io/en/latest/install.html)
- Add the `bin` folder of the fabric samples to your PATH
- Set the `FABRIC_PATH` to your profile that points to the fabric-samples folder
- An IDE that can run Kotlin code, like [Intellij Idea](https://www.jetbrains.com/idea/)
- A way to connect to the websocket, like the [Simple WebSocket Client](https://chrome.google.com/webstore/detail/simple-websocket-client/pfdhoblngboilpfeibdedpjgfnlcodoo?hl=en)

Hyperledger Fabric network setup:
- Open a terminal inside the `test-network` folder
- Run `./network.sh up createChannel -ca -s couchdb` to setup the Docker containers and create a channel
- Run `./network.sh deployCC -ccv 1.0 -ccs 1` to deploy the chaincode to the network. If you need to update the chaincode on the network, increase both the -ccv and -ccs value
- If you want to shutdown the network, run `./network.sh down`

Server setup:
- Start the main server from the `Main` file
- Start the websocket server from the `EventWebSocketServer` file
- Open up a websocket client in your browser, and connect to the websocket from `ws://localhost:8081`
- To subscribe to events from the websocket client, send `Subscribe` to the websocket server. This websocket client will then receive all events.

Creating users:
- Make sure the main server is started, then use `Create Admin` from the Postman collection
- After creating the admin, RESTART THE SERVER. (this is needed for some reason)
- Create the other users from `Create Farmer`, `Create Producer` and `Create Store`

Running through the business flow:
- Create 2 CocoBeanBags in the database by calling `Create CocoBeanBag` two times with different ids
- When there are 2 or more available CocoBeanBags in the database, a notification will be sent to the websocket client
- Create a shipment with the available CocoBeanBags received from the event by calling `Create ChocShipment`. Whenever a shipment is created, a notification will be sent to the websocket client
- Register store storage by calling `Register Storage`
- Whenever a shipment is ready, call `Load Storage` with this shipment id received from the event.
