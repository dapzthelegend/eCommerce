package com.gamecodeschool.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gamecodeschool.ecommerce.Model.Users;
import com.gamecodeschool.ecommerce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private Button btnJoinNow, btnMainLogin;
    private ProgressDialog mLoadingBar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Paper.init(this);
        btnJoinNow = (Button) findViewById(R.id.btnMainJoinNow);
        btnMainLogin = (Button) findViewById(R.id.btnMainLogin);
        mLoadingBar = new ProgressDialog(this);
        btnMainLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent  = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });


        btnJoinNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent  = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        String UserPhoneNumberKey = Paper.book().read(Prevalent.UserPhoneNumberKey);
        String UserPasswordKey = Paper.book().read(Prevalent.UserPasswordKey);
        if (!TextUtils.isEmpty(UserPasswordKey) && !TextUtils.isEmpty(UserPhoneNumberKey)){
            AllowAccess(UserPhoneNumberKey, UserPasswordKey);
            mLoadingBar.setTitle("Loading Account");
            mLoadingBar.setMessage("Please wait while account is being loaded");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
        }


    }

    private void AllowAccess(final String number, final String password) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Users").child(number).exists()){
                    Users userData = dataSnapshot.child("Users").child(number).getValue(Users.class);
                    if(userData.getPhone().equals(number)){
                        if (userData.getPassword().equals(password)){
                            Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();


                            Intent homeIntent = new Intent(MainActivity.this, HomeActivitry.class);
                            Prevalent.currentUser = userData;
                            startActivity(homeIntent);
                            mLoadingBar.dismiss();

                        }

                        else {
                            Toast.makeText(MainActivity.this, "incorrect password", Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                        }
                    }
                }

                else {
                    Toast.makeText(MainActivity.this, "Account not registered, Create new Account", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
