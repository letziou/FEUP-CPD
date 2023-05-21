To run, first start by compiling javac GameServer.java GameClient.java on the terminal or command prompt. Then run the java GameServer, then run java GameClient, since there need to be a server running before a client can connect to it.
When registering a new user, that user must first play a normal game only then can he play a rancked match.
User data can be edited in the users.txt file, such as the username, password, and rank.
the ranks are split into 4 groups, 0-250, 250-500, 500-750, and 750-1000, each win grants 25 points, and each loss deducts 25 points.
The games are played by 3 players, when 3 are ready, 1 at a time will pick a number from 1 to 20, the player with the highest number wins, and the other 2 lose.