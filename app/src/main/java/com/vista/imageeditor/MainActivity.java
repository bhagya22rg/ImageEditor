package com.vista.imageeditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ImageView userpic;
    private static final int STORAGE_REQUEST = 200;

    String storagePermission[];

    TextView click,tv_width,tv_height,tv_size,tv_resu,tv_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        click = findViewById(R.id.click);
        tv_width = findViewById(R.id.tv_width);
        tv_height = findViewById(R.id.tv_height);
        tv_size = findViewById(R.id.tv_size);
        tv_resu = findViewById(R.id.tv_resu);
        tv_type = findViewById(R.id.tv_type);
        userpic = findViewById(R.id.set_profile_image);

        // allowing permissions of gallery
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        click.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        });
    }



    // checking storage permissions
    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    // Requesting  gallery permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }



    // Requesting gallery
    // permission if not given
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }

    // Here we will pick image from gallery or camera
    private void pickFromGallery() {
        CropImage.activity().start(MainActivity.this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                InputStream in = null;
                String type = null;
               DecimalFormat df = new DecimalFormat("0.00");

                try {
                    File file = new File(resultUri.getPath());
                    in = getContentResolver().openInputStream(resultUri);
                    ExifInterface exifInterface = new ExifInterface(in);
                    int imageWidth = Integer.parseInt(Objects.requireNonNull(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)));
                    int imageHeight = Integer.parseInt(Objects.requireNonNull(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)));
                    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(resultUri.toString());
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
                    tv_width.setText("Width :"+imageWidth);
                    tv_height.setText("Height :"+imageHeight);
                    tv_size.setText("Size :"+df.format(file.length() / 1024.0 / 1024.0) + "MB");
                    tv_resu.setText("Resolution :"+MessageFormat.format("{0} x {1}", imageWidth, imageHeight));
                    tv_type.setText("MIME Type :" +type);

                   // Log.e("exifinfo",""+type +","+imageHeight+","+ MessageFormat.format("{0} x {1}", imageWidth, imageHeight)+","+(df.format(file.length() / 1024.0 / 1024.0)) + "MB");

                } catch (IOException  e) {
                    // Handle any errors
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ignored) {}
                    }
                }
                Picasso.with(this).load(resultUri).into(userpic);
            }
        }
    }

}