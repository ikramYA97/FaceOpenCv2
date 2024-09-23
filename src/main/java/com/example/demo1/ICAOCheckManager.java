package com.example.demo1;

import com.dermalog.dataexchange.BoundingBox;
import com.dermalog.dataexchange.KeyPoint;
import com.dermalog.face.common.exception.DermalogFaceSdkException;
import com.dermalog.face.detection2.FaceDetector;
import com.dermalog.face.icaocheck3.ICAOChecker;
import com.dermalog.face.icaocheck3.ICAOResult;
import com.dermalog.face.icaocheck3.enums.Enums;
import com.dermalog.face.icaocheck3.enums.Property;
import com.dermalog.face.icaocheck3.enums.Settings;
import com.dermalog.imageexchange.Image;
import com.dermalog.wrapper.common.exception.DermalogException;
import com.dermalog.wrapper.common.exception.ErrorCodes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ICAOCheckManager {

    private ICAOChecker icaoChecker;
    private ICAOResult resultHandle;
    private Image sourceImage;
    private FaceDetector detector;
    private double minFaceWidth = 0.15;

    List<String> propertyExclusions = new ArrayList<String>();


    ICAOCheckManager() throws DermalogException {
        sourceImage = new Image();
        createPropertyExclusions();
        icaoChecker = new ICAOChecker();
        resultHandle = new ICAOResult();
        detector = new FaceDetector();

        detector.setFaceDetectionSensitivity(com.dermalog.face.detection2.Enums.FaceDetectionSensitivity.HIGH);
        detector.setMinFaceWidth(0.05);
        icaoChecker.setStanceMode(Enums.StanceMode.NONE);
    }

    public void setBrighteningMode(Enums.BrighteningMode brighteningModeValue) throws DermalogException {
        icaoChecker.setBrighteningMode(brighteningModeValue);
    }

    void disposeAll() {
        icaoChecker.dispose();
        resultHandle.dispose();
        sourceImage.dispose();
        detector.dispose();
    }

    ICAOChecker getCheckHandle() {
        return icaoChecker;
    }

    void resetResultHandle() throws DermalogException {
        if (resultHandle != null)
            resultHandle.dispose();

        resultHandle = new ICAOResult();
    }

    private void createPropertyExclusions() {
//        propertyExclusions.add(Property.FIC3_PR_RESULT_SINGLE_FACE.value);
//        propertyExclusions.add(Property.FIC3_PR_RESULT_POSE_OK.value);
//        propertyExclusions.add(Property.FIC3_PR_RESULT_IS_PORTRAIT_IMAGE_INTERPOLATED.value);
//        propertyExclusions.add(Property.FIC3_PR_RESULT_NO_REFLECTION.value);
//        propertyExclusions.add(Property.FIC3_PR_RESULT_NO_SUNGLASSES.value);
//        propertyExclusions.add(Property.FIC3_PR_RESULT_EYES_OPEN.value);
        propertyExclusions.add(Settings.FIC3_PR_STANCE_MODE.value);
        propertyExclusions.add(Settings.FIC3_PR_BACKGROUND_RGB_RED.value);
        propertyExclusions.add(Settings.FIC3_PR_BACKGROUND_RGB_GREEN.value);
        propertyExclusions.add(Settings.FIC3_PR_BACKGROUND_RGB_BLUE.value);
        propertyExclusions.add(Settings.FIC3_PR_SEPARATED_PORTRAIT_RETURN_ALPHA.value);

        propertyExclusions.add(Settings.FIC3_PR_PORTRAIT_IMAGE_WIDTH.value);
        propertyExclusions.add(Settings.FIC3_PR_ENHANCED_IMAGE_AS_COLOR.value);
        propertyExclusions.add(Settings.FIC3_PR_PORTRAIT_IMAGE_TYPE.value);
        propertyExclusions.add(Settings.FIC3_PR_PRIORITY_MODE.value);
        propertyExclusions.add(Settings.FIC3_PR_BRIGHTENING_MODE.value);
        propertyExclusions.add(Settings.FIC3_PR_ENGRAVER_SEPARATION_ENABLED.value);

       // propertyExclusions.add(Property.FIC3_PR_RESULT_RESOLUTION_OK.value);
        //propertyExclusions.add(Property.FIC3_PR_RESULT_ICAO_PASS.value);
       // propertyExclusions.add(Property.FIC3_PR_RESULT_FACE_SHADOW_OK.value);
        //propertyExclusions.add(Property.FIC3_PR_RESULT_MOUTH_CLOSED.value);
      //  propertyExclusions.add(Property.FIC3_PR_RESULT_BACKGROUND_OK.value);
      //  propertyExclusions.add(Property.FIC3_PR_RESULT_FOCUS_OK.value);
     //   propertyExclusions.add(Property.FIC3_PR_RESULT_NON_OCCLUDED.value);
    //    propertyExclusions.add(Property.FIC3_PR_RESULT_GEOMETRIC_CONSTRAINTS_FULL_FRONTAL_OK.value);
       // propertyExclusions.add(Property.FIC3_PR_RESULT_EXPRESSION_NEUTRAL.value);
        propertyExclusions.add(Property.FIC3_PR_RESULT_ANGLES_YAW.value);
        propertyExclusions.add(Property.FIC3_PR_RESULT_ANGLES_PITCH.value);
        propertyExclusions.add(Property.FIC3_PR_RESULT_ANGLES_ROTATION.value);
    }

    class PortraitImageResponse {
        public BufferedImage image;
        public boolean showWarning = false;
        public boolean showErrorMessage = false;
    }

    /**
     * @param minFaceWidth the minFaceWidth
     * @throws DermalogException it is an exception
     */
    void setMinFaceSize(double minFaceWidth) throws DermalogException {
        this.minFaceWidth = minFaceWidth;
    }

    /**
     * @param eType the portrait image type
     * @throws DermalogException it is an exception
     */
    void setPortraitImageType(Enums.PortraitImageType eType) throws DermalogException{
        icaoChecker.setPortraitImageType(eType);
    }

    Image enhanceImage() throws DermalogException  {
        Image enhancedImage, portraitImage;
        portraitImage = icaoChecker.getPortraitImage( resultHandle, Enums.PortraitImageBackgroundSeparation.NO_SEPARATION);
        enhancedImage = icaoChecker.enhanceImageForEngraver(portraitImage);
        portraitImage.dispose();

        return enhancedImage;
    }

    void setEnhanceColorMode(boolean isColor) throws DermalogException {
        icaoChecker.setReturnEnhancedImageAsColor(isColor);
    }

    void setImage(Image image) {
        sourceImage = image;
    }

    byte[] loadImage(String filePath) throws DermalogException {
        sourceImage.dispose();
        sourceImage = new Image();
        sourceImage.loadImageFromFile(filePath);
        return sourceImage.getBuffer();
    }


    static class CheckImageResponse {

        List<BoundingBox> facePositions;
        List<KeyPoint> facePoints;

        void setFacePositions(List<BoundingBox> facePositions) {
            this.facePositions = facePositions;
        }

        void setFacePoints(List<KeyPoint> facePoints) {
            this.facePoints = facePoints;
        }
    }

    Image seperatePortraitImage() throws DermalogException, IOException {
        checkFace();
        return icaoChecker.getPortraitImage(resultHandle, Enums.PortraitImageBackgroundSeparation.ADAPTIVE_SEPARATION);
    }

    public CheckImageResponse checkFace() throws DermalogException {

        CheckImageResponse response = new CheckImageResponse();

        List<BoundingBox> faces = detector.findFaceBoundingBoxes(sourceImage, this.minFaceWidth);
//        if (faces.size()==0)
//        {
//          //  throw new DermalogException(ErrorCodes.FPC_ERROR_FD_NO_SUBJECT_FOUND, "Check could not performed, because no face was found");
//        }
        List<KeyPoint> facePointArray = detector.findKeyPoints(sourceImage, faces.get(0));
        resultHandle.dispose();

        resultHandle = icaoChecker.checkFace(sourceImage, faces, facePointArray);
        response.facePositions = faces;
        response.facePoints = facePointArray;
        return response;
    }

    PortraitImageResponse getPortraitImage() throws DermalogException, IOException {
        Image portraitImage = new Image();
        PortraitImageResponse response = new PortraitImageResponse();
        try {
            try {
                portraitImage = icaoChecker.getPortraitImage( resultHandle, Enums.PortraitImageBackgroundSeparation.NO_SEPARATION);
            }
            catch (DermalogException e) {
                if (e.getError() == ErrorCodes.FPC_ERROR_ICAO_NO_RESULT_IMAGE_AVAILABLE) {
                    checkFace();
                    portraitImage = icaoChecker.getPortraitImage(resultHandle, Enums.PortraitImageBackgroundSeparation.NO_SEPARATION);
                } else {
                    throw e;
                }
            }
            byte[] imageBytes = portraitImage.getBuffer();
            portraitImage.dispose();

            InputStream in = new ByteArrayInputStream(imageBytes);
            response.image = ImageIO.read(in);

        } catch (DermalogException e) {
            if (e.getError() == ErrorCodes.FPC_ERROR_FD_NO_SUBJECT_FOUND) {
                response.showErrorMessage = true;
            } else {
                if (portraitImage.getHandleValue() != null) {
                    portraitImage.dispose();
                }
                throw new DermalogException(e.getError(), e.getMessage());
            }
        }

        return response;
    }
    //this
    byte[] loadImageFromBuffer(byte[] imageData) throws DermalogFaceSdkException, IOException, DermalogException {
        if (sourceImage == null) {
            sourceImage = new Image(); // Ensure sourceImage is initialized if null
        }
        if (imageData == null) {
            throw new IllegalArgumentException("Image data is null."); // Throw an exception if imageData is null
        }
        sourceImage.loadImageFromBuffer(imageData); // Load image data into sourceImage

        return sourceImage.getBuffer(); // Return the buffer from the sourceImage
    }

    boolean getPropertyResult(Property property) throws DermalogException {
        return resultHandle.getPropertyState(property);
    }
}
