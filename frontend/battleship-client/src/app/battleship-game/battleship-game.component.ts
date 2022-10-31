import { AfterViewInit, Component, OnDestroy, OnInit, ViewEncapsulation, Renderer2 } from '@angular/core';
import { StorageService } from '../_services/storage.service';
import { UserService } from '../_services/user.service';

@Component({
  selector: 'app-battleship-game',
  templateUrl: './battleship-game.component.html',
  styleUrls: ['./battleship-game.component.css']
})
export class BattleshipGameComponent implements OnInit, OnDestroy, AfterViewInit {

  private stompClient;
  private currentUser;

   // For Battleship Game
   private width;
   private userSquares = [];
   private enemySquares = [];
   private userGrid;
   private enemyGrid;
   private displayGrid;
   private rotateButton;
   private destroyer;
   private submarine;
   private cruiser;
   private battleship;
   private carrier;
   private selectedShipNameWithIndex;
   private draggedShip;
   private draggedShipLength;
   private ships;
   private allShipsPlaced = false;
   private shipArray;
   private startButton;
   private setupButtons;
   private isGameOver;
   private turnDisplay;
   private infoDisplay;
   private userShips = [];
   private isHorizontal = true;
   
   private yourTurn = false;

  constructor(private userService: UserService, private storageService: StorageService, private renderer: Renderer2) { }

  ngOnInit(): void {
    this.currentUser = this.storageService.getUser();
    this.width = 10;
    // this.userSquares = [];
    // this.enemySquares = [];
    this.userGrid = document.querySelector('.grid-user');
    this.enemyGrid = document.querySelector('.grid-enemy');
    this.displayGrid = document.querySelector('.grid-display');
    this.rotateButton = document.querySelector('#rotate');
    this.destroyer = document.querySelector('.destroyer-container');
    this.submarine = document.querySelector('.submarine-container');
    this.cruiser = document.querySelector('.cruiser-container');
    this.battleship = document.querySelector('.battleship-container');
    this.carrier = document.querySelector('.carrier-container');
    this.ships = document.querySelectorAll('.ship');
    this.startButton = document.querySelector('#start');
    this.setupButtons = document.getElementById('setup-buttons');
    this.turnDisplay = document.querySelector('#whose-go');
    this.infoDisplay = document.querySelector('#info');
    this.isGameOver = false;
    this.createBoard(this.userGrid, this.userSquares);
    this.createBoard(this.enemyGrid, this.enemySquares);
  }

  ngOnDestroy(): void {
    // this.stompClient.disconnect();
  }

  ngAfterViewInit(): void {
    this.rotateButton.addEventListener('click', this.rotate.bind(this));
    this.ships.forEach(ship => ship.addEventListener('dragstart', this.dragStart.bind(this)));
    this.userSquares.forEach(square => square.addEventListener('dragstart', this.dragStart.bind(this)));
    this.userSquares.forEach(square => square.addEventListener('dragover', this.dragOver.bind(this)));
    this.userSquares.forEach(square => square.addEventListener('dragenter', this.dragEnter.bind(this)));
    this.userSquares.forEach(square => square.addEventListener('dragleave', this.dragLeave.bind(this)));
    this.userSquares.forEach(square => square.addEventListener('drop', this.dragDrop.bind(this)));
    this.userSquares.forEach(square => square.addEventListener('dragend', this.dragEnd.bind(this)));
    this.ships.forEach(ship => ship.addEventListener('mousedown', (e) => {
      this.selectedShipNameWithIndex = e.target.id;
    }));
    this.startButton.addEventListener('click', () => {
      console.log(this.userShips);
      this.stompClient.send(
        '/app/game.setShips/' + this.currentUser.id,
        {},
        JSON.stringify({
          currentPlayerId: this.currentUser.id, 
          currentPlayer: this.currentUser.username, 
          type: 'SET_SHIPS',
          shipsPlacement: this.userShips
        })
      );
    });

    this.enemySquares.forEach(square => square.addEventListener('click', () => {
      if (this.yourTurn) {
        this.fireAtTarget(square);
        this.yourTurn = false;
        document.querySelector('#destruction-info').innerHTML = '';
      }
    }));
  }

  private createBoard(grid, squares) {
    for (let i = 0; i < this.width * this.width; i++) {
      const square: HTMLParagraphElement = this.renderer.createElement('div');
      square.dataset['id'] = i.toString(); 
      grid.appendChild(square);
      squares.push(square);
    }
  }

  private fireAtTarget(square) {
    console.log("FIRE: " + square.dataset.id);
    this.stompClient.send(
      '/app/game.shootingTarget/' + this.currentUser.id,
      {},
      JSON.stringify({
        currentPlayerId: this.currentUser.id, 
        targetFire: square.dataset.id,
        type: 'FIRE', 
      })
    );
  }

  public onSubscribeToGame(stompClient) {
    this.stompClient = stompClient;
    this.stompClient.subscribe('/topic/private/game/' + this.currentUser.id, (payload) => {
      this.onMessageRecivedGame(payload);
    });
    this.stompClient.send(
      '/app/game.connectPlayer/' + this.currentUser.id,
      {},
      JSON.stringify({
        currentPlayerId: this.currentUser.id, 
        currentPlayer: this.currentUser.username, 
        type: 'CONNECT', 
      })
    );
  }

  private onMessageRecivedGame(payload) {
    const message = JSON.parse(payload.body);
    if (message.type === 'CONNECT' && message.currentPlayerId === this.currentUser.id) {
      const currentPlayerHeader = document.querySelector('#currentPlayerHeader');
      currentPlayerHeader.innerHTML = this.currentUser.username;
      if (message.currentPlayerOnlineStatus === 'ONLINE') {
        document.querySelector('#currentPlayer .connected').classList.add('active');
      }
      if (message.currentPlayerGameStatus === 'READY' && message.currentPlayerId === this.currentUser.id) {
        document.querySelector('#currentPlayer .ready').classList.add('active');
        this.populateUserSquaresWithShips(message.shipsPlacement);
        this.allShipsPlaced = true;
        this.displayGrid.innerHTML = ''; 
        document.querySelector('#hidden-info').removeChild(this.setupButtons);
      }
      if (message.enemyPlayerId !== null) {
        const enemyPlayerHeader = document.querySelector('#enemyPlayerHeader');
        enemyPlayerHeader.innerHTML = message.enemyPlayer;
      }
      if (message.enemyPlayerOnlineStatus === 'ONLINE') {
        document.querySelector('#enemyPlayer .connected').classList.add('active');
      }
      if (message.enemyPlayerGameStatus === 'READY') {
        document.querySelector('#enemyPlayer .ready').classList.add('active');
      }
      if (message.currentPlayerShoots !== null) {
        this.populateEnemySquaresWithShoots(message.currentPlayerShoots);
      }
      if (message.enemyPlayerShoots !== null) {
        this.populateUserSquaresWithShoots(message.enemyPlayerShoots);
      }
    }
    if (message.type === 'CONNECT_ENEMY') {
      const enemyPlayerHeader = document.querySelector('#enemyPlayerHeader');
      enemyPlayerHeader.innerHTML = message.enemyPlayer;
      if (message.enemyPlayerOnlineStatus === 'ONLINE') {
        document.querySelector('#enemyPlayer .connected').classList.add('active');
      }
      if (message.enemyPlayerOnlineStatus === 'READY') {
        document.querySelector('#enemyPlayer .ready').classList.add('active');
      }
    }
    if (message.type === 'DISCONNECT' && message.enemyPlayerOnlineStatus === 'OFFLINE') {
      document.querySelector('#enemyPlayer .connected').classList.remove('active');
    }
    if (message.type === 'NOT_READY' && message.currentPlayerId === this.currentUser.id) {
      this.infoDisplay.innerHTML = message.content;
    }
    if (message.type === 'READY' && message.currentPlayerId === this.currentUser.id && message.currentPlayerGameStatus === 'READY') {
      document.querySelector('#currentPlayer .ready').classList.add('active');
      document.querySelector('#hidden-info').removeChild(this.setupButtons);
    }
    if (message.type === 'READY' && message.currentPlayerId === this.currentUser.id && message.enemyPlayerGameStatus === 'READY') {
      document.querySelector('#enemyPlayer .ready').classList.add('active');
    }
    if (message.type === 'YOUR_TURN' && message.currentPlayerId === this.currentUser.id) {
      this.infoDisplay.innerHTML = 'FIGHT!';
      document.querySelector('#whose-go').innerHTML = message.content;
      this.yourTurn = true;
    }
    if (message.type === 'ENEMY_TURN' && message.currentPlayerId === this.currentUser.id) {
      this.infoDisplay.innerHTML = 'FIGHT!';
      document.querySelector('#whose-go').innerHTML = message.content;
    }
    if (message.type === 'AFTER_FIRE' && message.currentPlayerId === this.currentUser.id) {
      this.enemySquares[message.targetFire].classList.add(message.content);
    }
    if (message.type === 'ENEMY_FIRE' && message.currentPlayerId === this.currentUser.id) {
      this.userSquares[message.targetFire].classList.add(message.content);
    }
    if (message.type === 'DESTROYED_SHIP' && message.currentPlayerId === this.currentUser.id) {
      document.querySelector('#destruction-info').innerHTML = message.content;
    }
    if (message.type === 'GAME_OVER' && message.currentPlayerId === this.currentUser.id) {
      document.querySelector('#destruction-info').innerHTML = message.content;
      document.querySelector('#whose-go').innerHTML = "";
      this.infoDisplay.innerHTML = 'Game OVER';
      this.yourTurn = false;
    }
  }

  private populateUserSquaresWithShoots(shoots) {
    shoots.forEach(s => {
      if (s.damage) {
        this.userSquares[parseInt(s.target)].classList.add('boom');
      } else {
        this.userSquares[parseInt(s.target)].classList.add('miss');
      }
    });
  }

  private populateEnemySquaresWithShoots(shoots) {
    shoots.forEach(s => {
      if (s.damage) {
        this.enemySquares[parseInt(s.target)].classList.add('boom');
      } else {
        this.enemySquares[parseInt(s.target)].classList.add('miss');
      }
    });
  }

  private populateUserSquaresWithShips(ships) {
    ships.forEach(ship => {
      let direction = ship.placement.length; 
      let j = 0;
      ship.placement.forEach(i => {
        if (j === 0) {
          this.userSquares[parseInt(i)].classList.add('taken', ship.position, 'start' , ship.name);
        } else if (j === (direction - 1) ) {
          this.userSquares[parseInt(i)].classList.add('taken', ship.position, 'end' , ship.name);
        } else {
          this.userSquares[parseInt(i)].classList.add('taken', ship.position, 'undentified', ship.name);
        }
        j++;
      });
    });
  }

  private dragStart(event) {
    this.draggedShip = event.toElement;
    this.draggedShipLength = event.toElement.childNodes.length;
    // console.log(draggedShip)
  }

  private dragOver(e) {
    e.preventDefault();
  }

  private dragEnter(e) {
    e.preventDefault();
  }

  private dragLeave() {
    // console.log('drag leave')
  }

  private dragDrop(event) {
    let shipNameWithLastId = this.draggedShip.lastChild.id;
    let shipClass = shipNameWithLastId.slice(0, -2);
    // console.log(shipClass)
    let lastShipIndex = parseInt(shipNameWithLastId.substr(-1));
    let shipLastId = lastShipIndex + parseInt(event.toElement.dataset.id);
    // console.log(shipLastId)
    const notAllowedHorizontal = [0,10,20,30,40,50,60,70,80,90,1,11,21,31,41,51,61,71,81,91,2,22,32,42,52,62,72,82,92,3,13,23,33,43,53,63,73,83,93];
    const notAllowedVertical = [99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60];
    
    let newNotAllowedHorizontal = notAllowedHorizontal.splice(0, 10 * lastShipIndex);
    let newNotAllowedVertical = notAllowedVertical.splice(0, 10 * lastShipIndex);

    const selectedShipIndex = parseInt(this.selectedShipNameWithIndex.substr(-1));

    shipLastId = shipLastId - selectedShipIndex;
    // console.log(shipLastId)

    if (this.isHorizontal && !newNotAllowedHorizontal.includes(shipLastId)) {
      let shipPlacement = [];
      for (let i=0; i < this.draggedShipLength; i++) {
        let directionClass;
        if (i === 0) directionClass = 'start';
        if (i === this.draggedShipLength - 1) directionClass = 'end';
        this.userSquares[parseInt(event.toElement.dataset.id) - selectedShipIndex + i].classList.add('taken', 'horizontal', directionClass, shipClass);
        shipPlacement.push(parseInt(event.toElement.dataset.id) - selectedShipIndex + i);
        
      }
      this.userShips.push(
        {
          name: shipClass,
          placement: shipPlacement
        }
      );
    //As long as the index of the ship you are dragging is not in the newNotAllowedVertical array! This means that sometimes if you drag the ship by its
    //index-1 , index-2 and so on, the ship will rebound back to the displayGrid.
    } else if (!this.isHorizontal && !newNotAllowedVertical.includes(shipLastId)) {
      let shipPlacement = [];
      for (let i=0; i < this.draggedShipLength; i++) {
        let directionClass;
        if (i === 0) directionClass = 'start';
        if (i === this.draggedShipLength - 1) directionClass = 'end';
        this.userSquares[parseInt(event.toElement.dataset.id) - selectedShipIndex + this.width*i].classList.add('taken', 'vertical', directionClass, shipClass);
        shipPlacement.push(parseInt(event.toElement.dataset.id) - selectedShipIndex + this.width*i);
      }
      this.userShips.push(
        {
          name: shipClass,
          placement: shipPlacement
        }
      );
    } else return;

    this.displayGrid.removeChild(this.draggedShip);
    if(!this.displayGrid.querySelector('.ship')) {
      this.allShipsPlaced = true;
    }
  }

  private dragEnd() {
    // console.log('dragend')
  }

  private rotate(event) {
    if (this.isHorizontal) {
      this.destroyer.classList.toggle('destroyer-container-vertical');
      this.submarine.classList.toggle('submarine-container-vertical');
      this.cruiser.classList.toggle('cruiser-container-vertical');
      this.battleship.classList.toggle('battleship-container-vertical');
      this.carrier.classList.toggle('carrier-container-vertical');
      this.isHorizontal = false;
      return
    }
    if (!this.isHorizontal) {
      this.destroyer.classList.toggle('destroyer-container-vertical');
      this.submarine.classList.toggle('submarine-container-vertical');
      this.cruiser.classList.toggle('cruiser-container-vertical');
      this.battleship.classList.toggle('battleship-container-vertical');
      this.carrier.classList.toggle('carrier-container-vertical');
      this.isHorizontal = true;
      return
    }
  }

}
