package com.inventics.e_commerce;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {
    String mVerificationId="";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth auth;
    TextInputEditText mphone,motp;
    LinearLayout mphonelayout,mphonelayout2;
    Button msendotpbtn,msubmitotp;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 60000; // 60 seconds
    private boolean timerRunning = false;

    TextView mcountdown,mresendotp;
    String number;

    ProgressBar mprogress;
    LinearLayout mmaintimer,mnotgetotp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        auth = FirebaseAuth.getInstance();

        mphone=findViewById(R.id.signup_phone);

        msendotpbtn=findViewById(R.id.signup_btn);

        mcountdown=findViewById(R.id.otp_time);

        mresendotp=findViewById(R.id.resend_otp);

        mmaintimer=findViewById(R.id.main_timer_layout);
        mnotgetotp=findViewById(R.id.didnotget_otp);


        mprogress=findViewById(R.id.verification_loading);

        mphonelayout=findViewById(R.id.LinearLayout_Phone);
        msubmitotp=findViewById(R.id.signup_confirm);
        motp=findViewById(R.id.otp_field);

        mphonelayout2=findViewById(R.id.LinearLayout_PinView);

        msendotpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprogress.setVisibility(View.VISIBLE);
                 number=mphone.getText().toString();
                sendotp(number);
            }
        });

        mresendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmaintimer.setVisibility(View.VISIBLE);
                mnotgetotp.setVisibility(View.GONE);
                timeLeftInMillis = 60000;
                sendotp(number);
            }
        });

        msubmitotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mprogress.setVisibility(View.VISIBLE);
                String otp=motp.getText().toString();
                verifyPhoneNumberWithCode(mVerificationId,otp);
            }
        });
    }

    private void sendotp(String phonenumber){

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phonenumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                        Log.d("otp", "onCodeSent:" + verificationId);

                        // Save verification ID and resending token so we can use them later
                        mVerificationId = verificationId;
                        mResendToken = forceResendingToken;
                        mprogress.setVisibility(View.GONE);
                        mphonelayout.setVisibility(View.GONE);
                        mphonelayout2.setVisibility(View.VISIBLE);
                        mmaintimer.setVisibility(View.VISIBLE);
                        startTimer();

                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        mphonelayout2.setVisibility(View.GONE);
                        mphonelayout.setVisibility(View.VISIBLE);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                mcountdown.setText(timeLeftInMillis/1000+"");
                // Update UI with remaining time if needed
            }

            @Override
            public void onFinish() {
                // Timer finished, enable "Resend OTP" option
                timerRunning = false;
                mmaintimer.setVisibility(View.GONE);
                mnotgetotp.setVisibility(View.VISIBLE);
                // Enable resend button or show resend option
            }
        }.start();

        timerRunning = true;
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signInWithCredential:success", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            mprogress.setVisibility(View.GONE);
                            checkdata();
//                            updateUI();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("signinwithcredential", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void checkdata() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("FirebasePhoneAuth").child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (dataSnapshot.hasChild(userId)) {
                    Intent intet = new Intent(PhoneAuthActivity.this, MainActivity.class);
                    intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intet);
                    finish();

                } else {
                    Intent intet = new Intent(PhoneAuthActivity.this, ProfileActivity.class);
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


    private void updateUI() {
        Intent intet = new Intent(PhoneAuthActivity.this, ProfileActivity.class);
        intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intet);
        finish();

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

}