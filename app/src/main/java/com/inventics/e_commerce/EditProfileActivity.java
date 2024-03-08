package com.inventics.e_commerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

       TextInputEditText mname,memail,maddress;
       CircleImageView mimage;
       Button mupdate;
    int PICK_IMAGE_REQUEST=1;
    byte[] imageData=null;

    String imageurl;
    ImageView mback;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        mname=findViewById(R.id.edit_name);
        memail=findViewById(R.id.edit_email);
        maddress=findViewById(R.id.edit_address);
        mimage=findViewById(R.id.edit_img);
        mupdate=findViewById(R.id.update_profile_btn);
        mback=findViewById(R.id.edit_profile_back);


        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);

        String name = myPreferences.getString("user_name", "");
        String email = myPreferences.getString("user_email", "");
        imageurl= myPreferences.getString("user_img", "");
        String address= myPreferences.getString("user_address", "");
        mname.setText(name);
        memail.setText(email);
        maddress.setText(address);
        Picasso.get().load(imageurl).into(mimage);


        mimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);

            }
        });

        mupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                if(imageData!=null){
                    uploadimg(imageData);
                }else{
                    String name=mname.getText().toString();
                    String email=memail.getText().toString();
                    String address=maddress.getText().toString();

                    if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                        progressDialog.dismiss();
                    }else{
                        updatedata(name,email,address,imageurl);

                    }

                }
            }
        });

        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(EditProfileActivity.this);
                SharedPreferences.Editor myEditor = myPreferences.edit();
                myEditor.putString("activity", "account");
                myEditor.apply();
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                mimage.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imageData = baos.toByteArray();
                // Use the byte array as needed
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                            progressDialog.dismiss();
                        }else{
                            updatedata(name,email,address,uri.toString());

                        }

                    }
                });
            }
        });


    }

    private void updatedata(String name, String email, String address, String imageBytes) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String phoneNumber = users.getPhoneNumber();
        // Get the user ID of the currently authenticated user
        String userId = firebaseAuth.getCurrentUser().getUid();

        Map<String, Object> userdata = new HashMap<>();
        userdata.put("name",name );
        userdata.put("email",email );
        userdata.put("address",address );
        userdata.put("image",imageBytes );

        // Upload user data to Firebase Realtime Database
        databaseReference.child("users").child(userId).updateChildren(userdata)
                .addOnSuccessListener(aVoid ->{
                    storedata(name,email,phoneNumber,imageBytes,address);
                    progressDialog.dismiss();
                    Log.e("FirebaseUtils", "User data uploaded successfully");
                    SharedPreferences myPreferences
                            = PreferenceManager.getDefaultSharedPreferences(EditProfileActivity.this);
                    SharedPreferences.Editor myEditor = myPreferences.edit();
                    myEditor.putString("activity", "account");
                    myEditor.apply();
                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                    startActivity(intent);


                })
                .addOnFailureListener(e ->{
                    progressDialog.dismiss();
                    Log.e("FirebaseUtils", "Error uploading user data: " + e.getMessage());
                });
    }


    private void storedata(String name, String email, String phone, String img,String address) {
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(EditProfileActivity.this);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("user_name", name);
        myEditor.putString("user_email", email);
        myEditor.putString("user_phone", phone);
        myEditor.putString("user_img", img);
        myEditor.putString("user_address", address);
        myEditor.apply();
    }


}