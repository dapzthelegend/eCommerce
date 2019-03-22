package com.gamecodeschool.ecommerce;

import android.app.ProgressDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.paperdb.Paper;

public class RegisterActivity extends AppCompatActivity {
    private Button btnCreateAccount;
    private EditText txtPassword, txtUserName, txtPhoneNumber;
    private ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Paper.init(this);


        btnCreateAccount = (Button) findViewById(R.id.btnRegister);
        txtPassword = (EditText) findViewById(R.id.registerPassword);
        txtPhoneNumber = (EditText) findViewById(R.id.registerPhoneNumber);
        txtUserName = (EditText) findViewById(R.id.registerUserName);
        mLoadingBar = new ProgressDialog(this);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAccount();
            }
        });
    }

    private void CreateAccount() {
        String name = txtUserName.getText().toString();
        String number = txtPhoneNumber.getText().toString();
        String password = txtPassword.getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please write your name...", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(number)){
            Toast.makeText(this, "Please write your phone number...", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else{
            mLoadingBar.setTitle("Create Account");
            mLoadingBar.setMessage("Please wait while account is being created");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            ValidatePhoneNumber(name, number, password);
        }
    }

    private void ValidatePhoneNumber(final String name, final String number, final String password) {
  final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
  rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
          if(!(dataSnapshot.child("Users").child(number).exists())){
              HashMap<String, Object> userData = new HashMap<>();
              userData.put("phone", number);
              userData.put("password", password);
              userData.put("name", name);

              rootRef.child("Users").child(number).updateChildren(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful()){
                          Toast.makeText(RegisterActivity.this, "account created successfully", Toast.LENGTH_SHORT).show();
                          mLoadingBar.dismiss();
                          Intent loginIntent  = new Intent(RegisterActivity.this, LoginActivity.class);
                          startActivity(loginIntent);
                      }

                      else{
                          Toast.makeText(RegisterActivity.this, "Network Error Please Try Again", Toast.LENGTH_SHORT).show();
                      }

                  }
              });


          }

          else{
              Toast.makeText(RegisterActivity.this, "This number has been registered...", Toast.LENGTH_SHORT).show();
              mLoadingBar.dismiss();
              Toast.makeText(RegisterActivity.this, "try again using another phone number...", Toast.LENGTH_SHORT).show();
              Intent registerIntent  = new Intent(RegisterActivity.this, MainActivity.class);
              startActivity(registerIntent);
          }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
  });
    }
}
