package main;

import java.io.*;
import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *  Java Pacman Project - Chat App
 *  @author Luka Djogas & Rimac Karla
 */

public class ChatServer extends Application implements EventHandler<ActionEvent> {
   // Window Attributes
   private Stage stage;
   private Scene scene;
   private VBox root = null;

   // GUI components
   private TextArea taList = new TextArea();
   private Button btnClear = new Button("Clear");

   // socket
   private static final int SERVER_PORT = 12345;
   List<ObjectOutputStream> nameOfWriters = new ArrayList<>();

   int clientIDCounter = 0;

   /** Main program */
   public static void main(String[] args) {
      launch(args);
   }

   /** start the server */
   public void start(Stage _stage) {
      stage = _stage;
      stage.setTitle("Registration Server (YOUR_NAME)");
      final int WIDTH = 450;
      final int HEIGHT = 400;
      final int X = 550;
      final int Y = 100;

      stage.setX(X);
      stage.setY(Y);
      stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
         public void handle(WindowEvent evt) {
            System.exit(0);
         }
      });

      // Set up root
      root = new VBox();

      // Put clear button in North
      HBox hbNorth = new HBox();
      hbNorth.setAlignment(Pos.CENTER);
      hbNorth.getChildren().add(btnClear);

      // Set up rootis
      root.getChildren().addAll(hbNorth, taList);
      for (Node n : root.getChildren()) {
         VBox.setMargin(n, new Insets(10));
      }

      // Set the scene and show the stage
      scene = new Scene(root, WIDTH, HEIGHT);
      stage.setScene(scene);
      stage.show();

      // Adjust size of TextArea
      taList.setPrefHeight(HEIGHT - hbNorth.getPrefHeight());

      // do Server Stuff
      doServerStuff();
   }

   /** Server action */
   private void doServerStuff() {
      ServerThread st = new ServerThread();
      st.start();
   }

   // ServerThread
   class ServerThread extends Thread {
      @Override
      public void run() {
         try {
            System.out.println("Openning SOCKET PORT");
            ServerSocket sSocket = new ServerSocket(SERVER_PORT);

            while (true) {
               System.out.println("Waiting client to connect...");
               Socket cSocket = sSocket.accept();

               ClientThread cT = new ClientThread(cSocket);
               cT.start();
            }

         } catch (IOException e) {
            showAlert(AlertType.ERROR, e.getMessage());
         }
      }
   }

   // ClientThread
   class ClientThread extends Thread {
      private Socket cSocket;
      private ObjectOutputStream oos = null;
      private ObjectInputStream ois = null;

      public ClientThread(Socket cSocket) {
         this.cSocket = cSocket;
      }

      @Override
      public void run() {

         try {

            this.ois = new ObjectInputStream(this.cSocket.getInputStream());
            this.oos = new ObjectOutputStream(this.cSocket.getOutputStream());

            nameOfWriters.add(this.oos);

            while (true) {
               Object obj = ois.readObject();
               if (obj instanceof String) {
                  String message = (String) obj;
                  System.out.println("Received:" + message);
                  String[] arrayOfMessage = message.split("@");
                  System.out.println(arrayOfMessage);
                  if (arrayOfMessage.length == 2) {
                     switch (arrayOfMessage[0]) {
                        case "REGISTER":
                           System.out.println(message);
                           oos.writeObject(clientIDCounter);
                           oos.flush();
                           clientIDCounter++;
                           break;
                        case "CHAT":
                           System.out.println(message);

                           for (int i = 0; i < nameOfWriters.size(); i++) {
                              nameOfWriters.get(i).writeObject(arrayOfMessage[1]);
                              nameOfWriters.get(i).flush();
                           }
                           break;
                     }
                  }
               } else if (obj instanceof Status) {
                  Status newStatus = (Status) obj;
                  System.out.println(newStatus.toString());

                  for (int i = 0; i < nameOfWriters.size(); i++) {
                     if (nameOfWriters.get(i) != this.oos) {
                        nameOfWriters.get(i).writeObject(newStatus);
                        nameOfWriters.get(i).flush();
                     }
                  }
               }
            }
         }

         catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (EOFException eof) {
            showAlert(AlertType.WARNING, "Connection lost");
         } catch (IOException ioe) {
            showAlert(AlertType.ERROR, ioe.getMessage());
         }
         System.out.println("Client disconnected...");
      }

   }

   /** Button handler */
   public void handle(ActionEvent ae) {
   }

   public void showAlert(AlertType type, String message) {
      Platform.runLater(new Runnable() {
         public void run() {
            Alert alert = new Alert(type, message);
            alert.showAndWait();
         }
      });
   }
}