package com.inventics.e_commerce;
;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.inventics.e_commerce.adapter.productAdapter;
import com.inventics.e_commerce.model.Product;
import android.Manifest;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView mrecyclerview;
    private ArrayList<Product> ldata;
    private productAdapter mAdapter;
    ProgressBar pd;

    DatabaseReference mref;
    FirebaseDatabase database;

    FloatingActionButton fab;

    GridLayoutManager gd;

    private String lastKey = "";
    private int pageSize = 10;
    
    BottomNavigationView bv;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        bv=findViewById(R.id.bottom_nav);

        bv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.action_menu){
                    HomeFragment fragment = new HomeFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, fragment, "");
                    fragmentTransaction.commit();
                    return true;

                }else if(menuItem.getItemId()==R.id.action_cat){
                    CategoryFragment fragment = new CategoryFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, fragment, "");
                    fragmentTransaction.commit();
                    return true;
                }else if(menuItem.getItemId()==R.id.action_cart){
                    CartFragment fragment = new CartFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, fragment, "");
                    fragmentTransaction.commit();
                    return true;
                }
                else if(menuItem.getItemId()==R.id.action_setting){
                    AccountFragment fragment = new AccountFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, fragment, "");
                    fragmentTransaction.commit();
                    return true;
                }else{

                }
                return false;
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted
            // You can perform your operations that require this permission
        }



        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);

        String activity = myPreferences.getString("activity", "");

        if(activity.equals("account")){
            AccountFragment fragment = new AccountFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment, "");
            fragmentTransaction.commit();

        }else if(activity.equals("category")){
            CategoryFragment fragment = new CategoryFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment, "");
            fragmentTransaction.commit();

        }else if(activity.equals("cart")){
            CartFragment fragment = new CartFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment, "");
            fragmentTransaction.commit();

        }else{
            HomeFragment fragment = new HomeFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment, "");
            fragmentTransaction.commit();

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, you can perform your operations that require this permission
            } else {
                // Permission is denied, handle accordingly (e.g., show a message or disable functionality)
            }
        }
    }

}
