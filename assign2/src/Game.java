import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;


public class Game {
    private List<ClientHandler> players;
    private List<ClientHandler> buffer = new ArrayList<>();

    public Game(List<ClientHandler> players) {
        this.players = players;
    }

    public List<ClientHandler> start() throws IOException {
        int nPlayers = players.size();
        
        int numbers[] = new int[nPlayers];
        String usernames[] = new String[nPlayers];

        for (ClientHandler handler : players) {
          System.out.println(handler.getUsername());
          handler.setResponse(false); 
          if (!handler.getResponse()) {  // Only wait for a response if the client hasn't already responded
              handler.sendMessage("Game starting with " + nPlayers + " players.");
              handler.sendMessage("Please enter a number from 1 to 20");

              int number = 0;
              String username = handler.getUsername();

              while(!handler.getResponse()){
                  try{
                      number = Integer.parseInt(handler.getIn().readLine());
                  }catch (NumberFormatException e) {
                      System.out.println("This isn't a number " + e + ", I'm really disapointed");
                  }
                  if(number >= 1 && number <= 20) {
                      handler.setResponse(true);
                      numbers[players.indexOf(handler)] = number;
                      usernames[players.indexOf(handler)] = username;
                  }
              }
          }
        }

        int maxN = 0; 
        int minN = 30;
        String winner = "";
        String loser = "";

        for (int i = 0; i < nPlayers; i++) {
            if (numbers[i] > maxN) {
                maxN = numbers[i];
                winner = usernames[i];
            }
        }

        for (int i = 0; i < nPlayers; i++) {
          if (numbers[i] < minN) {
              minN = numbers[i];
              loser = usernames[i];
          }
      }

        for (ClientHandler handler : players) {
            handler.sendMessage("The winner is " + winner + " with the number " + maxN);
            if(handler.getUsername().equals(winner)){
              handler.sendMessage("Congratulations, here is your prize!!");
              handler.drawSprite(maxN);
              if(handler.getRankString().equals("2")) handler.setRank(25);
            }
            if(handler.getUsername().equals(loser) && handler.getRankString().equals("2"))
              handler.setRank(-25);
        }

        for (ClientHandler handler : players) {
          handler.sendMessage("If you want to play again press y, otherwise press n");
          String response = handler.getIn().readLine();
          if (response.equals("y") || response.equals("Y") || response.equals("yes") || response.equals("Yes")) {
            buffer.add(handler);
            handler.sendMessage("placeholder");
            System.out.println("GAME");
          }
          else {
            if(handler.getRankString().equals("2")) handler.updateRank(handler);
            handler.sendMessage("Goodbye!!");
          }
        }
        return buffer;
    }
}
