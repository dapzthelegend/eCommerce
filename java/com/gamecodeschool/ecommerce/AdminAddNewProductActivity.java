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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {
    
    private String categoryName, Description, Price, Name, saveCurrentDate, saveCurrentTime;
    private Button btnAddNewProduct;
    private EditText txtProductName, txtProductPrice, txtProductDescription;
    private ImageView productImage;
    private  static  final int GALLERY_REQUEST_CODE = 1;
    private Uri imageUri;
    private String productRandomKey, downloadImageUrl;
    private StorageReference ProductImagesRef;
    private DatabaseReference productRef;
    private ProgressDialog mLoadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);


        categoryName = getIntent().getExtras().get("category").toString();
        ProductImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productRef = FirebaseDatabase.getInstance().getReference().child("Products");
        mLoadingBar = new ProgressDialog(this);

       btnAddNewProduct = (Button) findViewById(R.id.btnAddNewProduct);
       txtProductName = (EditText) findViewById(R.id.productName);
       productImage = (ImageView) findViewById(R.id.productImage);
        txtProductPrice = (EditText) findViewById(R.id.productPrice);
        txtProductDescription = (EditText) findViewById(R.id.productDescription);

        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        btnAddNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });




    }

    private void ValidateProductData() {
        Description = txtProductDescription.getText().toString();
        Price = txtProductPrice.getText().toString();
        Name = txtProductName.getText().toString();

        if (imageUri == null){
            Toast.makeText(this, "Product image is required", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Product decription is required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Price)){
            Toast.makeText(this, "Product price is required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Name)){
            Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
        }

        else{
            StoreProductInformation();
        }

    }

    private void StoreProductInformation() {
        mLoadingBar.setTitle("Adding New Product");
        mLoadingBar.setMessage("Please wait while new product is being added");
        mLoadingBar.setCanceledOnTouchOutside(false);
        mLoadingBar.show();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filepath = ProductImagesRef.child(imageUri.getLastPathSegment() + productRandomKey + ".jpg");
        final UploadTask uploadTask = filepath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                 mLoadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this, "Product image uploaded successfully", Toast.LENGTH_SHORT).show();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){

                            throw  task.getException();

                        }
                        downloadImageUrl = filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();


                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            downloadImageUrl = task.getResult().toString();
                           //   Toast.makeText(AdminAddNewProductActivity.this, "Product", Toast.LENGTH_SHORT).show();
                       SaveProductInfoToDatabase();
                        }
                    }
                });
            }
        });






    }

    private void SaveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", Description);
        productMap.put("image", downloadImageUrl);
        productMap.put("category", categoryName);
        productMap.put("price",Price);
        productMap.put("name", Name);

        productRef.child(productRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
             if(task.isSuccessful()){
                 mLoadingBar.dismiss();
                 Toast.makeText(AdminAddNewProductActivity.this, "product added successfully..,", Toast.LENGTH_SHORT).show();

                 Intent adminIntent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
                 startActivity(adminIntent);
             }

             else{
                 mLoadingBar.dismiss();
                 String message = task.getException().toString();
                 Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                 Intent adminIntent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
                 startActivity(adminIntent);
             }
            }
        });


    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            productImage.setImageURI(imageUri);


            }
    }
}
