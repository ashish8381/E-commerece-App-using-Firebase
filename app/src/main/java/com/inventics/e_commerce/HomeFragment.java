package com.inventics.e_commerce;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.inventics.e_commerce.adapter.productAdapter;
import com.inventics.e_commerce.model.Product;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View  mMainview;
    Context context;


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


    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;


    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        mMainview=  inflater.inflate(R.layout.fragment_home, container, false);
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        Toolbar toolbar = mMainview.findViewById(R.id.toolbar);
////        toolbar.setTitle("E-Commerce Using Firebase");
//
//        SpannableString spannableString = new SpannableString("E-Commerce Using Firebase");
//        TypefaceSpan typefaceSpan = new TypefaceSpan("satisfyregular");
//        spannableString.setSpan(typefaceSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        toolbar.setTitle(spannableString);
//        toolbar.setTitleTextColor(Color.BLACK);
//        activity.setSupportActionBar(toolbar);

        mrecyclerview = mMainview.findViewById(R.id.menu_recyclerview);
        gd = new GridLayoutManager(context,2);
        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(gd);

        fab=mMainview.findViewById(R.id.add_pro);

        pd=mMainview.findViewById(R.id.progress);
        pd.setVisibility(View.VISIBLE);

        ldata=new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        mref = database.getReference("Product");

        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(requireContext());

        String cat = myPreferences.getString("click_cat", "");
        String activity = myPreferences.getString("activity", "");

        if(activity.equals("home2")){
            getdata(cat);
        }else{
            getData();
        }






        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(),AddProActivity.class));
            }
        });






//        mrecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                int totalItemCount = gd.getItemCount();
//                int lastVisibleItem = gd.findLastVisibleItemPosition();
//                Log.e("pos","lastvisible= "+lastVisibleItem+" total= "+totalItemCount);
//                if (totalItemCount <= (lastVisibleItem + 5)) {
//                    loadNextPage();
//                }
//            }
//        });


        return mMainview;
    }

    private void getdata(String cat) {
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ldata.clear();
                for(DataSnapshot Snapshot:dataSnapshot.getChildren()) {


                    Product post = Snapshot.getValue(Product.class);
                    lastKey = Snapshot.getKey();
                    if (post != null) {
                        post.setKey(Snapshot.getKey());
                    }
                    if(post.getCategory().equals(cat)){
                        post.setKey(Snapshot.getKey());
                        ldata.add(post);
                    }


                    Log.d("abcd", "Value is: " + Snapshot.getKey());
                }
                mAdapter=new productAdapter(ldata,requireContext(),new CustomItemClickListener() {
                    @Override
                    public void onItemClick(Product product, int position) {

                        Toast.makeText(context,""+product.getId(),Toast.LENGTH_SHORT).show();

                    }
                });
                mrecyclerview.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
                pd.setVisibility(View.GONE);
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(requireContext());
                SharedPreferences.Editor myEditor = myPreferences.edit();
                myEditor.putString("activity", "");
                myEditor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("abcd", "Failed to read value.", error.toException());
            }
        });
    }

    private void getData() {
        // Read from the database

//        Query query = mref.orderByKey().limitToFirst(pageSize);

        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ldata.clear();
                for(DataSnapshot Snapshot:dataSnapshot.getChildren()) {


                    Product post = Snapshot.getValue(Product.class);
                    lastKey = Snapshot.getKey();


                    if (post != null) {
                        post.setKey(Snapshot.getKey());
                        // Add the product to your list or adapter
                    }

                    ldata.add(post);
                    Log.d("abcd", "Value is: " + Snapshot.getKey());
                }
                mAdapter=new productAdapter(ldata,requireContext(),new CustomItemClickListener() {
                    @Override
                    public void onItemClick(Product product, int position) {

                        Toast.makeText(context,""+product.getId(),Toast.LENGTH_SHORT).show();

                    }
                });
                mrecyclerview.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
                pd.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("abcd", "Failed to read value.", error.toException());
            }
        });
    }

    private void loadNextPage()
    {

        Log.d("pos", "last key= " + lastKey);
        Query query = mref.orderByKey().startAt(lastKey).limitToFirst(pageSize);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.getKey().equals(lastKey)) {
                        lastKey = dataSnapshot.getKey();
                        Product model = dataSnapshot.getValue(Product.class);
                        ldata.add(model);
                        Log.d("pos", "next page values: " + dataSnapshot.getKey());
                    }
                    mAdapter=new productAdapter(ldata,context,new CustomItemClickListener() {
                        @Override
                        public void onItemClick(Product product, int position) {

                            Toast.makeText(context,""+product.getId(),Toast.LENGTH_SHORT).show();

                        }
                    });
                    mrecyclerview.setAdapter(mAdapter);

                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

}