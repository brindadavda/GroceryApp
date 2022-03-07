package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.Constans;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt , descriptionEt;
    private TextView catagoryTv , quantityEt , priceEt , discountedPriceEt , discountedNoteEt;
    private SwitchCompat discountedSwitch;
    private Button updateProductBtn;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image pic constans
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermission;
    private String[] storagePermission;

    //image picked uri
    private Uri image_uri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String productId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconTv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        catagoryTv = findViewById(R.id.catagoryTv);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedSwitch = findViewById(R.id.discountSwitch);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        updateProductBtn = (Button)findViewById(R.id.updateProductBtn);

        //get id of product from intent
        productId = getIntent().getStringExtra("productId");

        //on start is unchecked , so hide discountedPriceEt , discountNoteEt
        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        loadProductDetails(); // to set on views

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission arrays
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //if discountedSwitch is checked: show discountPriceEt , discountNoteEt | if discountSwitch is not checked: hide discountedPriceEt, discountNoteEt
        discountedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //checked , show discountPriceEt , discountNoteEt
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);
                }
                else
                {
                    //unchecked, hide discountedPriceEt , discountNoteEt
                    discountedPriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });

        catagoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick catagory
                catagoryDialog();
            }
        });

        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow :
                //1) Input data
                //2) validate data
                //3) Update data to db
                inputData();
            }
        });
    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String productId = ""+snapshot.child("productId").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String productIcon = ""+snapshot.child("productIcon").getValue();
                        String originalPrice = ""+snapshot.child("originalPrice").getValue();
                        String discountedPrice = ""+snapshot.child("discountedPrice").getValue();
                        String discountedNote = ""+snapshot.child("discountedNote").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        //set data to view
                        if(discountAvailable.equals("true")){
                            discountedSwitch.setChecked(true);

                            discountedPriceEt.setVisibility(View.VISIBLE);
                            discountedNoteEt.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            discountedSwitch.setChecked(false);

                            discountedPriceEt.setVisibility(View.GONE);
                            discountedNoteEt.setVisibility(View.GONE);
                        }

                        titleEt.setText(productTitle);
                        descriptionEt.setText(productDescription);
                        catagoryTv.setText(productCategory);
                        quantityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountedPriceEt.setText(discountedPrice);

                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_baseline_add_shopping_cart_white).into(productIconIv);
                        }
                        catch (Exception e){
                            productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_white);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle, productDescription , productCategory , productQuantity,originalPrice,discountedPrice , discountedNote;
    private boolean discountAvailable = false;

    private void inputData() {
        //1) Input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = catagoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountedSwitch.isChecked(); //true/false

        //2) Validate data
        if(TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is required...", Toast.LENGTH_SHORT).show();
            return; // don't proceed further
        }
        if(TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price is required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(discountAvailable) {
            //product is with discount
            discountedPrice = discountedPriceEt.getText().toString().trim();
            discountedNote = discountedNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountedPrice)) {
                Toast.makeText(this, "Discounted Price is required..", Toast.LENGTH_SHORT).show();
                return;
            }
        }
            else {
                //product is without discount
                discountedPrice = "0";
                discountedNote = "";
            }

            updateProduct();

    }

    private void updateProduct() {
        //show progress
        progressDialog.setMessage("Updating product....");
        progressDialog.show();

        if (image_uri == null){
            //update without image

            //setuo data in hashmap to update
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountedPrice", "" + discountedPrice);
            hashMap.put("discountedNote", "" + discountedNote);
            hashMap.put("discountAvailable", "" + discountAvailable);

            //update to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //update success
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Update....", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //update failed
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else
        {
            //update with image

            //first upload image
            //image name and path on firebase storage
            String filePathAndName = "product_image/"+""+productId; // overide previous image using same id
            //uploaded image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uplodded, get url of uplodded image

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productIcon",""+downloadImageUri);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountedPrice", "" + discountedPrice);
                                hashMap.put("discountedNote", "" + discountedNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);

                                //update to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //update success
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Update....", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //update failed
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //upload failed
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void catagoryDialog() {
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constans.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get picked category
                        String category = Constans.productCategories[which];

                        //set picked category
                        catagoryTv.setText(category);
                    }
                })
                .show();
    }

    private void showImagePickDialog() {
        //option to display in dialog
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if(which == 0){
                            //camera clicked
                            if(checkCameraPermission()){
                                //permission Granted
                                pickFromCamera();
                            }
                            else
                            {
                                //permission not Granted
                                requesCameraPermission();
                            }
                        }
                        else
                        {
                            //gallery clicked
                            if(checkStoragePermission()){
                                //permission granted
                                pickFromGallery();

                            }
                            else
                            {
                                //permission not granted
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private  void pickFromGallery(){
        //intent to pick Image from gallary
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        //using media store to pic high/original quality image
        ContentValues contentValues =new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requesCameraPermission(){
        ActivityCompat.requestPermissions(this,storagePermission,CAMERA_REQUEST_CODE);
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission Allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this,"Camera permission is necessary..",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if ( storageAccepted) {
                        //permission Allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this,"Storage permission is necessary..",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){

                image_uri = data.getData();
                productIconIv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                productIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}