package com.gamecodeschool.ecommerce;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.gamecodeschool.ecommerce.Model.Products;
import com.gamecodeschool.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity {
   private Button btnAddToCart;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView txtProductPrice, txtProductName, txtProductDescription;
    private String productID = "", state = "Normal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        productID = getIntent().getStringExtra("pid");

      btnAddToCart = (Button) findViewById(R.id.productDetailsATC);
        numberButton = (ElegantNumberButton) findViewById(R.id.btnNumber);
        productImage = (ImageView) findViewById(R.id.productDetailsImage);
        txtProductName = (TextView) findViewById(R.id.productDetailsName);
        txtProductPrice = (TextView) findViewById(R.id.productDetailsPrice);
        txtProductDescription = (TextView) findViewById(R.id.productDetailsDescription);

        getProductDetails(productID);

        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("state", state);

                if(state.equals("Order Placed") || state.equals("Order Shipped")){
                    Toast.makeText(ProductDetailsActivity.this, "you can add to cart once your order has been confirmed", Toast.LENGTH_LONG).show();
                }
               else {
                    AddingToCartList();
                }
            }
        });


    }

    private void AddingToCartList() {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

       final  DatabaseReference cartListReference = FirebaseDatabase.getInstance().getReference().child("Cart List");

        final HashMap<String, Object> cartMap = new HashMap<>();

        cartMap.put("pid", productID);
        cartMap.put("name", txtProductName.getText().toString());
        cartMap.put("price", txtProductPrice.getText().toString());
        cartMap.put("date", saveCurrentDate);
        cartMap.put("time", saveCurrentTime);
        cartMap.put("quantity", numberButton.getNumber());
        cartMap.put("discount", "");

        cartListReference.child("User View").child(Prevalent.currentUser.getPhone()).child("Products")
                .child(productID).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    cartListReference.child("Admin View").child(Prevalent.currentUser.getPhone()).child("Products")
                            .child(productID).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ProductDetailsActivity.this, "Added to cart list...", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ProductDetailsActivity.this, HomeActivitry.class);
                                startActivity(intent);
                            }

                        }
                    });

                }
            }
        });



    }

    private void getProductDetails(String productID) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference().child("Products");

        productRef.child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Products products= dataSnapshot.getValue(Products.class);
                    txtProductName.setText(products.getName());
                    txtProductPrice.setText( products.getPrice());
                    txtProductDescription.setText(products.getDescription());
                    Picasso.get().load(products.getImage()).into(productImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void checkOrderStat(){
        final DatabaseReference orderRef;
        orderRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentUser.getPhone());

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String shippingstate = dataSnapshot.child("state").getValue().toString();
                    Log.e("shippingstate", shippingstate);
                    if(shippingstate.equals("shipped")){
                        state = "Order Shipped";

                    }

                    else if(shippingstate.equals("not shipped")){
                        state = "Order Placed";
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkOrderStat();

    }
}
