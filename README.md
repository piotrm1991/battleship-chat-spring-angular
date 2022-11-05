# Battleship Game

This app allows multiple users play battleship game.\
There is global chat that allows all players to message each over.\
After going to battleship tab, the player will be paired with another player who isn't currently in the match.\
After setting all ships you can start the game.\
The game is persistent until both players leave the battleship tab, then they have to start the game from the beginning.

## Run

### Using Shell Scripts

`cd battleship-chat-spring-angular-master`/
`build.sh`
`docker-compose build`
`docker-compose up`

Open browser on `http://localhost:4200/`

### Without Shell Script

`cd battleship-chat-spring-angular-master`

1. Build forntend application

`cd frontend/battleship-client`
`npm install`
`ng build`
`cd ../../`

2. Build backend services

`cd backend/auth-service`
`mvn clean package`
`cd ../global-chat-service`
`mvn clean package`
`cd ../game-room-service`
`mvn clean package`
`cd ../../`

3. Build and run docker

`docker-compose build`
`docker-compose up`
