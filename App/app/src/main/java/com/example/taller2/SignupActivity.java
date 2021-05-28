package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    //------------------------------------
    //          Attributes
    //------------------------------------
    Button signupBtn, takePictureBtn;
    EditText firstName, secondName, email, password, id, latitude, longitude;

    //------- Firebase -------
    FirebaseAuth userAuth;
    DatabaseReference database;
    FirebaseStorage storage;
    StorageReference storageReference;
    String currentUserId = "";

    //------ Picture ------
    private Uri imagePath = null;
    private Bitmap takenPicture;

    //------- Camera -------
    private static final int cameraPermissionCode = 100; // Code for the camera permission
    private static final int SELECTED_PICTURE = 200; // Code to represent the action to select a picture
    private boolean pictureTaken = false; // Check if photo is selected


    //------------------------------------
    //          On create
    //------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //-----------------------------------
        //       Configurations
        //-----------------------------------
        userAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //-----------------------------------
        //       Camera permissions
        //-----------------------------------
        // Camera permission
        String[] cameraPermission = new String[] {Manifest.permission.CAMERA};
        // Check if permission is already granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
        }  else {
            ActivityCompat.requestPermissions(this, cameraPermission, cameraPermissionCode);
        }
        //-----------------------------------
        //          Take picture
        //-----------------------------------
        takePictureBtn = (Button) findViewById(R.id.signup_btn_agregarFoto);
        takePictureBtn.setOnClickListener( ( view ) -> {
            selectPhotoFromGallery();
        });

        //-----------------------------------
        //          Register user
        //-----------------------------------
        // Assign fields
        firstName = (EditText) findViewById(R.id.signup_nombre);
        secondName = (EditText) findViewById(R.id.signup_apellido);
        email = (EditText) findViewById(R.id.signup_email);
        password = (EditText) findViewById(R.id.signup_contraseña);
        id = (EditText) findViewById(R.id.signup_identificacion);
        latitude = (EditText) findViewById(R.id.signup_latitud);
        longitude = (EditText) findViewById(R.id.signup_longitud);
        // Listener on button
        signupBtn = (Button) findViewById(R.id.signup_btn_registrarse);
        signupBtn.setOnClickListener( view -> {
            // Validate form
            boolean isFormValid = validateForm();
            if (!isFormValid) {
                Toast.makeText(view.getContext(), "Campos inválidos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user in Firebase
            registerUser();

            // Start main activity
            finish();
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
        });
    }

    //-----------------------------------
    //            Methods
    //-----------------------------------
    // Function to register a user to firebase
    private void registerUser() {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();
        userAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            currentUserId = userAuth.getCurrentUser().getUid();

                            Map<String, String> userFields = new HashMap<>();
                            userFields.put("Nombres", firstName.getText().toString());
                            userFields.put("Apellidos", secondName.getText().toString());
                            userFields.put("Email", email.getText().toString());
                            userFields.put("Password", password.getText().toString());
                            userFields.put("Id", id.getText().toString());
                            userFields.put("Latitud", latitude.getText().toString());
                            userFields.put("Longitud", longitude.getText().toString());
                            userFields.put("Disponible", "false");
                            userFields.put("uid", currentUserId);

                            // Save user's info in Firebase Database
                            database.child("Users").child(currentUserId).setValue(userFields)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task2) {
                                            if (task2.isSuccessful()) {
                                                uploadImageToFirebase();
                                            }
                                            else {
                                                Toast.makeText(SignupActivity.this, "Ocurrió un error al guardar los datos del usuario", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignupActivity.this, "Ocurrió un error al guardar la imagen del usuario", Toast.LENGTH_SHORT).show();
                                        }
                            });
                        }
                        else {
                            Toast.makeText(SignupActivity.this, "Ocurrió un error al registrar al usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadImageToFirebase() {
        if (imagePath != null) {
            StorageReference ref = storageReference.child("Users/" + currentUserId);
            ref.putFile(imagePath)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(SignupActivity.this, "Imagen subida a Firebase", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignupActivity.this, "Error subiendo la imagen a firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(SignupActivity.this, "Error al subir imagen no se encontró el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to validate user form
    private boolean validateForm() {
        boolean isValid = false;
        // Validate image
        if (pictureTaken == false) {
            Toast.makeText(getBaseContext(), "Foto del usuario requerida", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Validate first name
        if (firstName.getText().toString().isEmpty()) {
            firstName.setError("Nombre requerido");
            return false;
        }
        // Validate second name
        if (secondName.getText().toString().isEmpty()) {
            secondName.setError("Apellido requerido");
            return false;
        }
        // Validate email
        if (email.getText().toString().isEmpty()) {
            email.setError("Email reqerido");
            return false;
        }
        // Validate id
        if (id.getText().toString().isEmpty()) {
            id.setError(("Número de identificación requerido"));
            return false;
        }
        // Validate password
        if (password.getText().toString().isEmpty() || password.getText().toString().length() < 6) {
            password.setError("La contraseña debe tener mínimo 6 caracteres y es requerida");
            return false;
        }
        // Validate Latitude
        if (latitude.getText().toString().isEmpty()) {
            latitude.setError("Latitud del usuario requerida");
            return false;
        }
        // Validate Longitude
        if (longitude.getText().toString().isEmpty()) {
            longitude.setText("Longitud requerida");
            return false;
        }

        // Otherwise return true
        return true;
    }

    // Function to select a photo from the gallery
    public void selectPhotoFromGallery() {
        Intent selectPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        // Start activity and wait for result
        startActivityForResult(Intent.createChooser(selectPictureIntent, "Select Picture"), SELECTED_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Should always have this super() call
        super.onActivityResult(requestCode, resultCode, data);
        //---------   Handle select a photo  ------------
        if (requestCode == SELECTED_PICTURE && resultCode == RESULT_OK) {
            imagePath = data.getData();
            if (imagePath != null) {
                pictureTaken = true;
                ImageView load_image = (ImageView) findViewById(R.id.signup_user_image);
                load_image.setImageURI(imagePath);
            }
        }
    }

}