package com.example.monobox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bxl.config.editor.BXLConfigLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AndroidCamera";
    private Button btnGallery;

    private ImageView imgBtnTake;
    private TextView timerText;
    private TextureView textureView;
    private RelativeLayout relativeLayout;
    private static final SparseIntArray Orientations = new SparseIntArray(4);

    static {
        Orientations.append(Surface.ROTATION_0,90);
        Orientations.append(Surface.ROTATION_90,0);
        Orientations.append(Surface.ROTATION_180,270);
        Orientations.append(Surface.ROTATION_270,180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;

    protected CameraManager manager;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private File folder;
    private File folder1;
    private String folderName = "MyPhotoDir";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    static int orientationNum = 0;
    Button setting1;
    Button setting2;
    Button setting3;
    Button setting4;
    Button settingSaveBtn;

    Switch dateSwitch;
    Switch logoSwitch;
    String logoPath = "";
    boolean dateAble;
    boolean logoAble;

    RelativeLayout settingBar;

    MediaActionSound sound = new MediaActionSound();

    boolean dateCheck;
    boolean logoCheck;

    int cnt1 = 0;
    int cnt2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerText = findViewById(R.id.timer);
        relativeLayout = findViewById(R.id.lay);
        textureView = findViewById(R.id.texture);

        setting1 = findViewById(R.id.setting1);
        setting2 = findViewById(R.id.setting2);
        setting3 = findViewById(R.id.setting3);
        setting4 = findViewById(R.id.setting4);

        settingSaveBtn = findViewById(R.id.saveBtn);
        dateSwitch = findViewById(R.id.switch1);
        logoSwitch = findViewById(R.id.switch2);

        Intent get = getIntent();
        dateCheck = get.getBooleanExtra("dateAble", false);
        logoCheck = get.getBooleanExtra("logoAble", false);
        logoPath = "";

        if (dateCheck) dateSwitch.setChecked(dateCheck);
        if (logoCheck) {
            logoSwitch.setChecked(logoCheck);
            if (!get.getStringExtra("path").isEmpty()) {
                logoPath = get.getStringExtra("path");

            }
        }

        if (dateSwitch.isChecked()) {
            dateAble = true;
            dateCheck = true;
        }
        if (logoSwitch.isChecked()) {
            logoAble = true;
            logoCheck = true;
        }
        dateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cnt1++;
                if (cnt1 > 0) dateAble = b;

            }
        });

        logoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cnt2++;
                if (cnt2 > 0) logoAble = b;
            }
        });

        settingSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateSwitch.isChecked()) {
                    dateSwitch.setChecked(true);
                    dateAble = true;
                }
                else {
                    dateSwitch.setChecked(false);
                    dateAble = false;
                }
                if (logoSwitch.isChecked()){
                    logoSwitch.setChecked(true);
                    logoAble = true;
                }
                else {
                    logoSwitch.setChecked(false);
                    logoAble = false;
                }

                cnt1 = 0;
                cnt2 = 0;
                imgBtnTake.setVisibility(View.VISIBLE);
                imgBtnTake.setClickable(true);
                settingBar.setVisibility(View.INVISIBLE);
                settingBar.setClickable(false);

                if (logoSwitch.isChecked()) {
                    File file3 = new File(getExternalFilesDir(folderName + "icon"), "/" + R.drawable.iconimg);
                    File file4 = new File(getExternalFilesDir(folderName + "iconsmall"), "/" + R.drawable.small);

                    File folder1 = new File(folderName + "icon");
                    if (!folder1.exists()) {
                        folder1.mkdirs();
                    }
                    if (file3.exists()) {
                        file3.delete();
                    }
                    if (file4.exists()) {
                        file4.delete();
                    }

                    byte[] bytes = byteSaveBig();
                    byte[] bytes1 = byteSaveSmall();
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file3);
                        output.write(bytes);
                        output = new FileOutputStream(file4);
                        output.write(bytes1);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (null != output) {
                            try {
                                output.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    Intent intent = new Intent(MainActivity.this, CustomGalleryActivity.class);
                    intent.putExtra("dateAble", dateAble);
                    startActivity(intent);

                }
            }
        });

        settingBar = findViewById(R.id.settingBar);

        if (textureView != null) {
            textureView.setSurfaceTextureListener(textureListener);
            OrientationEventListener mOrientationListener = new OrientationEventListener(
                    getApplicationContext()) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation > 330 || orientation <= 60) textureView.setRotation(0);
                    else if (orientation > 60 && orientation <= 150) textureView.setRotation(90);
                    else if (150 < orientation && orientation <= 240) textureView.setRotation(180);
                    else if (240 < orientation && orientation <= 330) textureView.setRotation(270);
                    orientationNum = orientation;
                }
            };
            if (mOrientationListener.canDetectOrientation()) {
                mOrientationListener.enable();
            }
        }

        imgBtnTake = findViewById(R.id.btnTake);

        if (imgBtnTake != null) {
            imgBtnTake.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timerText.setText("5");
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        int second = 6;
                        @Override
                        public void run() {
                            second--;
                            timerText.setText(String.valueOf(second));
                            if (second == 0) {
                                timerText.setText("");
                                timer.cancel();
                                sound.play(MediaActionSound.SHUTTER_CLICK);
                                try {
                                    takePicture(orientationNum);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    };
                    timer.schedule(timerTask, 0, 1000);
                }
            });
        }
        if (setting1 != null) {
            setting1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgBtnTake.setVisibility(View.INVISIBLE);
                    imgBtnTake.setClickable(false);
                    settingBar.setVisibility(View.VISIBLE);
                    settingBar.setClickable(true);
                }
            });
        }
        if (setting2 != null) {
            setting2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgBtnTake.setVisibility(View.INVISIBLE);
                    imgBtnTake.setClickable(false);
                    settingBar.setVisibility(View.VISIBLE);
                    settingBar.setClickable(true);
                }
            });
        }
        if (setting3 != null) {
            setting3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgBtnTake.setVisibility(View.INVISIBLE);
                    imgBtnTake.setClickable(false);
                    settingBar.setVisibility(View.VISIBLE);
                    settingBar.setClickable(true);
                }
            });
        }
        if (setting4 != null) {
            setting4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgBtnTake.setVisibility(View.INVISIBLE);
                    imgBtnTake.setClickable(false);
                    settingBar.setVisibility(View.VISIBLE);
                    settingBar.setClickable(true);
                }
            });
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }
        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();

        }
        @Override
        public void onError(@NonNull CameraDevice camera, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    protected void takePicture(int takeAngel) throws IOException {
        if (cameraDevice == null) {
            Log.e(TAG,"cameraDevice is null");
            return;
        }
        if (!isExternalStorageAvailableForRw() || isExternalStorageReadOnly()) {
            imgBtnTake.setEnabled(false);
        }
        if (isStoragePermissionGranted()) {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                Size[] jpegSizes = null;
                if (characteristics != null) {
                    jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                }
                int width = 640;
                int height = 480;
                if (jpegSizes != null && jpegSizes.length > 0) {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }
                final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG, 1); //

                List<Surface> outputSurfaces = new ArrayList<>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,Orientations.get(rotation));

                file = null;
                folder = new File(folderName);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";
                file = new File(getExternalFilesDir(folderName), "/" + imageFileName);
                File file2 = new File(getExternalFilesDir(folderName), "/" + R.drawable.under);

                if (file2.exists()) {
                    file2.delete();
                }

                if (!folder.exists()) {
                    folder.mkdirs();
                }
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = null;
                        try {
                            image = reader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            byte[] byteArray = null;
                            if (takeAngel > 330 || takeAngel <= 60) {
                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(270);
                                Bitmap rotatedImg = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                rotatedImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byteArray = stream.toByteArray();
                                bitmapImage.recycle();
                            }
                            else if (takeAngel > 60 && takeAngel <= 150) {
                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(180);
                                Bitmap rotatedImg = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                rotatedImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byteArray = stream.toByteArray();
                                bitmapImage.recycle();
                            }
                            else if (150 < takeAngel && takeAngel <= 240) {
                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                Bitmap rotatedImg = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                rotatedImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byteArray = stream.toByteArray();
                                bitmapImage.recycle();
                            }
                            else if (240 < takeAngel && takeAngel <= 330) {
                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(0);
                                Bitmap rotatedImg = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                rotatedImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byteArray = stream.toByteArray();
                                bitmapImage.recycle();
                            }

                            byte[] byteArray2 = null;
                            Resources res2 = getResources();
                            Drawable drawable2 = ResourcesCompat.getDrawable(res2, R.drawable.under, null);
                            Bitmap bmp2 = drawableToBitmap(drawable2);
                            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                            bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
                            byteArray2 = stream2.toByteArray();
                            bmp2.recycle();

                            save(byteArray,byteArray2);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }
                    private void save(byte[] bytes, byte[] bytes2) throws IOException {
                        OutputStream output = null;
                        OutputStream output2 = null;
                        try {
                            output = new FileOutputStream(file);
                            output.write(bytes);
                            output2 = new FileOutputStream(file2);
                            output2.write(bytes2);

                        } finally {
                            if (null != output) {
                                output.close();
                            }
                        }
                    }
                };
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        createCameraPreview();

                        BXLConfigLoader bxlConfigLoader = null;
                        try {
                            bxlConfigLoader = new BXLConfigLoader(MainActivity.this);
                            bxlConfigLoader.openFile();

                        }
                        catch (JposException e) {
                            e.printStackTrace();
                            bxlConfigLoader.newFile();

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            bxlConfigLoader.newFile();

                        }
                        try {
                            for (Object entry : bxlConfigLoader.getEntries()) {
                                JposEntry jposEntry = (JposEntry) entry;
                                bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
                            }
                            bxlConfigLoader.addEntry("BK3-3"
                            ,BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                            BXLConfigLoader.PRODUCT_NAME_BK3_3
                            ,BXLConfigLoader.DEVICE_BUS_USB
                            ,"74:F0:7D:E4:11:AF");
                            bxlConfigLoader.saveFile();

                            String timeStamp = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
                            POSPrinter posPrinter = new POSPrinter(MainActivity.this);
                            posPrinter.open("BK3-3");
                            posPrinter.claim(3000);
                            posPrinter.setDeviceEnabled(true);

                            ByteBuffer buffer = ByteBuffer.allocate(4);
                            buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
                            buffer.put((byte) 30);
                            buffer.put((byte) 0x00);
                            buffer.put((byte) 0x01);

                            posPrinter.printBitmap(buffer.getInt(0),file.getAbsolutePath(),576,
                                    POSPrinterConst.PTR_BM_CENTER);


                            if (logoCheck) {
                                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
                                if (logoPath.contains("small")) posPrinter.printBitmap(buffer.getInt(0),logoPath,300,
                                        POSPrinterConst.PTR_BM_RIGHT);
                                else posPrinter.printBitmap(buffer.getInt(0),logoPath,576,
                                        POSPrinterConst.PTR_BM_CENTER);
                            }

                            if (dateSwitch.isChecked()) {
                                String ESCAPE_CHARACTERS = new String(new byte[]{0x1b, 0x7c});
                                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
                                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "N" + ESCAPE_CHARACTERS + "rA" + ESCAPE_CHARACTERS + "bC" + ESCAPE_CHARACTERS + "2hC" + ESCAPE_CHARACTERS + "2vC" + timeStamp + "\n");


                            }

                            posPrinter.printBitmap(buffer.getInt(0),file2.getAbsolutePath(),576,
                                    POSPrinterConst.PTR_BM_CENTER);
                            posPrinter.printBitmap(buffer.getInt(0),file2.getAbsolutePath(),576,
                                    POSPrinterConst.PTR_BM_CENTER);
                            posPrinter.printBitmap(buffer.getInt(0),file2.getAbsolutePath(),576,
                                    POSPrinterConst.PTR_BM_CENTER);
                            posPrinter.printBitmap(buffer.getInt(0),file2.getAbsolutePath(),576,
                                    POSPrinterConst.PTR_BM_CENTER);
                            posPrinter.printBitmap(buffer.getInt(0),file2.getAbsolutePath(),576,
                                    POSPrinterConst.PTR_BM_CENTER);
                            posPrinter.printBitmap(buffer.getInt(0),file2.getAbsolutePath(),576,
                                    POSPrinterConst.PTR_BM_CENTER);

                            posPrinter.cutPaper(90);
                            posPrinter.close();

                            dateSwitch.setChecked(false);
                            logoSwitch.setChecked(false);
                        } catch (JposException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        try {
                            session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    }
                }, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    private boolean isExternalStorageAvailableForRw() {
        String extStorageState = Environment.getExternalStorageState();
        if (extStorageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private void openCamera() {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG,"is camera open");
        try {
            cameraId = manager.getCameraIdList()[1]; // camera 방향 바꾸기 0 : 후면 카메라, 1 : 전면 카메라
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null );
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this, "카메라 권한 필요", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        }
        else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG,"onPause");
        stopBackgroundThread();
        super.onPause();
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        final int width = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().height() : drawable.getIntrinsicHeight();

        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight() + 10);
        drawable.draw(canvas);

        return bitmap;
    }

    byte[] byteSaveBig() {
        byte[] byteArray1 = null;
        Resources res = getResources();
        Drawable drawable = ResourcesCompat.getDrawable(res, R.drawable.iconimg, null);
        Bitmap bmp = drawableToBitmap(drawable);
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream1);
        byteArray1 = stream1.toByteArray();
        bmp.recycle();

        return byteArray1;
    }
    byte[] byteSaveSmall() {
        byte[] byteArray1 = null;
        Resources res = getResources();
        Drawable drawable = ResourcesCompat.getDrawable(res, R.drawable.small, null);
        Bitmap bmp = drawableToBitmap(drawable);
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream1);
        byteArray1 = stream1.toByteArray();
        bmp.recycle();

        return byteArray1;
    }
}