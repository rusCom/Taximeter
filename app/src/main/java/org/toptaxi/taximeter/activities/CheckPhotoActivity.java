package org.toptaxi.taximeter.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.toptaxi.taximeter.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CheckPhotoActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 835478;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 64654;
    PreviewView previewView;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Button btnTakePicture;
    ImageCapture imageCapture;
    TextView tvCheckPhotoCaption;
    TextView tvCheckPhotoDescription;
    String captureType = "";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_photo);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tvCheckPhotoCaption = findViewById(R.id.tvCheckPhotoCaption);
        tvCheckPhotoDescription = findViewById(R.id.tvCheckPhotoDescription);



        Bundle extras = getIntent().getExtras();
        if (extras != null){
            captureType = extras.getString("capture_type", "");
            if (captureType.equals("self")){
                tvCheckPhotoCaption.setText("Необходимо сделать фото себя (селфи)");
            }
            else {
                tvCheckPhotoCaption.setText(Objects.requireNonNull(extras.getString("capture_title")).replace(".", "\n"));
                tvCheckPhotoDescription.setText("Убедитесь, что документ будет полностью сфотографирован, края документа не будут обрезаны, важные данные не закрыты посторонними предметами, а также, что все данные четко видны и читаемы.");
            }

        }



        // preview = findViewById(R.id.preview);
        previewView = findViewById(R.id.previewView);
        btnTakePicture = findViewById(R.id.btnTakePicture);

        btnTakePicture.setOnClickListener(view -> capturePhoto());



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
        else initializeCamera();



    }

    private void initializeCamera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }, getExecutor());
    }

    Executor getExecutor(){
        return ContextCompat.getMainExecutor(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            initializeCamera();
        }
    }

    private void startCameraX(ProcessCameraProvider cameraProvider){
        cameraProvider.unbindAll();
        ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                .setResolutionStrategy(new ResolutionStrategy(new Size(1280, 720),ResolutionStrategy.FALLBACK_RULE_NONE))
                .build();
        int cameraSelectorFacing = CameraSelector.LENS_FACING_BACK;
        if (captureType.equals("self")){
            cameraSelectorFacing = CameraSelector.LENS_FACING_FRONT;
        }
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraSelectorFacing)
                .build();

        Preview preview = new Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setResolutionSelector(resolutionSelector)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void capturePhoto(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        File root = android.os.Environment.getExternalStorageDirectory();
        Date date = new Date();
        String timeStamp = String.valueOf(date.getTime());
        String photoFilePath = root.getAbsolutePath() + "/DCIM/aTaxi/" + timeStamp + ".jpg";
        File photoFile = new File(photoFilePath);

        try {
            Files.createDirectories(Paths.get(photoFile.getAbsolutePath()));
        } catch (IOException exception) {
            Toast.makeText(CheckPhotoActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Intent data = new Intent();
                        data.putExtra("filename", photoFilePath);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CheckPhotoActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
        );


    }
}