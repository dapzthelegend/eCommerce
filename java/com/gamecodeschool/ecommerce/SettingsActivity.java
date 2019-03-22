package com.gamecodeschool.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gamecodeschool.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView btnClose, btnUpdate, btnChangeProfile;
    private EditText txtProfileName, txtProfileAdress, txtxProfilePhoneNumber;
    private Uri imageUri;
    private String muUrl;
    private StorageReference storageProfilePictureRef;
    private String checker = "";
    private StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        profileImageView = (CircleImageView) findViewById(R.id.settingsProfileImage);
        txtProfileName = (EditText) findViewById(R.id.settingsFullName);
        txtProfileAdress = (EditText) findViewById(R.id.settingsAddress);
        txtxProfilePhoneNumber = (EditText) findViewById(R.id.settingsPhoneNumber);
        btnClose = (TextView) findViewById(R.id.closeSettings);
        btnUpdate = (TextView) findViewById(R.id.updateSettings);
        btnChangeProfile= (TextView) findViewById(R.id.updateProfileImage);

        userInfoDisplay(profileImageView, txtProfileName, txtProfileAdress,txtxProfilePhoneNumber);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checker.equals("clicked")){
                    UserInfoSaved();
                }

                else{
                    UpdateOnlyUserInfo();
                }
            }
        });

        btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileImageView.setImageURI(imageUri);

        }

        else{
            Toast.makeText(this, "Error, Try Again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
            finish();
        }
    }

    private void UpdateOnlyUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String, Object> userMap = new HashMap<>();

        userMap.put("name", txtProfileName.getText().toString());
        userMap.put("address", txtProfileAdress.getText().toString());
        userMap.put("phoneOrder", txtxProfilePhoneNumber.getText().toString());
        ref.child(Prevalent.currentUser.getPhone()).updateChildren(userMap);

        startActivity(new Intent(SettingsActivity.this, HomeActivitry.class));
        Toast.makeText(SettingsActivity.this, "Profile updated successfully...", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void UserInfoSaved() {
        if (TextUtils.isEmpty(txtProfileName.getText().toString())){
            Toast.makeText(this, "write name...", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(txtProfileAdress.getText().toString())){
            Toast.makeText(this, "write address...", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(txtxProfilePhoneNumber.getText().toString())){
            Toast.makeText(this, "write phone number...", Toast.LENGTH_SHORT).show();
        }

        else if(checker.equals("clicked")){
            UploadImage();

        }
    }

    private void UploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Update Profile");
        progressDialog.setMessage("Please wait while profile is updated");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if(imageUri != null){
            final StorageReference fileRef = storageProfilePictureRef.child(Prevalent.currentUser.getPhone() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        muUrl = downloadUrl.toString();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String, Object> userMap = new HashMap<>();

                        userMap.put("name", txtProfileName.getText().toString());
                        userMap.put("address", txtProfileAdress.getText().toString());
                        userMap.put("phoneOrder", txtxProfilePhoneNumber.getText().toString());
                        userMap.put("image", muUrl);
                        ref.child(Prevalent.currentUser.getPhone()).updateChildren(userMap);
                         progressDialog.dismiss();

                         startActivity(new Intent(SettingsActivity.this, HomeActivitry.class));
                        Toast.makeText(SettingsActivity.this, "Profile updated successfully...", Toast.LENGTH_SHORT).show();
                         finish();
                    }
                    
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        else{
            Toast.makeText(this, "image not selected", Toast.LENGTH_SHORT).show();
        }



    }

    private void userInfoDisplay(final CircleImageView profileImageView, final EditText txtProfileName, final EditText txtProfileAdress, final EditText txtxProfilePhoneNumber) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentUser.getPhone());
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("image").exists()){
                    String image = dataSnapshot.child("image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String address = dataSnapshot.child("address").getValue().toString();
                    Picasso.get().load(image).into(profileImageView);
                    txtProfileName.setText(name);
                    txtProfileAdress.setText(address);
                    txtxProfilePhoneNumber.setText(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
