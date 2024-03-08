package com.inventics.e_commerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inventics.e_commerce.adapter.ViewPagerAdapter;
import com.inventics.e_commerce.model.Product;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProductdetailActivity extends AppCompatActivity {

    TextView mtitle, mdesc, mprice, mcat, mreview;
    ViewPager mimage;
    private String[] imageUrls;
    RatingBar mrating;

    String user_id;
    String p_id;
    ProgressBar pd;

    int qty_db;

    boolean isCheck = true;

    String key="";

    String descval="";

    LinearLayout mainQtyLayout;
    ImageView hideLayout,unhideLayout;

    RelativeLayout manageLayout;

    ImageView mdlt, medit,mback;

    ImageView mminus,mplus,maddtocart;
    TextView mqty;

    int qtyy=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productdetail);

//        Toolbar toolbar = findViewById(R.id.detail_toolbar);
//        toolbar.setTitle("Product Details");
//        toolbar.setTitleTextColor(Color.WHITE);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back arrow
//
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed(); // Handle the back arrow click
//            }
//        });

        mtitle = findViewById(R.id.detail_productname);
        mdesc = findViewById(R.id.detail_desc);
        mprice = findViewById(R.id.detail_price);
        mcat = findViewById(R.id.detail_category);
        mreview = findViewById(R.id.detail_review);
        pd = findViewById(R.id.detail_progress);
        pd.setVisibility(View.VISIBLE);
        mdlt = findViewById(R.id.dlt_pro_btn);

        mimage = findViewById(R.id.view_pager);

        mrating = findViewById(R.id.detail_rating);

        medit = findViewById(R.id.edit_pro_btn);

         mainQtyLayout = findViewById(R.id.main_qty_layout);
         hideLayout = findViewById(R.id.hide_layout);
         unhideLayout = findViewById(R.id.unhide_layout);
         manageLayout = findViewById(R.id.manage_layout);

        mqty=findViewById(R.id.home_menu_qty);
        mminus=findViewById(R.id.home_menu_minus_qty);
        mplus=findViewById(R.id.home_menu_plus_qty);

        maddtocart=findViewById(R.id.add_to_btn);
        mback=findViewById(R.id.product_details_back);

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        imageUrls = new String[3];
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(ProductdetailActivity.this);

        p_id = myPreferences.getString("p_id", "");
        getProducts();

        medit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProductdetailActivity.this, EditProActivity.class));
            }
        });

        mdlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder altBx = new AlertDialog.Builder(ProductdetailActivity.this);
                altBx.setTitle("Delete Product");
                altBx.setMessage("Are you sure? You want to delete this Product");
                altBx.setIcon(R.mipmap.ic_launcher);

                altBx.setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dlt_pro();
                    }
                });
                altBx.setNeutralButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ProductdetailActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }

                });
                altBx.show();
            }
        });

        mdesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCheck) {
                    mdesc.setMaxLines(20);
                    mdesc.setText(descval);
                    isCheck = false;
                } else {
                    mdesc.setMaxLines(2);
                    isCheck = true;
                  add_read_more(descval);
                }
            }
        });

        hideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hidelayout();


            }
        });

        unhideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                unhidelayout();


            }
        });

        mplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(qtyy>=0){
                    qtyy+=1;
                    mqty.setText(qtyy+"");
                }
            }
        });

        mminus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(qtyy>0){
                    qtyy-=1;
                    mqty.setText(qtyy+"");
                }
            }
        });

        maddtocart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setVisibility(View.VISIBLE);
                addtocart();
            }
        });

        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(ProductdetailActivity.this);
                SharedPreferences.Editor myEditor = myPreferences.edit();
                myEditor.putString("activity", "Home");
                myEditor.apply();
                Intent intent = new Intent(ProductdetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    private void hidelayout() {
        mainQtyLayout.animate()
                .translationX(mainQtyLayout.getWidth())
                .alpha(0.0f)
                .setDuration(250)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) manageLayout.getLayoutParams();
                        RelativeLayout.LayoutParams newLayoutParams = new RelativeLayout.LayoutParams(layoutParams);
                        newLayoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());


                        mainQtyLayout.setVisibility(View.GONE);
                        unhideLayout.setVisibility(View.VISIBLE);
                        manageLayout.setLayoutParams(newLayoutParams);
                    }
                });

    }

    private void unhidelayout() {
        mainQtyLayout.setVisibility(View.VISIBLE);
        mainQtyLayout.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(250)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) manageLayout.getLayoutParams();
                        RelativeLayout.LayoutParams newLayoutParams = new RelativeLayout.LayoutParams(layoutParams);
                        newLayoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;


                        mainQtyLayout.setVisibility(View.VISIBLE);
                        unhideLayout.setVisibility(View.GONE);
                        manageLayout.setLayoutParams(newLayoutParams);
                    }
                });
    }

    private void addtocart() {

        if(qtyy==0) {
            Toast.makeText(this, "Please.. Mention Product Quantity", Toast.LENGTH_SHORT).show();
        }else{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference mref = database.getReference("cart");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp  = dateFormat.format(new Date());


            DatabaseReference db=mref.child(user_id);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(key)){
                        qty_db = Integer.parseInt(snapshot.child(key).child("qty").getValue().toString());
                        Map<String, Object> cartdata = new HashMap<>();
                        cartdata.put("pro_key",key );
                        cartdata.put("qty",qtyy+qty_db );
                        cartdata.put("timestamp", timestamp);
                        db.child(key).setValue(cartdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                qtyy=0;
                                hidelayout();
                                Toast.makeText(ProductdetailActivity.this, "Product Added to Cart.", Toast.LENGTH_SHORT).show();
                                pd.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProductdetailActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                pd.setVisibility(View.GONE);
                            }
                        });

                    }
                    else{
                        Map<String, Object> cartdata = new HashMap<>();
                        cartdata.put("pro_key",key );
                        cartdata.put("qty",qtyy );
                        cartdata.put("timestamp", timestamp);
                        db.child(key).setValue(cartdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                qtyy=0;
                                hidelayout();
                                Toast.makeText(ProductdetailActivity.this, "Product Added to Cart.", Toast.LENGTH_SHORT).show();
                                pd.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProductdetailActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                pd.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProductdetailActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                }
            });






        }

    }

    private void dlt_pro() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mref = database.getReference("Product");

        mref.child(p_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ProductdetailActivity.this, "Data Deleted Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProductdetailActivity.this, MainActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductdetailActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProductdetailActivity.this, MainActivity.class));
            }
        });


    }


    private void getProducts() {


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mref = database.getReference("Product");

        mref.child(p_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                key=snapshot.getKey().toString();
                mtitle.setText(product.getTitle());
                mdesc.setText(product.getDescription());
                descval=product.getDescription();
                add_read_more(product.getDescription());
                mcat.setText(product.getCategory());
                mprice.setText("₹ " + (float) product.getPrice());
                imageUrls[0] = product.getImage();
                imageUrls[1] = product.getImage();
                imageUrls[2] = product.getImage();
                mrating.setRating((float) product.getRating().getRate());
                mreview.setText(product.getRating().getCount() + " reviews");

                getcartqty(key);


                ViewPagerAdapter adapter = new ViewPagerAdapter(ProductdetailActivity.this, imageUrls);
                mimage.setAdapter(adapter);
                pd.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.setVisibility(View.GONE);
            }
        });


    }

    private void getcartqty(String key) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mref = database.getReference("cart");

        mref.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(key)){

                    qtyy=Integer.parseInt(snapshot.child(key).child("qty").getValue().toString());
                    mqty.setText(qtyy+"");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    private void add_read_more(String description) {
        final String readMoreText = "...View more";
        final int maxLines = 2; // Assuming you want to truncate at 2 lines

        mdesc.post(new Runnable() {
            @Override
            public void run() {
                if (mdesc.getLineCount() >= maxLines) {
                    Layout layout = mdesc.getLayout();
                    if (layout != null) {
//                        int lineEndIndex = layout.getLineEnd(maxLines - 1);

                        String truncatedText = description.substring(0, 90) + readMoreText;

                        Spannable spannable = new SpannableString(truncatedText);
                        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), truncatedText.length() - readMoreText.length(), truncatedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        mdesc.setText(spannable, TextView.BufferType.SPANNABLE);
                    }
                }
            }
        });
    }

}