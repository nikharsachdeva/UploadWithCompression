package com.nikharsachdeva.uploadcompression;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    ImageView upload_inward_docs;
    int GALLERY = 1, CAMERA = 2;
    Uri photoURI;
    Bitmap rotatedBitmap;
    String mCurrentPhotoPath;
    public static APIinterface apiInterface;
    public static final String BASE_SERVER_URL = "https://laundrybuoy.com/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        upload_inward_docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAndRequestPermissions(MainActivity.this)) {
                    showPictureDialog();
                }

            }
        });
    }

    private void init() {

        upload_inward_docs = findViewById(R.id.upload_inward_docs);

    }

    ////////////////////////////UPLOAD////////////////////////////////////////
    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();

        int buffSize = 1024;
        byte[] buff = new byte[buffSize];

        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }

        return byteBuff.toByteArray();
    }
    ////////////////////////////UPLOAD////////////////////////////////////////


    //////////////////////////PERMISSION//////////////////////////////////////////

    public static boolean checkAndRequestPermissions(final Activity context) {
        int externalStoragePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (externalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "Requires Access to Camera.", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "Requires Access to Your Storage.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showPictureDialog();
                }
                break;
        }


    }

    //////////////////////////PERMISSION//////////////////////////////////////////

    //////////////////////////DIALOG//////////////////////////////////////////

    private void showPictureDialog() {

        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {"Select photo from gallery", "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                dispatchTakePictureIntent();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    //////////////////////////DIALOG//////////////////////////////////////////

    //////////////////////////OPTIONS//////////////////////////////////////////


    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                photoURI = FileProvider.getUriForFile(this,
                        "com.nikharsachdeva.uploadcompression.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA);
            }
        }
    }

    //////////////////////////OPTIONS//////////////////////////////////////////

    //////////////////////////IMAGES FUNCTION//////////////////////////////////////////


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("currentpath", mCurrentPhotoPath);
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            Log.d("resultcodecheck", String.valueOf(requestCode));
            switch (requestCode) {

                case 2: {
                    if (resultCode == RESULT_OK) {
                        rotateBitmapFun();

                        Uri rotatedUri = getImageUri(MainActivity.this, rotatedBitmap);

                        if (rotatedUri == null) {
                            Toast.makeText(this, "empty uri", Toast.LENGTH_SHORT).show();

                        } else {

                            try {

                                InputStream is = getContentResolver().openInputStream(rotatedUri);

                                MainActivity.uploadImage("1595671635", MainActivity.this, getBytes(is));

                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                            }

                        }


                    }
                    break;
                }

                case 1: {
                    if (intent.getData() != null) {
                        Uri contentURI = intent.getData();
                        try {

                            InputStream is = getContentResolver().openInputStream(intent.getData());

                            //THIS IS MY CODE, FOR UPLOADING IN BACKEND,FOR ONLY DEMONSTRATION PURPOSE
                            MainActivity.uploadImage("1595671635", MainActivity.this, getBytes(is));

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    //////////////////////////IMAGES FUNCTION//////////////////////////////////////////

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /////////////////ROTATE FUNCTION////////////////////


    private void rotateBitmapFun() throws IOException {

        //Toast.makeText(this, "Please Wait, Compressing.", Toast.LENGTH_SHORT).show();


        File file = new File(mCurrentPhotoPath);
        Bitmap bitmap = MediaStore.Images.Media
                .getBitmap(MainActivity.this.getContentResolver(), Uri.fromFile(file));

        ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;

        }
        rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 768, 1024, true);

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /////////////////ROTATE FUNCTION////////////////////

    /////////////////UPLOAD IMAGE///////////////////////

    public static void uploadImage(String code, final Context context, byte[] imageBytes) {

        apiInterface = Service.getClient().create(APIinterface.class);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

        RequestBody code1 = RequestBody.create(MediaType.parse("text/plain"),
                code);

        MultipartBody.Part body = MultipartBody.Part.createFormData("img", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + ".jpg", requestFile);

        Call<RatingModel> call = apiInterface.uploadProPicMultipart(body, code1);

        call.enqueue(new Callback<RatingModel>() {
            @Override
            public void onResponse(Call<RatingModel> call, retrofit2.Response<RatingModel> response) {


                if (response.body().getResponse().equals("true")) {
                    Toast.makeText(context, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "Failed!", Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onFailure(Call<RatingModel> call, Throwable t) {


                if (t instanceof IOException) {
                    Toast.makeText(context, "Please check your internet!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Unhandled Response Encountered!", Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }

            }
        });
    }

    ////////////////UPLOAD IMAGE ///////////////////////
}