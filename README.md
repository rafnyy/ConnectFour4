# 98Point6 Drop-Token: At-home interview question for BE engineers #
We would like you to implement a backend (REST web-service) that allows playing the game of 9dt, or 98point6 drop token. This should allow the players to create games, post moves, query moves and get state of games.
## Rules of the Game ##
Drop Token takes place on a 4x4 grid. A token is dropped along a column and said token goes to the lowest unoccupied row of the board. A player wins when they have 4 tokens next to each other either along a row, in a column, or on a diagonal. If the board is filled, and nobody has won then the game is a draw. Each player takes a turn, starting with player 1, until the game reaches either win or draw. If a player tries to put a token in a column that is already full, that results in an error state, and the player must play again until the play a valid move.
## Example Game
![samplegame](https://github.com/rafastealth/9dt-mobile/blob/master/sample_game.png)
## Minimal Requirements: ##
* Each game is between *k = 2* individuals, basic board size is 4x4 (number of columns x number of rows)
* A player can quit a game at every moment while the game is still in progress. The game will continue as long as there are 2 or more active players and the game is not done. In case only a single player is left, that player is considered the winner.
* The backend should validate that a move move is valid (it's the player's turn, column is not already full)
* The backend should identify a winning state.
* Multiple games may be running at the same time.

## API ##
### GET /drop_token - Return all in-progress games. ###
  * Output
```
 { "games" : ["gameid1", "gameid2"] }
```
  *  #### Status codes ####
    * 200 - OK. On success

### POST /drop_token - Create a new game. ###
  * Input:
```
{ "players": ["player1", "player2"],
  "columns": 4,
  "rows": 4
}
```
  * Output:
 ```
 { "gameId": "some_string_token"}
 ```
  * #### Status codes ####
    * 200 - OK. On success
    * 400 - Malformed request

### GET /drop_token/{gameId} - Get the state of the game. ###
  * output:
```
{ "players" : ["player1", "player2"], # Initial list of players.
  "state": "DONE/IN_PROGRESS",
  "winner": "player1", # in case of draw, winner will be null, state will be DONE.
                       # in case game is still in progess, key should not exist.
}
```
  * #### Status codes ####
    * 200 - OK. On success
    * 400 - Malformed request
    * 404 - Game/moves not found.

### GET /drop_token/{gameId}/moves- Get (sub) list of the moves played. ###
Optional Query parameters: **GET /drop_token/{gameId}/moves?start=0&until=1**.
  * Output:
```
{
  "moves": [{"type": "MOVE", "player": "player1", "column":1}, {"type": "QUIT", "player": "player2"}]
}
```
  * #### Status codes ####
    * 200 - OK. On success
    * 400 - Malformed request
    * 404 - Game/moves not found.

### POST /drop_token/{gameId}/{playerId} - Post a move. ###
  * Input:
```
{
 "column" : 2
}
```
  * Output:
```
{
  "move": "{gameId}/moves/{move_number}"
}
```
  * #### Status codes ####
    * 200 - OK. On success
    * 400 - Malformed input. Illegal move
    * 404 - Game not found or player is not a part of it.
    * 409 - Player tried to post when it's not their turn.


### GET /drop_token/{gameId}/moves/{move_number} - Return the move. ###
 * Output:
```
{
  "type" : "MOVE",
  "player": "player1",
  "column": 2
}
```
 * #### Status codes ####
    * 200 - OK. On success
    * 400 - Malformed request
    * 404 - Game/moves not found.

### DELETE /drop_token/{gameId}/{playerId} - Player quits from game. ###
 * #### Status codes ####
   * 202 - OK. On success
   * 404 - Game not found or player is not a part of it.
   * 410 - Game is already in DONE state.

## Assessment and Interview ##
 After we receive your submission we will conduct a code review and execute our suite of integration tests that assert the correctness of the API. Through the course of our review and testing we will assess your implementation on several different criteria:

 * _Correctness:_ Does your API adhere to the specification
 * _Robustness:_ Does your implementation handle malformed, edge case, or fuzzed input without failing and while returning meaningful messages on the cause of the failure?
 * _Readability:_ Can an engineer unfamiliar with your implementation read and understand what you wrote with sufficient depth to make modifications? This criteria speaks to style, naming conventions, organization, and comments.
 * _Scalability:_ Will your solution perform under stress of hundreds-to-thousands of games.

 On the day of your on-site interview you will present your solution to 2-3 members of the engineering team. You should prepare to talk about your implementation approach, design trade offs and approach to testing and validation.

 Through the course of the one-on-one interviews we will ask you further questions about how you would extend your service implementation and how you would fix any issues we find in our own testing to improve your solution.

## Submitting your solution ##

Please submit your source code and instructions for building/running it to your 98point6 contact.

To submit the source code, the preferred way is to share a Github or BitBucket repository with us. Alternatively, we can accept compressed tarballs or zip archives. We cannot accept those over email, though, so we recommend a file sharing service like Google Drive, Dropbox, or similar.

**Please submit your solution by 12pm (noon) the day before your interview.**

A starting point shim for your code is provided in the hope it will reduce boiler-plate code and some ramp-up in unknown technologies. Feel free to use/ignore it.

If you choose not to use the provided shim, you must provide a thorough instructions on how to setup and run your service. We are experienced developers, but we may not be familiar with the tools or languages you used, so please draft the instructions accordingly.

For .NET solutions, we will compile and run your code using [mono](http://www.mono-project.com).
