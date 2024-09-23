package com.example.demo1;

import com.dermalog.wrapper.common.exception.DermalogException;
import com.dermalog.dataexchange.BoundingBox;
import com.dermalog.dataexchange.KeyPoint;
import com.dermalog.face.detection2.Enums;
import com.dermalog.face.detection2.FaceDetector;
import com.dermalog.face.recognition3.*;
import com.dermalog.imageexchange.Image;
import com.dermalog.wrapper.common.exception.ErrorCodes;

import java.util.List;

class FaceRecognitionManager2 {

    private FaceDetector detector;
    private FaceEncoder encoder;
    private FaceTemplate template, template1;
    private FaceMatcher matcher;
    boolean isPersistentDetection1Enabled, isPersistentDetection2Enabled;
    Enums.FaceDetectionSensitivity faceDetectionSensitivity1, faceDetectionSensitivity2;


    FaceRecognitionManager2(FaceEncoderType eEncoderType) throws DermalogException {
        detector = new FaceDetector();
        encoder = new FaceEncoder(eEncoderType);
        matcher = new FaceMatcher();
        isPersistentDetection1Enabled = false;
        isPersistentDetection2Enabled = false;
        faceDetectionSensitivity1 = Enums.FaceDetectionSensitivity.MEDIUM;
        faceDetectionSensitivity2 = Enums.FaceDetectionSensitivity.MEDIUM;
    }

    void SetEncoder(FaceEncoderType eEncoderType) throws DermalogException{
        if (encoder != null)
            encoder.dispose();
        encoder = new FaceEncoder(eEncoderType);
    }

    public void SetDetectionSensitivity(Enums.FaceDetectionSensitivity faceDetectionSensitivity, int index) throws DermalogException {
        if (index == 0)
            faceDetectionSensitivity1 = faceDetectionSensitivity;
        else
            faceDetectionSensitivity2 = faceDetectionSensitivity;
    }

    public void setPerformPersistentDetection(boolean performPersistentDetection, int index) throws DermalogException {
        if (index == 0)
            isPersistentDetection1Enabled = performPersistentDetection;
        else
            isPersistentDetection2Enabled = performPersistentDetection;
    }

    VerificationResult verify(Image[] images, double[] minFaceSizes) throws DermalogException {

        VerificationResult verificationResult = null;

        detector.setMinFaceWidth(minFaceSizes[0]);
        detector.setPerformPersistentDetection(isPersistentDetection1Enabled);
        detector.setFaceDetectionSensitivity(faceDetectionSensitivity1);
        List<BoundingBox> positionArray1 = detector.findFaceBoundingBoxes(images[0]);
        if (positionArray1.size()==0)
        {
            throw new DermalogException(ErrorCodes.FPC_ERROR_FD_NO_SUBJECT_FOUND, "No face found on image 1!");
        }
        BoundingBox position1 = positionArray1.get(0);
        List<KeyPoint> facePoints1 = detector.findKeyPoints(images[0], position1);

        detector.setMinFaceWidth(minFaceSizes[1]);
        detector.setPerformPersistentDetection(isPersistentDetection2Enabled);
        detector.setFaceDetectionSensitivity(faceDetectionSensitivity2);
        List<BoundingBox> positionArray2 = detector.findFaceBoundingBoxes(images[1]);
        if (positionArray1.size()==0)
        {
            throw new DermalogException(ErrorCodes.FPC_ERROR_FD_NO_SUBJECT_FOUND, "No face found on image 1!");
        }
        BoundingBox position2 = positionArray2.get(0);
        List<KeyPoint> facePoints2 = detector.findKeyPoints(images[1], position2);

        double imageQuality1 = detector.checkFaceQuality(images[0], facePoints1);
        double imageQuality2 = detector.checkFaceQuality(images[1], facePoints2);

        template = encoder.encodeFace(images[0], facePoints1);
        template1 = encoder.encodeFace(images[1], facePoints2);

        double matchingScore = matcher.verifyTemplates(template, template1);

        verificationResult = new VerificationResult(new double[]{imageQuality1, imageQuality2}, matchingScore, position1, position2);

        return verificationResult;
    }

    void disposeAll() {
        encoder.dispose();
        matcher.dispose();
        detector.dispose();

        if(template!=null)
            template.dispose();
        if(template1!=null)
            template1.dispose();
    }

    class VerificationResult {
        double[] imageQualities;
        double score;
        BoundingBox facePosition1, facePosition2;

        VerificationResult(double[] imageQualities, double score, BoundingBox facePosition1, BoundingBox facePosition2) {
            this.imageQualities = imageQualities;
            this.score = score;
            this.facePosition1 = facePosition1;
            this.facePosition2 = facePosition2;
        }
    }
}
