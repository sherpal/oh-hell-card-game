# Oh Hell! card game
A very modest application to play the Oh Hell! card game with your friends.

In the project files, the game is called `Rikiki` according to (one of) the french name(s) of the game.

## Releases

A [release](https://github.com/sherpal/oh-hell-card-game/releases) is available for Windows and Linux.

## The rules, roughly

The Oh Hell! card game (it has many different names) is a trick based card game. At each deal, you have to bet the number of tricks you will take.
The trump changes every deal, and is decided randomly. (In a physical version of the game, you return the first un-distributed card of the deck. We simulate this by showing a card that was not distributed.)
The first deal, each player receives 1 card in hand. The second deal, 2. That goes up to the point you decide, then it goes back to 1.
There is a special rule for 1 card in hand. When it happens, you don't see your card, but the cards of the other players.

When a deal starts, each player bets on the number of tricks they will take. The constraint being that the total sum of bet tricks by the players can't be the number of cards in hand.

### Points

At the end of each deal, players win or lose points according to whether they manage to fulfil their contract.
- if a player does as forecast, they get 10 points plus the number of won tricks
- if a player does not, they loses the absolute value of the difference between their bet, and the actual number of tricks they managed to take.

## How to make it work

This project uses [Electron](http://electron.atom.io/) as engine. You need to have Electron installed on your machine.
[Scala.js](https://www.scala-js.org/) creates javascript files for you to use with Electron.
Once compiled, you will have to copy-paste de files in the right folders.
- the gamemenus related files must go into electron/gamemenus/js-files
- the server related files must go into electron/server/js-files
- the main related files must go into electorn/mainprocess
- the gameplaying related files must go into electron/gameplaying/js-files

### Steps to follow (at least for Windows):
- download [Electron](http://electron.atom.io/)
- download the [scalajs-ui](https://github.com/sherpal/scalajs-ui) library
- `publishLocal` it using [sbt](http://www.scala-sbt.org/)
- download this project
- compile it using `fastOptJS` or `fullOptJS` using [sbt](http://www.scala-sbt.org/)
- copy-paste the files to the right folders
- open the Electron app and drag the electron folder in it.

Remark: the game should automatically detect whether you `fastOpt`ed or you `fullOpt`ed.

## How to play

One of the players has to create a server from the app, by going into "Create Server" and entering the port the server will be listening to. Then that player can host a game by going into "Host Game", chose a name for theirselfs, a name for the game (that basically behaves like a password, although no encryption is used), the address of the computer of the server (if the player created the server on their computer, it is localhost) and the port.
To join a game, the same information must be provided.

Note that the server can be on a computer different from the one of each player.

Remark: if you want to play with people that are not on the same wifi network as you are, you will probably have to do some extra work. Indeed, you have to set up a special NAT rule on your router, to redirect incoming packets coming through the game port. You should check your Internet access provider if you need more information regarding the procedure of setting up a NAT rule.

## GUI

The GUI is currently utterly ugly, but this comes from my lack of artistic fiber. Feel free to customize the menus and the game GUI itself.

## Patch notes

### v0.1.1
- polished the chat
- changed game gui a bit, by moving some buttons around
- the Bet window is no more modal, which allows to GUI interaction (chat, score button...) while it is open
- save connection data between sessions
- the score history window is now available from the score board
- prevent the window to close when the game is not over

### Pre-0.1.1
There is no patch note record before v0.1.1.

## Upcoming features and fixes

#### Upcoming
- in the menu, having more feedback from the server (creation/dead...)
- when clicking on launch game, ask confirmation by showing all game detail
- allow to kick someone from the game
- allow to save and load games
- drag cards on the table instead of clicking on them in order to play them (?)
- insert version control

#### Done
- save game name and player name when he or she join/host a game
- add a "card viewer", to help people for which the cards would be too small (maybe now there is the need of bigger card images)
- when it is the 1 card deal, show a decoy card to click on when it's your turn
- in game chat
