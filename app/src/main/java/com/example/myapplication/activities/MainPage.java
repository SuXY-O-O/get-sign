package com.example.myapplication.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Range;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.client.Sender;
import com.example.myapplication.trays.PicToSend;
import com.example.myapplication.trays.PicToShow;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class MainPage extends AppCompatActivity {
    private boolean nonStop;
    @SuppressLint("HandlerLeak")
    public Handler globalHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case -1:
                    toFailPage();
                    break;
                case -2:
                    toFailPage2();
                    break;
                case 2:
                    if (nonStop) {
                        Bitmap bitmap = picToShow.getBitmap();
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                    break;
                case 3:
                    count++;
                    TextView textView = findViewById(R.id.textView10);
                    String test = count + " GET";
                    textView.setText(test);
                    break;
                case 4:
                    /*byte[] bytes = picToSend.getWaiting();
                    int size = picToSend.getSize();
                    ImageView imageView3 = findViewById(R.id.imageView3);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, size);
                    if (bitmap != null) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90, bitmap.getWidth(), bitmap.getHeight());
                        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        imageView3.setImageBitmap(bitmap1);
                    }*/
                    break;
            }
        }
    };
    private Handler sendHandler = Sender.getMyHandler();
    private CameraManager cameraManager;
    private CaptureRequest.Builder previewBuilder;
    private String cameraId;
    private ImageReader imageReader;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private ImageView imageView;
    private static long count;
    private PicToSend picToSend = new PicToSend();
    private PicToShow picToShow = new PicToShow();
    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            int size = buffer.remaining();
            byte[] bytes = new byte[size];
            //byte[] copy = new byte[size];
            buffer.get(bytes);
            //System.arraycopy(bytes, 0, copy, 0, size);
            picToSend.addIn(bytes, size);
            globalHandler.sendEmptyMessage(4);
            sendHandler.sendEmptyMessage(5);
            image.close();
            buffer.clear();
        }
    };
    private Semaphore cameraLock = new Semaphore(1);
    private final CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            cameraLock.release();
            MainPage.this.cameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraLock.release();
            cameraDevice.close();
            MainPage.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };
    private CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    toFailPage2();
                    try {
                        session.stopRepeating();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        MainHandler.setHandler(globalHandler);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        count = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        imageView = findViewById(R.id.imageView2);
        nonStop = true;
        System.out.println("open camera");
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        stopBackgroundThread();
        picToSend.setKill(true);
    }

    public void backToStart(View view) {
        nonStop = false;
        //Intent intent = new Intent(this, StartPage.class);
        //startActivity(intent);
        closeCamera();
        sendHandler.sendEmptyMessage(6);
        this.finish();
    }


    private void createCameraPreviewSession() {
        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(imageReader.getSurface());
            cameraDevice.createCaptureSession(
                    Collections.singletonList(imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                toFailPage2();
                                return;
                            }
                            captureSession = cameraCaptureSession;
                            try {
                                previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON);
                                previewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getRange());
                                previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
                                CaptureRequest mPreviewRequest = previewBuilder.build();
                                captureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, cameraHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                                toFailPage2();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            toFailPage2();
                        }
                    }, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            toFailPage2();
        }
    }

    private Range<Integer> getRange() {

        CameraCharacteristics chars = null;
        try {
            chars = cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            toFailPage2();
            e.printStackTrace();
            return null;
        }
        Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

        Range<Integer> result = null;

        assert ranges != null;
        for (Range<Integer> range : ranges) {
            if (result == null)
                result = range;
            else if (range.getLower() < result.getLower())
                result = range;
        }
        return result;
    }

    private void getCameraId() {
        try {
            //Return the list of currently connected camera devices by identifier, including cameras that may be in use by other camera API clients
            for (String cameraId : cameraManager.getCameraIdList()) {
                //Query the capabilities of a camera device
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                this.cameraId = cameraId;
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toFailPage2();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        cameraThread = new HandlerThread("CameraBackground");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        cameraThread.quitSafely();
        try {
            cameraThread.join();
            cameraThread = null;
            cameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            toFailPage2();
            return;
        }
        System.out.println("get id");
        getCameraId();
        System.out.println("set image reader");
        imageReader = ImageReader.newInstance(2048, 1024, ImageFormat.JPEG, 3);
        imageReader.setOnImageAvailableListener(onImageAvailableListener, cameraHandler);
        try {
            if (!cameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                toFailPage2();
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            System.out.println("open camera ing");
            sleep(300);
            cameraManager.openCamera(cameraId, stateCallBack, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            toFailPage2();
        } catch (InterruptedException e) {
            toFailPage2();
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
        System.out.println("open camera ed");
    }

    private void closeCamera() {
        try {
            cameraLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            toFailPage2();
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraLock.release();
        }
    }

    private void toFailPage() {
        Intent fail = new Intent(this, FailToConnect.class);
        startActivity(fail);
        this.finish();
    }

    private void toFailPage2() {
        Intent fail = new Intent(this, FailPage2.class);
        startActivity(fail);
        this.finish();
    }
}
