package main;


import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

//slider
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *  Java Pacman Project - Chat App
 *  @author Luka Djogas & Rimac Karla
 */

public class ChatClient extends Application implements EventHandler<ActionEvent> {
   // Window objects
   Stage stage = null;
   Scene scene = null;
   VBox root = null;

   // GUI Components
   private TextField tfServer = new TextField("localhost");
   private TextField tfName = new TextField();
   private Button btnConnect = new Button("Connect");
   private TextArea taForMessages = new TextArea();
   private TextField tfMessageToSend = new TextField("Message to send");

   Slider slider1 = new Slider();
   Slider slider2 = new Slider();

   // socket
   private Socket socket = null;
   private ObjectOutputStream oos = null;
   private ObjectInputStream ois = null;
   private static final int SERVER_PORT = 12345;

   private int currentID = -1;

   /**
    * Main program ...
    * 
    * @args - command line arguments (ignored)
    */
   public static void main(String[] args) {
      launch(args);
   }

   /** constructor */
   public void start(Stage _stage) {
      // Window set up
      stage = _stage;
      stage.setTitle("Registration Client (YOUR_NAME)");
      final int WIDTH = 500;
      final int HEIGHT = 400;
      final int X = 50;
      final int Y = 100;
      stage.setX(X);
      stage.setY(Y);
      stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
         public void handle(WindowEvent evt) {
            System.exit(0);
         }
      });

      // Draw the GUI
      root = new VBox();

      HBox hbTop = new HBox(10);
      hbTop.getChildren().addAll(new Label("Server"), tfServer, new Label("Name"), tfName, btnConnect);

      root.getChildren().addAll(hbTop, taForMessages, tfMessageToSend, slider1, slider2);
      for (Node n : root.getChildren()) {
         VBox.setMargin(n, new Insets(5));
      }

      btnConnect.setOnAction(this);

      // Set the scene and show the stage
      scene = new Scene(root, WIDTH, HEIGHT);
      stage.setScene(scene);
      stage.show();

      // Adjust sizes
      taForMessages.setPrefHeight(HEIGHT - hbTop.getPrefHeight() - tfMessageToSend.getPrefHeight());
      tfServer.setPrefColumnCount(12);
      tfName.setPrefColumnCount(7);

      // ENTER PRESSED // import javafx.scene.input.KeyEvent;
      tfMessageToSend.setOnKeyPressed(new EventHandler<KeyEvent>() {
         @Override
         public void handle(KeyEvent t) {
            if (t.getCode() == KeyCode.ENTER) {
               sendMessage();
            }
         }
      });

      slider1.valueProperty().addListener(new ChangeListener<Number>() {
         public void changed(ObservableValue<? extends Number> ov,
               Number old_val, Number new_val) {

            if (currentID == 0) {
               System.out.println(new_val.doubleValue());
               sendSliderStatus(new_val.intValue());

            }

         }
      });
      slider2.valueProperty().addListener(new ChangeListener<Number>() {
         public void changed(ObservableValue<? extends Number> ov,
               Number old_val, Number new_val) {
            if (currentID == 1) {
               System.out.println(new_val.doubleValue());
               sendSliderStatus(new_val.intValue());
            }
         }
      });

   }

   /** Button handler */
   public void handle(ActionEvent ae) {
      Button btn = (Button) ae.getSource();
      switch (btn.getText()) {
         case "Connect":
            doConnect();
            break;
         case "Disconnect":
            doDisconnect();
            break;
      }
   }

   private void doDisconnect() {
      System.out.println("Disconnect the client");
      try {
         oos.close();
         ois.close();
         socket.close();
         btnConnect.setText("Connect");
         tfName.setDisable(false);
         tfServer.setDisable(false);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         showAlert(AlertType.ERROR, "Exception");
      }
   }

   private void sendMessage() {
      try {
         oos.writeObject("CHAT@" + tfName.getText() + ": " + tfMessageToSend.getText());
         oos.flush();
         tfName.clear();
         System.out.println("Sent" + tfName.getText() + ": " + tfMessageToSend.getText());
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void sendSliderStatus(int value) {
      Status newStatus = new Status(this.currentID, value);
      try {
         oos.writeObject(newStatus);
         oos.flush();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   class ClientThread extends Thread {
      @Override
      public void run() {

         while (true) {
            try {
               Object obj = ois.readObject();
               if (obj instanceof String) {// this is chat feedback
                  String message = (String) obj;
                  System.out.println("Feedback:" + message);
                  Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                        taForMessages.appendText(message + "\n");
                     }
                  });
               } else if (obj instanceof Status) {
                  Status newStatus = (Status) obj;
                  System.out.println("NewStatusReceived" + newStatus.toString());
                  if (newStatus.getID() != currentID) {
                     switch (newStatus.getID()) {
                        case 0:
                           Platform.runLater(new Runnable() {

                              @Override
                              public void run() {
                                 slider1.setValue(newStatus.getSliderStatus());
                              }

                           });

                           break;
                        case 1:
                           Platform.runLater(new Runnable() {

                              @Override
                              public void run() {
                                 slider2.setValue(newStatus.getSliderStatus());
                              }

                           });
                           break;
                     }
                  }
               }
            } catch (ClassNotFoundException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            } catch (EOFException eof) {
               showAlert(AlertType.WARNING, "Connection lost");
            } catch (IOException ioe) {
               showAlert(AlertType.ERROR, ioe.getMessage());
            }
         }
      }
   }

   private void doConnect() {

      try {
         this.socket = new Socket(tfServer.getText(), SERVER_PORT);

         this.oos = new ObjectOutputStream(this.socket.getOutputStream());
         this.ois = new ObjectInputStream(this.socket.getInputStream());

         this.oos.writeObject("REGISTER@" + tfName.getText());
         this.currentID = (Integer) this.ois.readObject();

         switch (this.currentID) {
            case 0:
               slider2.setDisable(true);
               break;
            case 1:
               slider1.setDisable(true);
               break;
         }

         System.out.println("Client registered successfully at ID" + this.currentID);
         btnConnect.setText("Disconnect");
         tfName.setDisable(true);
         tfServer.setDisable(true);

         // run the message receiving thread
         ClientThread th = new ClientThread();
         th.start();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      } catch (UnknownHostException e) {
         showAlert(AlertType.ERROR, e.getMessage());
      } catch (IOException e) {
         showAlert(AlertType.ERROR, e.getMessage());
      }
   }

   public void showAlert(AlertType type, String message) {
      Alert alert = new Alert(type, message);
      alert.showAndWait();
   }

}