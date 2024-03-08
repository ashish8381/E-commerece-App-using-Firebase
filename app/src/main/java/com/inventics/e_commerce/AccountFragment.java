package com.inventics.e_commerce;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inventics.e_commerce.adapter.ViewPagerAdapter;
import com.inventics.e_commerce.model.Product;
import com.inventics.e_commerce.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View mMainView;

    private RelativeLayout mlogout;
    private CircleImageView mprofile;
    private TextView mname,mphoneno,memail;

    ImageView meditprofile;

    ImageView mback;

    private Context context;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_account, container, false);

        mlogout=mMainView.findViewById(R.id.logout);

        meditprofile=mMainView.findViewById(R.id.edit_profile);

        mname=mMainView.findViewById(R.id.account_holder);
        mphoneno=mMainView.findViewById(R.id.account_holder_no);
        memail=mMainView.findViewById(R.id.account_holder_email);
        mprofile=mMainView.findViewById(R.id.account_image);
        mback=mMainView.findViewById(R.id.account_back);

        meditprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(),EditProfileActivity.class));
            }
        });

        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(requireContext());
                SharedPreferences.Editor myEditor = myPreferences.edit();
                myEditor.putString("activity", "home");
                myEditor.apply();
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        getdata();
        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireContext(),PhoneAuthActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        return mMainView;
    }

    private void getdata() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mref = database.getReference("users");

        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String phoneNumber = users.getPhoneNumber();
        // Get the user ID of the currently authenticated user
        String userId = users.getUid();

        mref.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                mname.setText(user.getName());
                memail.setText(user.getEmail());
                mphoneno.setText(user.getPhoneNumber());

//                savepredata(user.getImage()+" ");

                if (user.getImage().equals(getpredata())) {
                    SharedPreferences myPreferences
                            = PreferenceManager.getDefaultSharedPreferences(requireContext());

                    String base64= myPreferences.getString("user_profile_base64", "");
                    Bitmap bitmap = base64ToBitmap(base64);
                    if (bitmap != null) {
                        // Use the bitmap (e.g., set it to an ImageView)
                        mprofile.setImageBitmap(bitmap);
                    } else {
                        // Handle error
                    }

                }
                else {
                    //save the data
                    //save image in assets
                    Toast.makeText(requireContext(), "else", Toast.LENGTH_SHORT).show();
                    savepredata(user.getImage());
                    downloadimage(user.getImage());

                }
                storedata(user.getName(), user.getEmail(), user.getPhoneNumber(), user.getImage(), user.getAddress());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void downloadimage(String image) {
        new DownloadImageTask().execute(image);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Use the bitmap (e.g., set it to an ImageView)
            if (bitmap != null) {

                mprofile.setImageBitmap(bitmap);
                String fileName = "profile.png";
                saveBitmapToInternalStorage(bitmap);

            } else {
                // Handle error
                mprofile.setImageResource(R.drawable.avatarr);
            }
        }
    }


    private void saveBitmapToInternalStorage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base4img= Base64.encodeToString(byteArray, Base64.DEFAULT);

        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("user_profile_base64", base4img);
        myEditor.apply();
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }





    private void savepredata(String image) {
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("user_profile", image);
        myEditor.apply();
    }

    private String getpredata(){
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(requireContext());

        return myPreferences.getString("user_profile", "");

    }

    private void storedata(String name, String email, String phone, String img,String address) {
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString("user_name", name);
        myEditor.putString("user_email", email);
        myEditor.putString("user_phone", phone);
        myEditor.putString("user_img", img);
        myEditor.putString("user_address", address);
        myEditor.apply();
    }
}