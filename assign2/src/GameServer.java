import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GameServer {
	private ServerSocketChannel serverSocketChannel;
  private int maxPlayers = 15;
  private boolean gameInProgress = false;
	public List<ClientHandler> handlers = new ArrayList<ClientHandler>();
  public List<ClientHandler> players = new ArrayList<ClientHandler>();

  public List<ClientHandler> bronzeRank = new ArrayList<ClientHandler>();
  public List<ClientHandler> silverRank = new ArrayList<ClientHandler>();
  public List<ClientHandler> goldRank = new ArrayList<ClientHandler>();
  public List<ClientHandler> diamondRank = new ArrayList<ClientHandler>();
  
  private List<ClientHandler> buffer = new ArrayList<>();
  private boolean gameStarted = false;
  private ExecutorService executorService = Executors.newFixedThreadPool(5);
  private final ReentrantLock lock = new ReentrantLock();
  

	public GameServer(int port) throws IOException {
      try {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        System.out.println("Created server socket channel");
    } catch (IOException ex) {
        System.out.println("Server error: " + ex.getMessage());
    }
  }

  public boolean registerUser(String username, String password){
    synchronized (this) {
      try {
          File file = new File("users.txt");
          BufferedReader reader = new BufferedReader(new FileReader(file));
          String line;
          boolean userExists = false;

          while ((line = reader.readLine()) != null) {
              String[] parts = line.split(":");
              String existingUsername = parts[0];
              
              if (existingUsername.equals(username)) {
                  userExists = true;//TODO nao deve ser break aqui >:(
                  break;
              }
          }

          if (!userExists) {
            // Add new user to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.newLine();
            writer.write(username + ":" + password + ":0");
            writer.close();
            
            return true; // Authentication successful
        }
      } catch (IOException e) {
          e.printStackTrace();
          System.out.println("An error occurred during authentication.");
      }
      
      return false; // Authentication failed
    }
  }

  public Integer getUserRank(String username){
    try {
      File file = new File("users.txt");
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line;
      Integer rank = -1;
      
      while ((line = reader.readLine()) != null) {
          String[] parts = line.split(":");
          String existingUsername = parts[0];
          String existingPassword = parts[1];
          String existingRanked = parts[2];
          
          if (existingUsername.equals(username)) {
            rank = Integer.parseInt(existingRanked);
            break;
          }
      }
      
      reader.close();
      return rank;
    } catch (IOException e) {
        e.printStackTrace();
        System.out.println("An error occurred during .");
    }
    return -1;
  }

  public void updateRank(ClientHandler client){
    try {
      File inputFile = new File("users.txt");
      File tempFile = new File("temp.txt");

      BufferedReader reader = new BufferedReader(new FileReader(inputFile));
      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

      String line;

      while ((line = reader.readLine()) != null) {
          String[] parts = line.split(":");
          String existingUsername = parts[0];
          String existingPassword = parts[1];
          String existingRanked = parts[2];
          
          if (!existingUsername.equals(client.getUsername())) {
            writer.write(line + System.lineSeparator());
          }
      }

      writer.write(client.getUsername() + ":" + client.getPassword() + ":" + client.getRank());
      
      writer.close();
      reader.close();

      if (inputFile.delete()) {
          if (tempFile.renameTo(inputFile)) {
              System.out.println("Line updated successfully.");
          } else {
              System.out.println("Failed to rename the temporary file.");
          }
      } else {
          System.out.println("Failed to delete the original file.");
      }
    } catch (IOException e) {
        e.printStackTrace();
    }
  }

  public boolean authenticateUser(String username, String password) {
    synchronized (this) {
      try {
          File file = new File("users.txt");
          BufferedReader reader = new BufferedReader(new FileReader(file));
          String line;
          
          while ((line = reader.readLine()) != null) {
              String[] parts = line.split(":");
              String existingUsername = parts[0];
              String existingPassword = parts[1];
              String existingRanked = parts[2];
              
              if (existingUsername.equals(username)) {
                  if (existingPassword.equals(password)) {
                      return true; // Authentication successful
                  } else {
                      return false; // Incorrect password
                  }
              }


          }
          
          reader.close();
      } catch (IOException e) {
          e.printStackTrace();
          System.out.println("An error occurred during authentication.");
      }
      
      return false; // Authentication failed
    }
}

  public Integer getPlayersSize(){
    synchronized(this){
      return players.size();
    }
  }

  public Integer getBronzePlayersSize(){
    synchronized(this){
      return bronzeRank.size();
    }
  }

  public Integer getSilverPlayersSize(){
    synchronized(this){
      return silverRank.size();
    }
  }

  public Integer getGoldPlayersSize(){
    synchronized(this){
      return goldRank.size();
    }
  }

  public Integer getDiamonPlayersSize(){
    synchronized(this){
      return diamondRank.size();
    }
  }

  public void start() throws IOException {
    while (true) {
        SocketChannel clientSocket = serverSocketChannel.accept();
        if (clientSocket != null) {
          ClientHandler handler = new ClientHandler(clientSocket.socket(), this);
          new Thread(handler).start();
        }

        if (this.getPlayersSize()>=3 && this.getPlayersSize()!=0) {
          executorService.execute(() -> {
              try {
                List<ClientHandler> playersCopy = new ArrayList<>();
                playersCopy = getPlayers(0);
                //if(players.size()==6) System.out.println("6");
                  if(playersCopy.size()==3) {
                    System.out.println("playercopy:::" + playersCopy);
                    startGame(playersCopy, 0);
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
          });
        }
        
        if (this.getBronzePlayersSize()>=3 && this.getBronzePlayersSize()!=0) {
          executorService.execute(() -> {
              try {
                List<ClientHandler> playersCopy = new ArrayList<>();
                playersCopy = getPlayers(1);
                  if(playersCopy.size()==3) {
                    System.out.println("playercopy:::" + playersCopy);
                    startGame(playersCopy, 1);
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
          });
        }

        if (this.getSilverPlayersSize()>=3 && this.getSilverPlayersSize()!=0) {
          executorService.execute(() -> {
              try {
                List<ClientHandler> playersCopy = new ArrayList<>();
                playersCopy = getPlayers(2);
                  if(playersCopy.size()==3) {
                    System.out.println("playercopy:::" + playersCopy);
                    startGame(playersCopy, 2);
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
          });
        }

        if (this.getGoldPlayersSize()>=3 && this.getGoldPlayersSize()!=0) {
          executorService.execute(() -> {
              try {
                List<ClientHandler> playersCopy = new ArrayList<>();
                playersCopy = getPlayers(3);
                  if(playersCopy.size()==3) {
                    System.out.println("playercopy:::" + playersCopy);
                    startGame(playersCopy, 3);
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
          });
        }

        if (this.getDiamonPlayersSize()>=3 && this.getDiamonPlayersSize()!=0) {
          executorService.execute(() -> {
              try {
                List<ClientHandler> playersCopy = new ArrayList<>();
                playersCopy = getPlayers(4);
                  if(playersCopy.size()==3) {
                    System.out.println("playercopy:::" + playersCopy);
                    startGame(playersCopy, 4);
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
          });
        }
    }
  }

  public List<ClientHandler> getPlayers(Integer order) {
    synchronized(this) {
      List<ClientHandler> playersCopy = new ArrayList<>();
      int playerNumber = 0; 
      
      if(order == 0){
        for (var clientHandler : players) {
          if(playerNumber == 3) break;
          if(!clientHandler.getInGame()){
            playersCopy.add(clientHandler);
            playerNumber++;
          }
        }

        if(playersCopy.size() == 3){
          for (ClientHandler clientHandler : playersCopy) {
            players.get(players.indexOf(clientHandler)).setInGame(true);
          }
        }
      }
      else if(order == 1){
        for (var clientHandler : bronzeRank) {
          if(playerNumber == 3) break;
          if(!clientHandler.getInGame()){
            playersCopy.add(clientHandler);
            playerNumber++;
          }
        }

        if(playersCopy.size() == 3){
          for (ClientHandler clientHandler : playersCopy) {
            bronzeRank.get(bronzeRank.indexOf(clientHandler)).setInGame(true);
          }
        }
      }
      else if(order == 2){
        for (var clientHandler : silverRank) {
          if(playerNumber == 3) break;
          if(!clientHandler.getInGame()){
            playersCopy.add(clientHandler);
            playerNumber++;
          }
        }

        if(playersCopy.size() == 3){
          for (ClientHandler clientHandler : playersCopy) {
            silverRank.get(silverRank.indexOf(clientHandler)).setInGame(true);
          }
        }
      }
      else if(order == 3){
        for (var clientHandler : goldRank) {
          if(playerNumber == 3) break;
          if(!clientHandler.getInGame()){
            playersCopy.add(clientHandler);
            playerNumber++;
          }
        }

        if(playersCopy.size() == 3){
          for (ClientHandler clientHandler : playersCopy) {
            goldRank.get(goldRank.indexOf(clientHandler)).setInGame(true);
          }
        }
      }
      else {
        for (var clientHandler : diamondRank) {
          if(playerNumber == 3) break;
          if(!clientHandler.getInGame()){
            playersCopy.add(clientHandler);
            playerNumber++;
          }
        }

        if(playersCopy.size() == 3){
          for (ClientHandler clientHandler : playersCopy) {
            diamondRank.get(diamondRank.indexOf(clientHandler)).setInGame(true);
          }
        }
      }

      return playersCopy;
    }
  }

  public void addPerson(ClientHandler client) {
    synchronized (this) {
      if(client.getRankString().equals("1")){
        if (players.size() < maxPlayers) {            
            if (players.contains(client)) {
              players.remove(client);
              players.add(client);
            } else {
                players.add(client);
            }         
            handlers.remove(client);        
        }
        for(var ClientHandler : players){
          System.out.println(ClientHandler.getUsername()+" "+ClientHandler.getInGame()+" "+ClientHandler.isAuthenticated());
        }
      } else {
        if(client.getRank() < 251){
          if (bronzeRank.size() < maxPlayers) {            
              if (bronzeRank.contains(client)) {
                bronzeRank.remove(client);
                bronzeRank.add(client);
              } else {
                bronzeRank.add(client);
              }         
              handlers.remove(client);        
          }
        }
        else if(client.getRank() < 501){
          if (silverRank.size() < maxPlayers) {            
              if (silverRank.contains(client)) {
                silverRank.remove(client);
                silverRank.add(client);
              } else {
                silverRank.add(client);
              }         
              handlers.remove(client);        
          }
        }
        else if(client.getRank() < 751){
          if (goldRank.size() < maxPlayers) {            
              if (goldRank.contains(client)) {
                goldRank.remove(client);
                goldRank.add(client);
              } else {
                goldRank.add(client);
              }         
              handlers.remove(client);        
          }
        }
        else {
          if (diamondRank.size() < maxPlayers) {            
              if (diamondRank.contains(client)) {
                diamondRank.remove(client);
                diamondRank.add(client);
              } else {
                diamondRank.add(client);
              }         
              handlers.remove(client);        
          }
        }
      }
    }
  }

  public void startGame(List<ClientHandler> playersInGame, Integer order) throws IOException {
    try {
      System.out.println("in game");
        Game game = new Game(playersInGame);
        buffer = game.start();
        if(buffer.size() == playersInGame.size()) {
            startGame(playersInGame, order);
        }

    } finally {
        System.out.println("unlock");
    } 
    synchronized (this) {
      List<ClientHandler> bufferCopy = new ArrayList<>(buffer);

      List<ClientHandler> missingPlayers = new ArrayList<>(playersInGame);
      missingPlayers.removeAll(bufferCopy);

      removeMissingPlayers(missingPlayers, order);

      for (ClientHandler clientHandler : bufferCopy) {
          if(players.contains(clientHandler)) System.out.println("contains");
          handlers.add(clientHandler);
          clientHandler.setInGame(false);
          System.out.println("check: "+clientHandler.getInGame());
          addPerson(clientHandler);
      }
    }
  }

  
  public void removeMissingPlayers(List<ClientHandler> missingPlayers, Integer order) {
    synchronized (this) {
      if(order == 0){
        for (ClientHandler clientHandler : missingPlayers) {
          players.remove(clientHandler);
        }
      }
      else if(order == 1){
        for (ClientHandler clientHandler : missingPlayers) {
          bronzeRank.remove(clientHandler);
        }
      }
      else if(order == 2){
        for (ClientHandler clientHandler : missingPlayers) {
          silverRank.remove(clientHandler);
        }
      }
      else if(order == 3){
        for (ClientHandler clientHandler : missingPlayers) {
          goldRank.remove(clientHandler);
        } 
      }
      else {
        for (ClientHandler clientHandler : missingPlayers) {
          diamondRank.remove(clientHandler);
        }
      }
    }
  }

  public boolean getGameStarted() {
    return this.gameStarted;
  }

  public void setGameStarted(boolean gameStarted) {
      this.gameStarted = gameStarted;
  }

  public static void main(String[] args) throws IOException {
    GameServer server = new GameServer(8000);
    server.start();
  }
}