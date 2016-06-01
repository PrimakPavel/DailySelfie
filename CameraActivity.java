package su.moy.chernihov.dailyselfie;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CameraActivity extends Activity {


    private static final String TAG = "AudioVideoCameraActivity";
    private Camera mCamera;
    private LinearLayout mFrame;
    private ImageButton mBtnCancel, mBtnOk, mBtnTakePicture;
    private SurfaceHolder mSurfaceHolder;
    private boolean mIsPreviewing;
    private byte[] mData;
    private static final String CAMERA_DIR = "/Camera";
    private static final String SIMPLY_DATA_FORMAT_FOR_FILE_NAME = "'/'yyyyMMdd'_'HHmmss'.jpg'";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        mFrame = (LinearLayout) findViewById(R.id.frame);
        // Disable touches on mFrame
        mFrame.setEnabled(false);


        mBtnCancel = (ImageButton) findViewById(R.id.camera_activity_btn_cansel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnOk.setVisibility(View.INVISIBLE);
                finish();
            }
        });

        mBtnOk = (ImageButton) findViewById(R.id.camera_activity_btn_ok);
        mBtnOk.setVisibility(View.INVISIBLE);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData != null && mData.length > 0) {
                    saveToFile(mData);
                }
                mBtnOk.setVisibility(View.INVISIBLE);
                finish();
            }
        });

        mBtnTakePicture = (ImageButton) findViewById(R.id.camera_activity_btn_take_picture);


        mBtnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsPreviewing) {
                    mCamera.takePicture(mShutterCallback, null,
                            mPictureCallback);
                    mBtnOk.setVisibility(View.VISIBLE);
                    mBtnTakePicture.setImageResource(R.drawable.ic_cached_black_48dp);
                }
                else {
                    mBtnTakePicture.setImageResource(R.drawable.ic_camera_black_48dp);
                    mBtnOk.setVisibility(View.INVISIBLE);
                    startPreview();
                }

            }
        });

        // Setup SurfaceView for previewing camera image
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraView);

        // Get SurfaceHolder for accessing the SurfaceView's Surface
        mSurfaceHolder = surfaceView.getHolder();

        // Set callback Object for the SurfaceHolder
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null == mCamera) {
            try {

                // Returns first back-facing camera or null if no camera is
                // available.
                // May take a long time to complete
                // Consider moving this to an AsyncTask
                mCamera = Camera.open();

            } catch (RuntimeException e) {
                //not
            }

            // Ensure presence of camera or finish()
            if (null == mCamera)
                finish();
        }
    }

    @Override
    protected void onPause() {

        // Disable touches on mFrame
        mFrame.setEnabled(false);

        // Shutdown preview
        stopPreview();

        // Release camera resources
        releaseCameraResources();

        super.onPause();

    }

    // Start the preview
    private void startPreview() {
        if (null != mCamera) {
            try {
                mCamera.startPreview();
                mIsPreviewing = true;
            } catch (Exception e) {
                //not
            }
        }
    }

    // Shutdown preview
    private void stopPreview() {
        if (null != mCamera && mIsPreviewing) {
            try {
                mCamera.stopPreview();
                mIsPreviewing = false;
            } catch (Exception e) {

            }
        }
    }

    // Release camera so other applications can use it.
    private void releaseCameraResources() {
        if (null != mCamera) {
            mCamera.release();
            mCamera = null;
        }
    }

    // SurfaceHolder callback Object
    SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // Do nothing
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

            if (mSurfaceHolder.getSurface() == null) {
                return;
            }

            // Disable touches on mFrame
            mFrame.setEnabled(false);

            // Shutdown current preview
            stopPreview();

            setCameraParameters(width, height);

            // Initialize preview display
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {

            }

            // Start preview
            try {
                startPreview();
                mFrame.setEnabled(true);
            } catch (RuntimeException e) {

            }
        }

        // Change camera parameters
        private void setCameraParameters(int width, int height) {

            // Get camera parameters object
            Camera.Parameters p = mCamera.getParameters();

            // Find closest supported preview size
            Camera.Size bestSize = findBestSize(p, width, height);

            // FIX - Should lock in landscape mode?

            int tmpWidth = bestSize.width;
            int tmpHeight = bestSize.height;

            if (bestSize.width < bestSize.height) {
                tmpWidth = bestSize.height;
                tmpHeight = bestSize.width;
            }

            p.setPreviewSize(tmpWidth, tmpHeight);
            mCamera.setParameters(p);
        }

        // Determine the largest supported preview size
        private Camera.Size findBestSize(Camera.Parameters parameters,
                                         int width, int height) {

            List<Camera.Size> supportedSizes = parameters
                    .getSupportedPreviewSizes();

            Camera.Size bestSize = supportedSizes.remove(0);

            for (Camera.Size size : supportedSizes) {
                if ((size.width * size.height) > (bestSize.width * bestSize.height)) {
                    bestSize = size;
                }
            }

            return bestSize;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Do Nothing
        }
    };

    // Plays system shutter Sound
    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // Do nothing
        }
    };

    // Freeze the Preview for a few seconds and then restart the preview
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // Preview stopped by system
            mIsPreviewing = false;

            // Would normally save the image here
            mData = new byte[data.length];
            mData = Arrays.copyOf(data, data.length);


        }

    };

    private void saveToFile(byte[] data) {
        SimpleDateFormat format = new SimpleDateFormat(SIMPLY_DATA_FORMAT_FOR_FILE_NAME);

        FileOutputStream outStream = null;
        try {
            // Write to SD Card
            //текущее время
            Date date = new Date(System.currentTimeMillis());
            // название создаваемого файла
            String fileName = format.format(date);
            // директория куда будет сохраняться файл
            File file = createDirIfNotExists(Environment.DIRECTORY_DCIM + CAMERA_DIR);
            // создаю поток
            outStream = new FileOutputStream(file.getPath() + fileName);
            // записываю в поток данные
            outStream.write(data);
            // тост о успешном сохранении
            Toast.makeText(this, getString(R.string.camera_activity_save_message), Toast.LENGTH_SHORT).show();
            outStream.close();


        } catch (FileNotFoundException e) {
            Toast.makeText(this, getString(R.string.camera_activity_not_save_message), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.camera_activity_not_save_message), Toast.LENGTH_LONG).show();
        }
    }

    private File createDirIfNotExists(String path) {
        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(this, getString(R.string.camera_activity_not_save_message), Toast.LENGTH_LONG).show();
            }
        }
        return file;
    }


}
