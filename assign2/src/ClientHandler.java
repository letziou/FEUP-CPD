import java.net.Socket;
import java.io.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String password;
    private Boolean authenticated;
    private Boolean inQueue;
    private Boolean responded;
    private Boolean inGame;
    private GameServer gameServer;
    private String choice;
    private String ranked;
    private Integer rank;

    public ClientHandler(Socket socket, GameServer gameServer) {
        this.clientSocket = socket;
        this.gameServer = gameServer;
        this.authenticated = false;
        this.inQueue = false;
        this.responded = false;
        this.inGame = false;
        gameServer.handlers.add(this);
    }

    @Override
    public void run() {
      try {
          in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          out = new PrintWriter(clientSocket.getOutputStream(), true);

          while(true) {
              choice = in.readLine();
              username = in.readLine();
              password = in.readLine();
              ranked = in.readLine();

            if(choice.equals("1")) {
              if(!authenticated){
                if(!gameServer.authenticateUser(username, password)) {
                    out.println("Authentication failed.");
                    return;
                } else out.println("Authentication successful.");{
                  out.println("Username " + username + " Password " + password);
                  authenticated = true;
                }
              }
            }

            if(choice.equals("2")) {
              while(true){
                if(!gameServer.registerUser(username, password)) {
                    out.println("Registration failed, please try a different username:");
                    username = in.readLine();
                } else out.println("Registration successful.");{
                  out.println("Username " + username + " Password " + password);
                  authenticated = true;
                  break;
                }
              }
            }

            if(ranked.equals("2"))
              rank = gameServer.getUserRank(username);
              
            if (!inQueue) {
                addUserToQueue();
                inQueue = true;
            }

            if(authenticated && inQueue) {
                out.println("\033[31m Waiting for other players to enter");
              break;  //TODO: kill thread after breaking maybe? idk
            }

          } 
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

    public void addUserToQueue() {
      if(ranked.equals("1"))
        out.println("You have joined the queue. There are " + gameServer.getPlayersSize() + " people in front of you"); 
      else {
        if(rank < 251) out.println("You have joined the queue. There are " + gameServer.getBronzePlayersSize() + " people in front of you"); 
        else if(rank < 501) out.println("You have joined the queue. There are " + gameServer.getSilverPlayersSize() + " people in front of you");
        else if(rank < 751) out.println("You have joined the queue. There are " + gameServer.getGoldPlayersSize() + " people in front of you");
        else out.println("You have joined the queue. There are " + gameServer.getDiamonPlayersSize() + " people in front of you");
      }  
      gameServer.addPerson(this);
    }

    public void outToClients(String message) {
      for (ClientHandler handler : gameServer.handlers) {
          handler.out.println(message);
      }
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

    public String getRankString() {
      return ranked;
    }

    public Integer getRank() {
      return rank;
    }

    public void setRank(Integer sum) {
      rank += sum;
      if(rank < 0) rank = 0;
      if(rank > 1000) rank = 1000;
    }
  
    public BufferedReader getIn() {
      return in;
    }

    public void sendMessage(String message){
      out.println(message);
    }

    public int getPlayerIndex() {
      return gameServer.handlers.indexOf(this);
    }

    public Boolean getResponse() {
      return this.responded;
    }

    public void setResponse(Boolean state) {
      responded = state;
    }

    public void setInGame(Boolean inGame) {
      this.inGame = inGame;
    }

    public Boolean isAuthenticated() {
      return authenticated;
  }

    public Boolean getInGame() {
        return inGame;
    }

    public void updateRank(ClientHandler client){
      gameServer.updateRank(client);
    }

    public void drawSprite(int n) {
      int espaços = (n-1)/2;
      int espaçosmax = n;
      int asteriscos = 1;
  
      while(asteriscos <= n){     
        for(int i=1;i<=espaços;i++)
        out.print('.');
  
        for(int j=1;j<=asteriscos;j++)
        out.print('#');
  
        for(int l=(n+1)/2;l<espaçosmax;l++)
        out.print('.');
  
        out.println();
  
        asteriscos += 2;
        espaços--;
        espaçosmax--;
      }
      espaços = 1;
      asteriscos -= 4;
      espaçosmax = (n+1)/2;
      while(asteriscos >= 1){
        for(int k=1;k<=espaços;k++)
        out.print('.');
  
        for(int g=1;g<=asteriscos;g++)
        out.print('#');
  
        for(int m=(n+1)/2;m<=espaçosmax;m++)
        out.print('.');
  
        out.println();
  
        asteriscos -=2;
        espaços++;
        espaçosmax++;
      }
    }
}
