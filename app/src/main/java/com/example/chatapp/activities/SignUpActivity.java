package com.example.chatapp.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    //all signup form fields
    EditText nameEditText;
    EditText emailEditText ;
    EditText password;
    EditText confirmPassword ;
    MaterialButton signUpButton;
    TextView signInText;
    ProgressBar signUpProgressBar;
    private String encodedImage;
    RoundedImageView profileImage ;
    TextView addImageText ;
    FrameLayout layoutImage;
    PreferenceManager preferenceManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEditText = findViewById(R.id.SignUpNameEditText);
        emailEditText = findViewById(R.id.signUpEmailEditText);
        password = findViewById(R.id.signUpPasswordEditText);
        confirmPassword = findViewById(R.id.signUpConfirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signInText = findViewById(R.id.signUpSignInText);
        signUpProgressBar = findViewById(R.id.signUpProgressBar);
        profileImage = findViewById(R.id.imageProfile);
        addImageText = findViewById(R.id.addImageText);
        layoutImage = findViewById(R.id.layoutImage);

        preferenceManager = new PreferenceManager(getApplicationContext());

        Log.d("Signup : ","Sign up activity opened !");

        //picking image
        layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidSignUp()){
                    signUp();
                }
            }
        });

        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(getApplicationContext(),SignInActivity.class);
            }
        });


    }

    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> data = new HashMap<>();
        data.put(Constants.KEY_NAME,nameEditText.getText().toString());
        data.put(Constants.KEY_EMAIL,emailEditText.getText().toString());
        data.put(Constants.KEY_PASSWORD,password.getText().toString());
        data.put(Constants.KEY_IMAGE,encodedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_NAME,nameEditText.getText().toString());
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Log.d("SignUp activity","Calling MainActivity");
                    startActivity(intent);


                })
                .addOnFailureListener(exception->{
                    loading(false);
                    showToast(exception.getMessage());

                });


    }

    private String encodeImage(Bitmap bitmap){

        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth/bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);

    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            profileImage.setImageBitmap(bitmap);
                            addImageText.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );


    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    private Boolean isValidSignUp(){
        if(encodedImage == null){
            showToast("Select a profile Image");
            return false;
        }
        else if(nameEditText.getText().toString().trim().isEmpty()){
            showToast("Enter Name");
            return false;
        }
        else if(emailEditText.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()){
            showToast("Enter valid Email Address");
            return false;
        }
        else if(password.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }
        else if(confirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm Password");
            return false;
        }
        else if(!password.getText().toString().equals(confirmPassword.getText().toString())){
            showToast("Password and Confirm Password must be same");
            return false;
        }
        else{
            return true;
        }
    }

    private void loading(boolean isloading){
        if(isloading){
            signUpButton.setVisibility(View.INVISIBLE);
            signUpProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            signUpButton.setVisibility(View.VISIBLE);
            signUpProgressBar.setVisibility(View.INVISIBLE);
        }
    }

}