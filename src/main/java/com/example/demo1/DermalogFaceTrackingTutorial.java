package com.example.demo1;

import com.dermalog.face.common.exception.DermalogFaceSdkException;
import com.dermalog.face.icaocheck3.enums.Property;
import com.dermalog.face.recognition3.FaceEncoderType;
import com.dermalog.imageexchange.Image;
import com.dermalog.wrapper.common.exception.DermalogException;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXRadioButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class DermalogFaceTrackingTutorial extends JFrame {


    private FaceTrackingManager trackingManager;
    private DermalogFaceTrackingWebcamPanel webcamPanel; // Declare webcamPanel here
    int webcamViewWidth = 640;
    int webcamViewHeight = 480;
    private AtomicBoolean running = new AtomicBoolean(false);

    public byte[] _portraitOCRAsByte = null;
    @FXML
    public ImageView portraitRFID_Id;
    @FXML
    private AnchorPane streamView_Id;
    @FXML
    private ImageView capturedImageView;
   @FXML
   private Button Bt_Capture;
    @FXML
    private Button Bt_Recapture;
    @FXML
    private Button Bt_Save;
    @FXML
    private TableView<face_ICAO_Criteria> tabView_Id;
    @FXML
    public ImageView portraitGraphic_Id;
    ObservableList<face_ICAO_Criteria> _criteriaDataList = FXCollections.observableArrayList();
    public static HashMap<Property, JFXRadioButton> complianceFaceList;
    private com.dermalog.imageexchange.Image applicantImage;
    private com.dermalog.imageexchange.Image candidateImage;

    private final FaceRecognitionManager2 faceRecognitionManager = new FaceRecognitionManager2(FaceEncoderType.STANDARD);
    double verificationThreshold = 75;

    public DermalogFaceTrackingTutorial() throws DermalogException, DermalogFaceSdkException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        adjustUIComponentsPostCreation();


        trackingManager = new FaceTrackingManager();
        try {
            trackingManager.RestartTracker();
            applicantImage = new com.dermalog.imageexchange.Image ();
            candidateImage = new com.dermalog.imageexchange.Image ();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DermalogFaceTrackingTutorial.this,
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }


    }
    final int[] i = {1};
    @FXML
    public void initialize() throws DermalogException {
       setComplianceFaceListFromPropertiesFile();
        face_setTableView();
        try {
            initializeWebcamPanel();
        } catch (DermalogException | DermalogFaceSdkException e) {
            e.printStackTrace();
        }


        _portraitOCRAsByte = convertBufferedImageToArrayBytes(SwingFXUtils.fromFXImage(portraitGraphic_Id.getImage(), null));

        Bt_Recapture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                try {
                    resetWebcamPanel();
                } catch (DermalogException ex) {
                    ex.printStackTrace();
                } catch (DermalogFaceSdkException e) {
                    e.printStackTrace();
                }

                System.out.println("camera "+ i[0]++);
            }
        });
        Bt_Capture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                capturedImageView.setImage(SwingFXUtils.toFXImage(webcamPanel.bufferedImage, null));
                webcamPanel.stop();
                running.set(false);
                System.gc();


              byte[] _portraitPassAsBytes = convertBufferedImageToArrayBytes( webcamPanel.bufferedImage);

                System.out.println("Matching between live picture and OCR picture started at :" + LocalDateTime.now());
                try {
                    face_matching(_portraitOCRAsByte, _portraitPassAsBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        Bt_Save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HelloApplication.controller.FaceImage.setImage(SwingFXUtils.toFXImage(webcamPanel.bufferedImage, null));
                capturedImageView.setImage(null);
                System.gc();
                Stage stage = (Stage)   Bt_Save.getScene().getWindow();
                stage.close();
            }
        });

        webcamPanel.portraitLive = capturedImageView;
    }
    private void adjustUIComponentsPostCreation() {
        setTitle("Dermalog Face Tracking Tutorial");
        webcamViewWidth = 640;
        webcamViewHeight = 480;
    }

// In DermalogFaceTrackingTutorial.java

    private void initializeWebcamPanel() throws DermalogException, DermalogFaceSdkException {
            Dimension imageSize = new Dimension(webcamViewWidth, webcamViewHeight);
            webcamPanel = new DermalogFaceTrackingWebcamPanel(trackingManager, 0, imageSize);
        SwingNode swingNode = new SwingNode();
        createSwingContent(swingNode);
        streamView_Id.getChildren().add(swingNode);
        new Thread(webcamPanel).start();

    }
    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(webcamPanel);
        });
    }

    void stop_camera() throws DermalogException, DermalogFaceSdkException {

       capturedImageView.setImage(SwingFXUtils.toFXImage(webcamPanel.bufferedImage, null));
       HelloApplication.controller.FaceImage.setImage(SwingFXUtils.toFXImage(webcamPanel.bufferedImage, null));
        webcamPanel.stop();
        running.set(false);
        System.gc();
        Stage stage = (Stage)   Bt_Capture.getScene().getWindow();
        stage.close();
    }

void resetWebcamPanel() throws DermalogException, DermalogFaceSdkException {
    running.set(true);

    if (webcamPanel != null) {
        System.out.println("hereee reset not null");
        webcamPanel.reset();  // Réinitialise l'état du webcamPanel existant
    } else {
        Dimension imageSize = new Dimension(webcamViewWidth, webcamViewHeight);
        webcamPanel = new DermalogFaceTrackingWebcamPanel(trackingManager, 0, imageSize);
        SwingNode swingNode = new SwingNode();
        createSwingContent(swingNode);
        streamView_Id.getChildren().add(swingNode);
    }

    pack();
    System.out.println("camera "+ i[0]++);

}




    private void face_setTableView() {

        TableColumn<face_ICAO_Criteria, String> critereCol = new TableColumn<>("Description");
        critereCol.setCellValueFactory(new PropertyValueFactory<face_ICAO_Criteria, String>("description"));

        TableColumn<face_ICAO_Criteria, JFXRadioButton> ledCol = new TableColumn<>("Value");
        ledCol.setCellValueFactory(new PropertyValueFactory<face_ICAO_Criteria, JFXRadioButton>("radioButton"));
        ledCol.setPrefWidth(40);
        ledCol.setStyle("-fx-alignment: CENTER;");

        this.tabView_Id.getColumns().addAll(critereCol, ledCol);
        this.tabView_Id.setItems(this._criteriaDataList);
    }

    private void setComplianceFaceListFromPropertiesFile() {
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            String propFileName = "face.properties";
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
                complianceFaceList = new HashMap<Property, JFXRadioButton>();

                for (Object key : prop.keySet()) {
                    if (Integer.parseInt(prop.getProperty(key.toString())) == 1) {
                        JFXRadioButton radioButton = new JFXRadioButton();

                        radioButtonStyle(radioButton, false);
                        Property property = Property.valueOf(key.toString());
                        complianceFaceList.put(property, radioButton);
                        _criteriaDataList.add(new face_ICAO_Criteria(property.label, radioButton));
                    }
                }
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void radioButtonStyle(JFXRadioButton radioButton, boolean enabled) {
        radioButton.setUnSelectedColor(javafx.scene.paint.Color.RED);
        radioButton.setSelected(enabled);
        radioButton.setDisable(true);
        radioButton.setStyle("-fx-opacity: 1");
    }

    public static byte[] convertBufferedImageToArrayBytes(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    public static byte[] convertBufferedImageToArrayBytes2(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
//change
    public void face_matching(byte[] applicantPortrait, byte[] candidatePortrait) throws IOException {

        try {
            applicantImage.loadImageFromBuffer(applicantPortrait);
            candidateImage.loadImageFromBuffer(candidatePortrait);

            int minFaceSize = 25;

            if ( candidateImage == null) {
                dialogAlert("Portrait not found", null, "Missing portraits candidateImage", Alert.AlertType.ERROR);
            }
            if (applicantImage == null) {
                dialogAlert("Portrait not found", null, "Missing portraits applicantImage", Alert.AlertType.ERROR);
            }

            int currentMinFaceSize = (int) (convertToBufferedImage(applicantPortrait).getWidth() / 100.0);
           int currentMinFaceSize1 = (int) (convertToBufferedImage(candidatePortrait).getWidth()  / 100.0);

            FaceRecognitionManager2.VerificationResult result = faceRecognitionManager.verify(new Image[]{applicantImage, candidateImage}, new double[]{0.2, 0.2});
            System.out.println("Matching ended at :" + LocalDateTime.now());

            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("score: " + df.format(result.score));

            if (result.score > verificationThreshold) {
                dialogAlert("Success Match", null, "Successful matching occurs with a score of : " + df.format(result.score), Alert.AlertType.INFORMATION);
            } else {
                dialogAlert("No Match found", null, "No match found, low score : " + df.format(result.score), Alert.AlertType.ERROR);
            }

        } catch (DermalogException e) {
e.printStackTrace();
            dialogAlert("Portrait not found", null, "Missing portraits here", Alert.AlertType.ERROR);
        }
    }
    public static void dialogAlert(String title, String headerText, String contentText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, contentText, ButtonType.YES);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }
    public static BufferedImage convertToBufferedImage(byte[] imageByteArray) {
        BufferedImage buffImage = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(imageByteArray);

        try {
            buffImage = ImageIO.read(bais);
        } catch (IOException e) {
        }
        return buffImage;
    }
}
