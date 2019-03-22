package com.gamecodeschool.ecommerce;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gamecodeschool.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ConfirmFinalActivity extends AppCompatActivity {
    private EditText txtOrderName, txtOrderAddress, txtOrderNumber, txtOrderCity;
    private Button btnConfirmOrder;
    private String totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final);
        btnConfirmOrder = (Button) findViewById(R.id.btnConfirmFinalOrder);
        txtOrderName = (EditText) findViewById(R.id.orderConfirmationName);
        txtOrderAddress = (EditText) findViewById(R.id.orderConfirmationAddress);
        txtOrderCity = (EditText) findViewById(R.id.orderConfirmationCity);
        txtOrderNumber = (EditText) findViewById(R.id.orderConfirmationNumber);

        totalAmount = getIntent().getStringExtra("Total Price");
        Toast.makeText(this, "total "+totalAmount , Toast.LENGTH_SHORT).show();
         btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Check();
             }
         });
    }

    private void Check() {
        if (TextUtils.isEmpty(txtOrderAddress.getText().toString())){
            Toast.makeText(this, "please provide your delivery address", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(txtOrderNumber.getText().toString())){
            Toast.makeText(this, "please provide your phone number", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(txtOrderName.getText().toString())){
            Toast.makeText(this, "please provide your full name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(txtOrderCity.getText().toString())){
            Toast.makeText(this, "please provide your city name", Toast.LENGTH_SHORT).show();
        }

        else{
            ConfirmOrder();

        }
    }

    private void ConfirmOrder() {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        final DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentUser.getPhone());
        HashMap<String, Object> orderMap = new HashMap<>();

        orderMap.put("totalAmount", totalAmount);
        orderMap.put("name", txtOrderName.getText().toString());
        orderMap.put("phone", txtOrderNumber.getText().toString());
        orderMap.put("address", txtOrderAddress.getText().toString());
        orderMap.put("city", txtOrderCity.getText().toString());
        orderMap.put("date", saveCurrentDate);
        orderMap.put("time", saveCurrentTime);
        orderMap.put("state", "not shipped");

        orderReference.updateChildren(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseDatabase.getInstance().getReference().child("Cart List")
                        .child("User View")
                        .child(Prevalent.currentUser.getPhone())
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ConfirmFinalActivity.this, "your order has been placed successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ConfirmFinalActivity.this, HomeActivitry.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });

            }
        });



    }
}
