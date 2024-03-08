package com.inventics.e_commerce;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inventics.e_commerce.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    TextInputEditText mname,memail,maddress;
    int PICK_IMAGE_REQUEST=1;
    CircleImageView mimg;
    Button msubmit;

    ProgressBar mprogress;

    byte[] imageData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        updateui();

        mname=findViewById(R.id.profile_name);
        memail=findViewById(R.id.profile_email);
        maddress=findViewById(R.id.profile_address);

        mprogress=findViewById(R.id.profile_loading);

        msubmit=findViewById(R.id.create_profile);

        mimg=findViewById(R.id.profile_image);

        mimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
            }
        });


        msubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadimg(imageData);
            }
        });


    }

    private void updateui() {
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Intent intet = new Intent(ProfileActivity.this, PhoneAuthActivity.class);
            intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intet);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                mimg.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imageData = baos.toByteArray();
                // Use the byte array as needed
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatedata(String name, String email, String address, String imageBytes) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String phoneNumber = users.getPhoneNumber();
        // Get the user ID of the currently authenticated user
        String userId = firebaseAuth.getCurrentUser().getUid();
        User user = new User(name, address, email, imageBytes,phoneNumber);

        // Upload user data to Firebase Realtime Database
        databaseReference.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid ->{
                    storedata(name,email,phoneNumber,imageBytes,address);
                    mprogress.setVisibility(View.GONE);
                    Log.d("FirebaseUtils", "User data uploaded successfully");
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);


                })
                .addOnFailureListener(e ->{
                    mprogress.setVisibility(View.GONE);
                    Log.e("FirebaseUtils", "Error uploading user data: " + e.getMessage());
                });


    }

    private void uploadimg(byte[] imageBytes) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child("images/"+ UUID.randomUUID().toString());
        UploadTask uploadTask = riversRef.putBytes(imageBytes);



        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String name=mname.getText().toString();
                        String email=memail.getText().toString();
                        String address=maddress.getText().toString();

                        if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                            Log.e("errorlog","fields are empty");
                        }else{
                            mprogress.setVisibility(View.VISIBLE);
                            updatedata(name,email,address,uri.toString());

                        }

                    }
                });
            }
        });


    }

    private void storedata(String name, String email, String phone, String img,String address) {
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("user_name", name);
        myEditor.putString("user_email", email);
        myEditor.putString("user_phone", phone);
        myEditor.putString("user_img", img);
        myEditor.putString("user_address", address);
        myEditor.apply();
    }
}