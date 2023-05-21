import java.net.Socket;
import java.io.*;

public class GameClient {
   private Socket socket;
   private BufferedReader in;
   private static BufferedReader console;
   private PrintWriter out;
   private int nullCounter = 0;

   public GameClient(String host, int port) throws IOException {
      socket = new Socket(host, port);
      console = new BufferedReader(new InputStreamReader(System.in));
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);
   }

   private void authenticate(String username, String password, Integer ranked) throws IOException {
      out.println("1");
      out.println(username);
      out.println(password);
      out.println(ranked);
      String firstResponse = in.readLine();
      String secondResponse = in.readLine();
      System.out.println(firstResponse);
      System.out.println(secondResponse);
   }

   private void register(String username, String password, Integer ranked) throws IOException {
      out.println("2");
      out.println(username);
      out.println(password);
      out.println(ranked);
      String firstResponse = in.readLine();
      while (firstResponse.startsWith("Registration failed")) {
         System.out.println(firstResponse);
         username = console.readLine();
         out.println(username);
         String buffer = in.readLine();
         firstResponse = in.readLine();
         System.out.println(firstResponse);
         //System.out.println("buffer: " + buffer);
      }   
      String secondResponse = in.readLine();
      System.out.println(firstResponse);
      System.out.println(secondResponse);
   }

   private void addToQueue() throws IOException {
      String response = in.readLine();
      System.out.println(response);
      String wait = in.readLine();
      System.out.println(wait);
      return;
   }

   private void startGame() throws IOException {
      if(nullCounter == 3) {
         System.out.println("Server is down, please try again later");
         System.exit(0);
      }
      
      String finished;
      while(true){
         String start = in.readLine();
         if(start == null)  {
            nullCounter++;
            startGame();
         }
         while(!start.startsWith("Game starting with")){
            try {
               Thread.sleep(1000);
               start = in.readLine();
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }
         }

         nullCounter = 0;
         System.out.println(start);      
         
         String response = in.readLine();
         
         System.out.println(response);

         Integer number = null;
         System.out.println("Trying to read number ");
         while (number == null || number > 20 || number < 1) {
            try {
               System.out.println("Enter a number: ");
               String line = console.readLine();
               number = Integer.parseInt(line);
            } catch (NumberFormatException e) {
               System.out.println("Invalid input. Please enter a number.");
            }
            if (number > 20 || number < 1) {
               System.out.println("Invalid input. Please enter a number between 1 and 20.");
            }
         }

         out.println(number);
         System.out.println("After sending number:"+number);
         response = in.readLine();

         System.out.println(response);
         
         //edited

         while(true){
            String buffer;
            buffer = in.readLine();

            if(buffer.startsWith("If you want to play")) {  // Message that asks if player wants to continue
               System.out.println(buffer);
               break;
            }
            
            System.out.println(buffer);
         }
         
         String someString = "";
         while (someString == null || !someString.equals("y") || !someString.equals("n")) {
            someString = console.readLine();
            if (someString.equals("y") || someString.equals("n")) {
                  break;
            }
            else{
               System.out.println("Invalid input. Please enter y or n.");
            }
         }

         out.println(someString);

         finished = in.readLine();
         if(finished.startsWith("Good")) {
            System.out.println(finished);
            break;
         }      
      }
      System.out.println("Thanks for playing !!");
   }

   public static void main(String[] args) throws IOException {
      String host = "localhost";
      int port = 8000;
      GameClient client = new GameClient(host, port);

      int choice = 0;
      while (choice == 0){
         System.out.println("1. Login");
         System.out.println("2. Register");
         System.out.println("3. Exit");
         try {
            String line = console.readLine();
            choice = Integer.parseInt(line);
         } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
         }
         if (choice < 1 || choice > 3) {
            System.out.println("Invalid input. Please enter a number between 1 and 3.");
            choice = 0;
         }
      }
      
      if(choice == 1){
         String username = "";
         while (username == null || username.trim().isEmpty()) {
            System.out.print("Enter username: ");
            username = console.readLine();
            if (username == null || username.trim().isEmpty()) {
               System.out.println("Invalid username. Please enter a non-empty username.");
            }
         }
   
         String password = "";
         while (password == null || password.trim().isEmpty()) {
            System.out.print("Enter password: ");
            password = console.readLine();
            if (password == null || password.trim().isEmpty()) {
               System.out.println("Invalid password. Please enter a non-empty password.");
            }
         }

         int ranked = 0;
         while (ranked == 0){
            System.out.println("What type do you wanna play ?");
            System.out.println("1. Normal");
            System.out.println("2. Ranked");
            try {
               String line = console.readLine();
               ranked = Integer.parseInt(line);
            } catch (NumberFormatException e) {
               System.out.println("Invalid input. Please enter a number.");
            }
            if (ranked < 1 || ranked > 2) {
               System.out.println("Invalid input. Please pick 1 or 2.");
               ranked = 0;
            }
         }

         client.authenticate(username, password, ranked);
         client.addToQueue();
         client.startGame();
         
      }else if(choice == 2){
         String username = "";
         while (username == null || username.trim().isEmpty()) {
            System.out.print("Enter username: ");
            username = console.readLine();
            if (username == null || username.trim().isEmpty()) {
               System.out.println("Invalid username. Please enter a non-empty username.");
            }
         }
   
         String password = "";
         while (password == null || password.trim().isEmpty()) {
            System.out.print("Enter password: ");
            password = console.readLine();
            if (password == null || password.trim().isEmpty()) {
               System.out.println("Invalid password. Please enter a non-empty password.");
            }
         }

         client.register(username, password, 1);
         client.addToQueue();
         client.startGame();
      }else{
         System.exit(0);
      }
   }
}