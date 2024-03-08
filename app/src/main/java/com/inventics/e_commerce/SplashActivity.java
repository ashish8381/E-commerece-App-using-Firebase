package com.inventics.e_commerce;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    FirebaseUser currentUser ;
    FirebaseAuth mAuth;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("activity", "");
        myEditor.apply();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (currentUser == null) {
                    updateUI();
                }else {
                    userId = currentUser.getUid();
                    checkdata();
                }


            }
        }, 3000);
    }

    private void checkdata() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {

                    String name=dataSnapshot.child(userId).child("name").getValue().toString();
                    String email=dataSnapshot.child(userId).child("email").getValue().toString();
                    String phone=dataSnapshot.child(userId).child("phoneNumber").getValue().toString();
                    String img=dataSnapshot.child(userId).child("image").getValue().toString();
                    String address=dataSnapshot.child(userId).child("address").getValue().toString();

                    storedata(name,email,phone,img,address);


                    Intent intet = new Intent(SplashActivity.this, MainActivity.class);
                    intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intet);
                    finish();

                } else {
                    Intent intet = new Intent(SplashActivity.this, ProfileActivity.class);
                    intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intet);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    private void storedata(String name, String email, String phone, String img,String address) {
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(SplashActivity  .this);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("user_name", name);
        myEditor.putString("user_email", email);
        myEditor.putString("user_phone", phone);
        myEditor.putString("user_img", img);
        myEditor.putString("user_address", address);
        myEditor.apply();
    }

    private void updateUI()
    {
        Intent intet = new Intent(SplashActivity.this, PhoneAuthActivity.class);
        intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intet);
        finish();

    }
}