package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //------------------------------------
    //          Attributes
    //------------------------------------
    private Button loginBtn, signupBtn;
    private EditText email, password;
    // Firebase Auth
    private FirebaseAuth userAuth;

    //------------------------------------
    //          On create
    //------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //-----------------
        // Configurations
        //-----------------
        // Form fields
        email = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById((R.id.login_password));

        // Firebase
        userAuth = FirebaseAuth.getInstance();

        signupBtn = (Button) findViewById(R.id.login_btn_registrarse);
        signupBtn.setOnClickListener( (view) -> {
            // Launch activity with intent
            Intent signupActivity = new Intent( view.getContext(), SignupActivity.class );
            startActivity( signupActivity );
        });

        loginBtn = (Button) findViewById(R.id.login_btn_ingresar);
        loginBtn.setOnClickListener( (view) -> {
            // Validate form
            boolean isFormValid = validateForm();
            if (isFormValid) {
                userAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity( new Intent(LoginActivity.this, MainActivity.class) );
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Ocurrió un error en la autenticaión del usuario", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(LoginActivity.this, "Formulario inválido, revise los campos", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //------------------------------------
    //          Methods
    //------------------------------------
    private boolean validateForm() {
        if (email.getText().toString().isEmpty()) {
            email.setError("Email requerido");
            return false;
        }
        if (password.getText().toString().isEmpty()) {
            password.setError("Contraseña requerida");
            return false;
        }
        return true;
    }

}