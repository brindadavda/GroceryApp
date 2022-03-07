package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {


    private ImageButton backBtn, gpsBtn;
    private ImageView profileIv;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt, addressEt, emailEt, passwordEt, cPasswordEt;
    private Button registerBtn;
    private TextView registerSellerTv;
    private double longitude, latitude;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //permission arrays
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;

    //permission constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //image picked url
    private Uri image_uri;

    private LocationManager locationManager;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //initialization of UI Views
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        cPasswordEt = findViewById(R.id.cpasswordEt);
        registerBtn = findViewById(R.id.registerBtn);
        registerSellerTv = findViewById(R.id.registerSellerTv);

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //delete current location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    //not allowed , request
                    requestLocationPermission();
                }
            }
        });

        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //pick image
                showImagePickDialog();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Register user
                inputData();
            }
        });


        registerSellerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open register seller activity
                startActivity(new Intent(RegisterUserActivity.this,RegisterSellerActivity.class));
            }
        });
    }


    private String fullname,  phoneNumber ,  country , state , city , address , email , password , confirmPassword;

    private void inputData() {
        fullname = nameEt.getText().toString().trim();
        phoneNumber = phoneEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        confirmPassword = cPasswordEt.getText().toString().trim();

        //validate data
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Enter Phone Number...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(latitude == 0.0 || longitude == 0.0){
            Toast.makeText(this, "Please click on GPS Button to detect location...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email address...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() < 5){
            Toast.makeText(this, "password must be at least 6 character long....", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Password doesn't match..", Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account....");
        progressDialog.dismiss();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        saveFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed create account
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("Saving Account Info...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        if(image_uri == null){
            //save info without image

            //setup data to save
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("name",""+fullname);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("password",""+password);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","User");
            hashMap.put("online","true");
            hashMap.put("shopOpen","true");
            hashMap.put("profileImage","");

            //save to do
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //db update
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUserActivity.this,LoginActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating db
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUserActivity.this,LoginActivity.class));
                            finish();
                        }
                    });

        }
        else
        {
            //save info with image

            //name and path of image
            String filePathAndName = "profile_images/"+""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of upload image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){

                                //setup data to save
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("email",""+email);
                                hashMap.put("name",""+fullname);
                                hashMap.put("phone",""+phoneNumber);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("password",""+password);
                                hashMap.put("accountType","User");
                                hashMap.put("online","true");
                                hashMap.put("shopOpen","true");
                                hashMap.put("profileImage",""+downloadImageUri); //url of upload image

                                //save to do
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //db update
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUserActivity.this,LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed updating db
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUserActivity.this,LoginActivity.class));
                                                finish();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }


    private void showImagePickDialog() {
        //options to display
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, (dialog, which) -> {
                    //handle clicked
                    if (which == 0) {
                        //camera clicked
                        if (checkCameraPermission()) {
                            pickFromCamera();
                        } else {
                            requestCameraPermission();
                        }

                    } else {
                        //gallery clicked
                        if (checkStoragePermission()) {
                            pickFromGallery();
                        } else {
                            requestStoragePermission();
                        }
                    }
                })
                .show();
    }

        private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

        private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Team_Image Descriptive");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

        private void detectLocation() {
        Toast.makeText(this, "Please Wait...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

    private void findAddress() {
        //find Address country , state , city
        Geocoder geocoder;
        List<Address> addresses;

        geocoder = new Geocoder(this , Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);

            String address = addresses.get(0).getAddressLine(0);//complete Address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set addresses
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);

        } catch (IOException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

        private boolean checkLocationPermission(){

            boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
            return result;
        }

        private void requestLocationPermission(){
            ActivityCompat.requestPermissions(this,locationPermission,LOCATION_REQUEST_CODE);
        }

        private boolean checkStoragePermission(){
            boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

            return result;
        }

        private void requestStoragePermission(){
            ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
        }

        private boolean checkCameraPermission(){
            boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
            boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

            return result && result1;
        }

        private void requestCameraPermission(){
            ActivityCompat.requestPermissions(this,storagePermission,CAMERA_REQUEST_CODE);
        }



        @Override
        public void onLocationChanged(@NonNull Location location) {
            //location detected
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            findAddress();
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            //gpslocation disable
            Toast.makeText(this,"Please turn on  location..." , Toast.LENGTH_LONG).show();
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode){
                case LOCATION_REQUEST_CODE: {
                    if (grantResults.length > 0) {
                        boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if (locationAccepted) {
                            //permission Allowed
                            detectLocation();
                        } else {
                            //permission denied
                            Toast.makeText(this,"Location permission is necessary..",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
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

                    assert data != null;
                    image_uri = data.getData();
                    profileIv.setImageURI(image_uri);


                }
                else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                    profileIv.setImageURI(image_uri);
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }