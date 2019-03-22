package com.gamecodeschool.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gamecodeschool.ecommerce.Model.Users;
import com.gamecodeschool.ecommerce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private EditText txtPhoneNumber, txtPassword;
    private Button btnLogin;
    private ProgressDialog mLoadingBar;
    private String parentDBName = "Users";
    private TextView txtAdminLink, txtNotAdminLink;
    private DatabaseReference nameRef;
    final String[] name = new String[1];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtPhoneNumber = (EditText) findViewById(R.id.LoginPhoneNumber);
        txtPassword = (EditText) findViewById(R.id.LoginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        mLoadingBar = new ProgressDialog(this);
        txtAdminLink = (TextView) findViewById(R.id.adminPanelLink);
        txtNotAdminLink = (TextView) findViewById(R.id.notAdminPanelLink);
        nameRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });
        Paper.init(this);

        txtAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogin.setText("Login Admin");
                txtAdminLink.setVisibility(View.INVISIBLE);
                txtNotAdminLink.setVisibility(View.VISIBLE);
                parentDBName = "Admins";

            }
        });

        txtNotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogin.setText("Login");
                txtAdminLink.setVisibility(View.VISIBLE);
                txtNotAdminLink.setVisibility(View.INVISIBLE);
                parentDBName = "Users";
            }
        });
    }
//this method allows users to login
    private void LoginUser() {
        String number = txtPhoneNumber.getText().toString();
        String password = txtPassword.getText().toString();




         if (TextUtils.isEmpty(number)){
            Toast.makeText(this, "Please write your phone number...", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }

        else{
             mLoadingBar.setTitle("Login Account");
             mLoadingBar.setMessage("Please wait while account is being logged in");
             mLoadingBar.setCanceledOnTouchOutside(false);
             mLoadingBar.show();

             AllowAccessToccount(number, password);
         }
    }

    private void AllowAccessToccount(final String number, final String password) {
        Paper.book().write(Prevalent.UserPhoneNumberKey, number);
        Paper.book().write(Prevalent.UserPasswordKey, password);

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(parentDBName).child(number).exists()){
                    Users userData = dataSnapshot.child(parentDBName).child(number).getValue(Users.class);
                    if(userData.getPhone().equals(number)){
                        if (userData.getPassword().equals(password)){
                           if(parentDBName.equals("Admins")){
                               Toast.makeText(LoginActivity.this, "Welcome Admin", Toast.LENGTH_SHORT).show();
                               mLoadingBar.dismiss();

                               Intent adminIntent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               startActivity(adminIntent);
                               finish();
                           }
                           else if(parentDBName.equals("Users")) {
                               Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                               mLoadingBar.dismiss();

                               Intent homeIntent = new Intent(LoginActivity.this, HomeActivitry.class);
                               Prevalent.currentUser = userData;
                               homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               startActivity(homeIntent);
                               finish();

                           }

                        }

                        else {
                            Toast.makeText(LoginActivity.this, "incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                else {
                    Toast.makeText(LoginActivity.this, "Account not registered, Create new Account", Toast.LENGTH_SHORT).show();
                mLoadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
