package com.example.demo1;

import com.dermalog.dataexchange.Track;
import com.dermalog.face.detection2.FaceDetector;
import com.dermalog.face.tracking2.Enums;
import com.dermalog.face.tracking2.FaceTracker;
import com.dermalog.imageexchange.Image;
import com.dermalog.wrapper.common.exception.DermalogException;

import java.util.ArrayList;
import java.util.List;

public class FaceTrackingManager implements AutoCloseable {
    private FaceTracker faceTracker = null;
    private FaceDetector faceDetector = null;
    private List<Track> tracks;

    private int poolSize = 1;
    private Boolean preferLargerFace = false;
    private int minDetectionsBeforeReport = 0;
    private int maxExtrapolations = 4;
    private int maxTracks = -1;

    private final Object trackLock = new Object();
    private final Object resetLock = new Object();
    private int frameNumber = 0;

    public FaceTrackingManager() throws DermalogException
    {
        faceDetector = new FaceDetector();
        faceTracker = new FaceTracker(Enums.AlgorithmType.KALMAN, faceDetector, poolSize);
        faceTracker.setMaxQueueSize(-1);
        faceTracker.registerCallback(this::resultCollector);
        tracks = new ArrayList<>();
    }

    @Override
    public void close()
    {
        synchronized (resetLock) {
            if (faceTracker != null) {
                faceTracker.dispose();
                faceTracker = null;
            }
            if (faceDetector != null) {
                faceDetector.dispose();
                faceDetector = null;
            }
        }
    }

    private void resultCollector(List<Track> tracks, int frameNumber, int  errorCode) {
        synchronized (trackLock) {
            this.tracks = tracks;
        }
    }

    public void RestartTracker() throws DermalogException {
        synchronized (resetLock) {
            if (faceTracker != null) {
                faceTracker.dispose();
            }
            faceTracker = new FaceTracker(Enums.AlgorithmType.KALMAN, faceDetector, poolSize);
            faceTracker.registerCallback(this::resultCollector);
            faceTracker.setMaxQueueSize(-1);
            faceTracker.setPreferLargestFaces(preferLargerFace);
            faceTracker.setMaxTracks(maxTracks);
            faceTracker.setMinDetectionsBeforeReport(minDetectionsBeforeReport);
            faceTracker.setMaxExtrapolatedFrames(maxExtrapolations);
            frameNumber = 0;
        }
    }
    public void Reset() throws DermalogException {
        synchronized (resetLock) {
            if (faceTracker != null) {
                faceTracker.reset();
            }
        }
    }
    public void ClearTracks() {
        synchronized (trackLock) {
            this.tracks = new ArrayList<>();
        }
    }

    public void trackOnImage(Image oImage) throws DermalogException {
        synchronized (resetLock) {
            if (faceTracker != null) {
                faceTracker.update(oImage, frameNumber++, System.currentTimeMillis());
            }
        }
    }

    public List<Track> getTracks() {
        synchronized (trackLock) {
            return tracks;
        }
    }

    public void setMaxTracks(int maxTracks) throws DermalogException {
        if (faceTracker != null) {
            faceTracker.setMaxTracks(maxTracks);
        }
        this.maxTracks = maxTracks;
    }

    public void setMaxExtrapolations(int maxExtrapolations) throws DermalogException {
        if (faceTracker != null) {
            faceTracker.setMaxExtrapolatedFrames(maxExtrapolations);
        }
        this.maxExtrapolations = maxExtrapolations;
    }
    public void setMinDetectionsBeforeReport(int minDetections) throws DermalogException {
        if (faceTracker != null) {
            faceTracker.setMinDetectionsBeforeReport(minDetections);
        }
        this.minDetectionsBeforeReport = minDetections;
    }
    public void setPreferLargerFace(boolean isPreferLargerFaces) throws DermalogException{
        if (faceTracker != null) {
            faceTracker.setPreferLargestFaces(isPreferLargerFaces);
        }
        this.preferLargerFace = isPreferLargerFaces;
    }
    public void setDetectorMinFaceWidth(int minFaceWidthPercent) throws DermalogException{
        if (faceDetector != null) {
            faceDetector.setMinFaceWidth(minFaceWidthPercent / 100.0);
        }
    }
    public void setDetectorInputSize(int inputSize) throws DermalogException {
        if (faceDetector != null) {
            faceDetector.setDetectionInputSize(inputSize);
        }
    }

    public void setPoolSize(int poolSize) throws DermalogException {
        this.poolSize = poolSize;
    }

}
