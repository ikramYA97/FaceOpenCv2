package com.example.demo1;

import com.dermalog.dataexchange.Track;
import com.dermalog.dataexchange.TrackState;
import com.dermalog.face.common.exception.DermalogFaceSdkException;
import com.dermalog.face.icaocheck3.enums.Property;
import com.dermalog.imageexchange.Image;
import com.dermalog.imageexchange.ImageDataType;
import com.dermalog.wrapper.common.exception.DermalogException;
import com.jfoenix.controls.JFXRadioButton;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


public class DermalogFaceTrackingWebcamPanel extends JPanel implements Runnable {

    private FaceTrackingManager trackingManager;
    private VideoCapture videoCapture;
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private BufferedImage currentFrame;
    private int cameraIndex;
    private static final int[] BACKENDS = {Videoio.CAP_DSHOW, Videoio.CAP_MSMF, Videoio.CAP_VFW, Videoio.CAP_ANY};

    public ICAOCheckManager icaoChecker = new ICAOCheckManager();
    private final double DETECTION_SENSITIVITY_NORMAL = 0.15;
    double factor = DETECTION_SENSITIVITY_NORMAL;
    private final double SOURCE_IMAGE_WITH = 480.0;
    private final double SOURCE_IMAGE_HEIGHT = 640.0;
    public BufferedImage bufferedImage;
    public BufferedImage bufferedImageToSave;
    BufferedImage snapPortrAsBuf;
    byte[] snapPortrAsByt;
    public ImageView portraitLive = null;
    Mat frame = new Mat();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> icaoCheckFuture;

    private int frameCounter = 0;
    private static final int CHECK_INTERVAL = 10;
    private static final int BUFFER_SIZE = 640 * 480 * 3; // Adjust buffer size as needed
    private byte[] buffer = new byte[BUFFER_SIZE];

    // Constructor to initialize the webcam panel
    DermalogFaceTrackingWebcamPanel(FaceTrackingManager trackingManager, int cameraIndex, Dimension imageSize) throws DermalogException, DermalogFaceSdkException {
        this.trackingManager = trackingManager;
        this.cameraIndex = cameraIndex;
        initCamera();

        setPreferredSize(imageSize);
    }

    private void initCamera()  {
        for (int backend : BACKENDS) {
            this.videoCapture = new VideoCapture(cameraIndex, backend);

            if (this.videoCapture.isOpened()) {
                System.out.println("Camera opened with backend: " + backend);
                break;
            } else {
                System.err.println("Failed to open camera with backend: " + backend);
            }
        }

        if (!this.videoCapture.isOpened()) {
            System.out.println("Failed to open camera with index " + cameraIndex);
        }

        this.videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        this.videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        //fps
        this.videoCapture.set(Videoio.CAP_PROP_FPS, 30);
    }

    private byte[] bufferedImagetoByteArray(BufferedImage image) throws IOException {
        return ((DataBufferByte) image.getData().getDataBuffer()).getData();
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        }
        BufferedImage bufferedImage = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0, ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData());
        return bufferedImage;
    }



    @Override
    public void run() {
        while (!stopped.get()) {
            try {
                if (!videoCapture.grab()) {
                    System.err.println("Error: Could not grab frame from video capture");
                    break;
                }
                if (!videoCapture.read(frame)) {
                    System.err.println("Error: Could not read frame from video capture");
                    break;
                }
                if (!frame.empty()) {
                    currentFrame = matToBufferedImage(frame);
                    this.repaint();

//                    if (frameCounter % CHECK_INTERVAL == 0) {
//                        snapPortrAsBuf = matToBufferedImage(frame);
//                        snapPortrAsByt = convertBufferedImageToArrayBytes(snapPortrAsBuf);
//
//                        icaoCheckFuture = executorService.submit(() -> checkICAOLive(snapPortrAsByt));
                //    }
                 //   frameCounter++;

                }
            } catch (Exception e) {
                System.err.println("Exception during frame capture: " + e.getMessage());
                break;
            }
            try {
                Thread.sleep(25); // Ajoutez un délai de 10 millisecondes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (videoCapture.read(frame)) {
                snapPortrAsBuf = matToBufferedImage(frame);
                        snapPortrAsByt = convertBufferedImageToArrayBytes(snapPortrAsBuf);
                        icaoCheckFuture = executorService.submit(() -> checkICAOLive(snapPortrAsByt));

     //        checkICAOLive(snapPortrAsByt);
            } else {
                System.err.println("Failed to capture image");
            }
        }
        stopped.set(true);
        frame.release();
    }





    public void stop() {
        stopped.set(true);
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void reset() throws DermalogException {

      stopped.set(false);
        initCamera();
        new Thread(this).start();
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentFrame != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(currentFrame, 0, 0, this);

            // Perform tracking on the current frame
            try {
                byte[] imageBytes = bufferedImagetoByteArray(currentFrame);
                Image image = new Image();
                image.loadImageFromRawData(imageBytes, currentFrame.getHeight(), currentFrame.getWidth(), 3, ImageDataType.IMDATATYPE_UCHAR, 0);
                trackingManager.trackOnImage(image);

                List<Track> currentTracks = trackingManager.getTracks();
                if (!currentTracks.isEmpty()) {
                    Track track = currentTracks.get(0); // Only take the first detected track
                    int trackId = track.getTrackId();
                    double height = track.getHeight();
                    double width = track.getWidth();
                    int upperLeftX = (int) (track.getX() - Math.round(width / 2.0));
                    int upperLeftY = (int) (track.getY() - Math.round(height / 2.0));
                    TrackState trackState = track.getTrackState();
                    Date startDate = new Date(track.getStartTimestamp());

                    g2d.setStroke(new BasicStroke(3.0f));
                    String title = "Track: " + trackId + " - found at ";
                    Format formatter = new SimpleDateFormat("HH:mm:ss");
                    title += formatter.format(startDate);
                    Color titleColor = Color.orange;
                    if (trackState == TrackState.TRACKSTATE_UPDATED || TrackState.TRACKSTATE_NEW.equals(trackState)) {
                        g2d.setColor(Color.green);
                        titleColor = Color.cyan;
                    } else {
                        g2d.setColor(Color.red);
                    }
                    g2d.drawRect(upperLeftX, upperLeftY, (int) width, (int) height);
                    g2d.setColor(titleColor);
                    g2d.drawString(title, upperLeftX, upperLeftY - 10);
                }
                image.dispose();
            } catch (IOException | DermalogException e) {
                e.printStackTrace();
            } finally {
                System.gc();
            }
        }
    }

    private void checkICAOLive(byte[] snapPortrAsByt) {
        try {
            icaoChecker.loadImageFromBuffer(snapPortrAsByt);
            try {
                icaoChecker.setMinFaceSize(factor);

                if (icaoChecker.getCheckHandle() != null) {
                    ICAOCheckManager.CheckImageResponse response = icaoChecker.checkFace();
                    response.facePoints.clear();
                    response.facePositions.clear();
                }

                ICAOCheckManager.PortraitImageResponse portraitResponse = icaoChecker.getPortraitImage();

                if (portraitResponse.showErrorMessage) {
                    System.out.println("No Face found!");
                    //Util.dialogAlert("No Face found!", null, "Error capture photo", Alert.AlertType.ERROR);
                } else {
                    BufferedImage icon = new BufferedImage((int) SOURCE_IMAGE_WITH / 2 - 5,
                            (int) SOURCE_IMAGE_HEIGHT / 2 - 5, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g3 = icon.createGraphics();

                    double scaleSx = (SOURCE_IMAGE_WITH / 2.0 - 5) / portraitResponse.image.getWidth();
                    double scaleSy = (SOURCE_IMAGE_HEIGHT / 2.0 - 5) / portraitResponse.image.getHeight();

                    g3.scale(scaleSx, scaleSy);

                    g3.drawImage(portraitResponse.image, 0, 0, null);
                    g3.dispose();
                    bufferedImageToSave = portraitResponse.image;

                    bufferedImage = icon;
                    if (icaoChecker.getCheckHandle() != null) {
                        boolean isCompliant = true;

                        for (Map.Entry<Property, JFXRadioButton> element : DermalogFaceTrackingTutorial.complianceFaceList.entrySet()) {
                            boolean enabled = icaoChecker.getPropertyResult(element.getKey());

                            isCompliant = isCompliant && (enabled); // || icaoChecker.getCheckHandle() == null);

                            final JFXRadioButton radioButton = (JFXRadioButton) element.getValue();

                            radioButtonStyle(radioButton, enabled);
                        }

                        if (isCompliant) {
//                        System.out.println("is compliant");
//                        if (portraitLive.getImage() == null)
//                            portraitLive.setImage(SwingFXUtils.toFXImage(icon, null));
//
//                        stop();
                        }
                    }

                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //   System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DermalogFaceSdkException | DermalogException e) {
            e.printStackTrace();
        }

        //  icaoChecker.disposeAll();
        System.gc();
    }

    public static byte[] convertBufferedImageToArrayBytes(BufferedImage bufferedImage)  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    public static void radioButtonStyle(JFXRadioButton radioButton, boolean enabled){
        radioButton.setUnSelectedColor(javafx.scene.paint.Color.RED);
        radioButton.setSelected(enabled);
        radioButton.setDisable(true);
        radioButton.setStyle("-fx-opacity: 1");
    }

}
