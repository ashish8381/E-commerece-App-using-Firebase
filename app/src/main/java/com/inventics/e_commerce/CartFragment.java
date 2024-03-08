package com.inventics.e_commerce;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inventics.e_commerce.adapter.cartAdapter;
import com.inventics.e_commerce.adapter.productAdapter;
import com.inventics.e_commerce.model.Product;
import com.inventics.e_commerce.model.cartModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mMainView;


    RecyclerView mrecyclerview;
    private ArrayList<Product> ldata;
    private cartAdapter mAdapter;
    ProgressBar pd;

    String user_id;

    DatabaseReference mref,mref2;
    FirebaseDatabase database;

    public CartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
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
        mMainView= inflater.inflate(R.layout.fragment_cart, container, false);

        mrecyclerview = mMainView.findViewById(R.id.cart_recyclerview);
        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));

        pd=mMainView.findViewById(R.id.cart_progress);
        pd.setVisibility(View.VISIBLE);

        ldata=new ArrayList<>();
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        mref = database.getReference("cart").child(user_id);

        fetchdata();


        return mMainView;
    }

    private void fetchdata() {
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot Snapshot:dataSnapshot.getChildren()) {


                    cartModel post = Snapshot.getValue(cartModel.class);
                    getprodetails(post.getPro_key(),post.getQty());

                }
                pd.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("abcd", "Failed to read value...", error.toException());
            }
        });
    }

    private void getprodetails(String proKey, int qty) {
        mref2 = database.getReference("Product");

        mref2.child(proKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot Snapshot) {



                    Product post = Snapshot.getValue(Product.class);
                    ldata.add(post);

                    post.setQty(qty);

                    post.setKey(Snapshot.getKey());

                    Log.d("xyz", "Value is: " + Snapshot.getKey());

                    mAdapter=new cartAdapter(ldata,requireContext());
                    mrecyclerview.setAdapter(mAdapter);

                    mAdapter.notifyDataSetChanged();
                    pd.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

    }

}