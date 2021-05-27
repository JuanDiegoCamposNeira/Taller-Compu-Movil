package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ShowUsersActivity extends AppCompatActivity {

    //--------------------------------------------------
    //                  Variables
    //--------------------------------------------------
    // Users
    private ArrayList<User> activeUsers;
    // Firebase
    private FirebaseAuth userAuth;
    private DatabaseReference database;

    //--------------------------------------------------
    //                  On Create
    //--------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);

        // List view configurations
        ListView showUsers = (ListView) findViewById(R.id.show_users);

    }

    //--------------------------------------------------
    //                  Menu
    //--------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            // Sign out, selected
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;

            case R.id.menu_set_available:
                database.child("Disponible").get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Get current state
                                    String currentState = String.valueOf(task.getResult().getValue());
                                    currentState = (currentState.equals("false")) ? "true" : "false";
                                    // Write new state
                                    database.child("Disponible").setValue(currentState);
                                    String message = "Su estado ahora es [ ";
                                    message += (currentState.equals("false")) ? "No disponible ]" : "Disponible ]";
                                    Toast.makeText(ShowUsersActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                return true;

            case R.id.menu_show_active_users:
                startActivity(new Intent(this, ShowUsersActivity.class));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

        //--------------------------------------------------
    //               Custom adapter
    //--------------------------------------------------
    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

    }

    //--------------------------------------------------
    //                  User
    //--------------------------------------------------
    class User {
        // Attributes
        protected String firstName;
        protected String image;
        protected double latitude;
        protected double longitude;

        // Constructor(s)

    }
}