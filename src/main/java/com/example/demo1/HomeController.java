package com.example.demo1;

import com.dermalog.face.common.exception.DermalogFaceSdkException;
import com.dermalog.wrapper.common.exception.DermalogException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HomeController {
    @FXML
    Button capture;
    @FXML
    public ImageView FaceImage;
    Stage faceStage;
    @FXML
    AnchorPane ProgressBox;
    @FXML
    ProgressIndicator progressBar1;
    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
    Scene scene = new Scene(new Group()); // Crée une scène vide avec un groupe vide comme racine
    private Parent root;

    public HomeController() throws IOException {
    }

    public void initialize() throws IOException {
progressBar1.setVisible(false);
        //   controller2 = fxmlLoader.getController();
        capture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               //   progressBar.setVisible(true);
             //  ProgressBox.setVisible(true);
          //      createCapture();
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        ProgressBox.setVisible(true);
                        createCapture();
                        return null;
                    }
                };
                Thread thread = new Thread(task);
                thread.start();
            }
       });

   //    automate();
    }




    @FXML
    public void createCapture() {

//        Task<Void> task1 = new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {

                Platform.runLater(() -> {
                  //  capture.setDisable(true);
                    FaceImage.setImage(null);
               //    ProgressBox.setVisible(true);
                //    progressBar1.setVisible(true);
                            if (faceStage == null) {
                                faceStage = new Stage();
                                faceStage.initStyle(StageStyle.UNDECORATED);
                                faceStage.setTitle("Face");
                                faceStage.setResizable(false);
                                faceStage.setScene(scene);
                                System.out.println("Nouvelle fenêtre créée.");
                            }
                        }
                );

                try {
                    if (root == null) {
                        root = fxmlLoader.load();
                        scene.setRoot(root);
                        System.out.println("Root chargée pour la première fois.");

                }else {
                    DermalogFaceTrackingTutorial controller2 = fxmlLoader.getController();
                    controller2.resetWebcamPanel();
                }
//                    DermalogFaceTrackingTutorial controller2 = fxmlLoader.getController();
//                    controller2.resetWebcamPanel();
                             Platform.runLater(() -> {
                                         faceStage.show();
                                     }     );

//            Task<Void> closeTask = new Task<Void>() {
//                @Override
//                protected Void call() throws Exception {
//                    Thread.sleep(10000); // Attendre 20 secondes
//                    Platform.runLater(() -> closeSecondStage());
//                    return null;
//                }
//            };
//            new Thread(closeTask).start();


                    faceStage.setOnCloseRequest(event -> {
                        Platform.exit();
                        System.exit(0);
                    });

                    // Mettre à jour l'interface utilisateur pour cacher la ProgressBox
                    Platform.runLater(() -> ProgressBox.setVisible(false));
                } catch (Exception e) {
                    e.printStackTrace();
                    // En cas d'erreur, cacher la ProgressBox et gérer l'exception correctement
                    Platform.runLater(() ->
                            ProgressBox.setVisible(false)
                    );
                }

              //  return null;
           // }
        };

//        // Démarrer la tâche dans un nouveau thread
//        Thread thread1 = new Thread(task1);
//        thread1.start();
 //   }
    private void closeSecondStage() {
        if (faceStage != null) {
            Platform.runLater(() -> {
                try {
                    DermalogFaceTrackingTutorial controller2 = fxmlLoader.getController();
                    controller2.stop_camera();
                } catch (DermalogException | DermalogFaceSdkException e) {
                    e.printStackTrace();
                }

                faceStage.close();
                ProgressBox.setVisible(false);
            });
        }
    }

        private void automate() {
        Task<Void> automationTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    Thread.sleep(20000); // Attendre 10 secondes avant de ré-ouvrir la deuxième page
                    Platform.runLater(() -> capture.fire());

                }
            }
        };
        new  Thread(automationTask).start();
    }

}
