package com.inventics.e_commerce;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.BuildConfig;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddProActivity extends AppCompatActivity {

    CircleImageView mimage;

    Uri muploadimageuri;

    private static final int REQUEST_IMAGE_CAPTURE = 2;

    ImageView mback;

    private static String picturePath;

    TextInputEditText mpro_name,mpro_cat,mpro_price,mpro_desc;

    Button mupload;
    private static final String IMAGE_FILE_NAME = "captured_image.jpg";
    ProgressDialog progressDialog;
    private ActivityResultLauncher<Intent> captureImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pro);


        mback=findViewById(R.id.add_product_back);

       mback.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               SharedPreferences myPreferences
                       = PreferenceManager.getDefaultSharedPreferences(AddProActivity.this);
               SharedPreferences.Editor myEditor = myPreferences.edit();
               myEditor.putString("activity", "Home");
               myEditor.apply();
               Intent intent = new Intent(AddProActivity.this, MainActivity.class);
               startActivity(intent);
           }
       });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);


        mpro_name=findViewById(R.id.add_title);
        mpro_cat=findViewById(R.id.add_cat);
        mpro_price=findViewById(R.id.add_price);
        mpro_desc=findViewById(R.id.add_desc);
        mimage=findViewById(R.id.add_img);

        mupload=findViewById(R.id.add_pro_btn);


        mupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                upload_img();
            }
        });



        mimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCaptureButton(v);


            }
        });

    }

    private void upload_img() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), muploadimageuri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Compress the bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // 50 is the compression quality
        byte[] imageData = baos.toByteArray();

        StorageReference riversRef = storageRef.child("images/"+ UUID.randomUUID().toString());
        UploadTask  uploadTask = riversRef.putBytes(imageData);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uploaddata(uri.toString());

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
    }

    private void Uploaddata(String string) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mref = database.getReference("Product");

        String productname=mpro_name.getText().toString();
        String productcat=mpro_cat.getText().toString();
        float productprice= Float.parseFloat(mpro_price.getText().toString());
        String productdesc=mpro_desc.getText().toString();



        int id= (int) System.currentTimeMillis();
        Map<String, Object> produtsdata = new HashMap<>();
        produtsdata.put("title",productname );
        produtsdata.put("description",productdesc );
        produtsdata.put("price", (productprice));
        produtsdata.put("category",productcat );
        produtsdata.put("id",id);
        produtsdata.put("image",string );
        HashMap<String, Object> rating = new HashMap<>();
        rating.put("rate", 4.5);
        rating.put("count", 50);
        produtsdata.put("rating", rating);

        DatabaseReference db=mref.push();

        db.setValue(produtsdata).addOnSuccessListener(new OnSuccessListener<Void>() {

            @Override
            public void onSuccess(Void aVoid) {

                progressDialog.dismiss();
                startActivity(new Intent(AddProActivity.this,MainActivity.class));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.toString());
                progressDialog.dismiss();
            }
        });





    }

    private void onClickCaptureButton(View view) {
        Intent takePictureIntent_ = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent_.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile_ = null;
            try {
                photoFile_ = createImageFile();
            } catch (IOException ex) {
            }
            if(photoFile_!=null){
                picturePath=photoFile_.getAbsolutePath();
            }
            // Continue only if the File was successfully created
            if (photoFile_ != null) {
                Uri photoURI_ = FileProvider.getUriForFile(this,
                        "com.inventics.e_commerce.fileprovider", photoFile_);
                takePictureIntent_.putExtra(MediaStore.EXTRA_OUTPUT, photoURI_);
                startActivityForResult(takePictureIntent_, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = System.currentTimeMillis()+"";
        String imageFileName_ = "IMG_" + timeStamp + "_";
        File image_ = File.createTempFile(
                imageFileName_,  /* prefix */
                ".jpg",         /* suffix */
                getFilesDir()      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        picturePath= image_.getAbsolutePath();
        return image_;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK
        ){

            try {
                File file_ = new File(picturePath);
//                save_originalimg(file_);
                Toast.makeText(this, "Image Size="+ getfilesize(file_.length()), Toast.LENGTH_SHORT).show();
                save_originalimg(picturePath);
                Uri uri_ = FileProvider.getUriForFile(this,
                        "com.inventics.e_commerce.fileprovider", file_);
                mimage.setImageURI(uri_);
                muploadimageuri=uri_;
            } catch (/*IO*/Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void save_originalimg(String filepath) {

        File file_= new File(filepath);

        try {

            Uri uri =Uri.fromFile(file_);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                File jpgFile = createJPGFile();
                FileOutputStream outputStream = new FileOutputStream(jpgFile);

                // Convert HEIC to JPG and save the bytes to the output stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();

                inputStream.close();
            } else {
                // Handle the case where the input stream is null (file not found)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private File createJPGFile() {
        File downloadsDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "com.inventics.e_commerce");
        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs();
        }
//        getFilesDir()
        return new File(downloadsDirectory, "IMG_"+System.currentTimeMillis()+".jpg");
    }


    public String getfilesize(long fileSize){
        String fileSizeStr = "";
        if (fileSize < 1024) {
            fileSizeStr = fileSize + " bytes";
        } else if (fileSize < 1024 * 1024) {
            fileSizeStr = String.format("%.2f KB", (float) fileSize / 1024);
        } else {
            fileSizeStr = String.format("%.2f MB", (float) fileSize / (1024 * 1024));
        }
        return fileSizeStr;
    }


}