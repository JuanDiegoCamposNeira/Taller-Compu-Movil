package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShowUsersActivity extends AppCompatActivity {

    //--------------------------------------------------
    //                  Variables
    //--------------------------------------------------
    // Users
    private ArrayList<User> activeUsers;
    // Firebase
    private FirebaseAuth userAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String currentUserId;
    // Listview
    private boolean firstFill = false;

    //--------------------------------------------------
    //                  On Create
    //--------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);

        // Configurations
        activeUsers = new ArrayList<User>();
        userAuth = FirebaseAuth.getInstance();
        currentUserId = userAuth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("Users");

        // Read all active users from DB and asign a listener to the users
        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Traverse snapshot array
                int activeUsersBefore = activeUsers.size();
                activeUsers.clear();
                for (DataSnapshot singleUser : snapshot.getChildren()) {
                    User user = singleUser.getValue( User.class );
                    Log.i("USER", user.uid);
                    Log.i("USER", user.Nombres);
                    if ( user.Disponible.equals("true") && !user.uid.equals(currentUserId) ) {
                        activeUsers.add(user);
                    }
                }
                // Update List View if a new user was entered
                if (activeUsersBefore != activeUsers.size() || !firstFill) {
                    firstFill = true;
                    CustomAdapter adapter = new CustomAdapter();
                    ListView listview = (ListView) findViewById(R.id.show_users);
                    listview.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("ActiveUser", "Cancelled");
            }
        };
        databaseReference.addValueEventListener(usersListener);


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
                databaseReference.child(currentUserId).child("Disponible").get()
                                 .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Get current state
                                    String currentState = String.valueOf(task.getResult().getValue());
                                    currentState = (currentState.equals("false")) ? "true" : "false";
                                    // Write new state
                                    databaseReference.child(currentUserId).child("Disponible").setValue(currentState);
                                    String message = "Su estado ahora es [ ";
                                    message += (currentState.equals("false")) ? "No disponible ]" : "Disponible ]";
                                    Toast.makeText(ShowUsersActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                return true;

            case R.id.menu_show_active_users:
                Toast.makeText(ShowUsersActivity.this, "Ya se encuentra en esta actividad", Toast.LENGTH_SHORT).show();
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
            return activeUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return activeUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.user_template, null);

            Button showLocationBtn = (Button) convertView.findViewById(R.id.user_template_show_location_btn);
            ImageView profilePhoto = (ImageView) convertView.findViewById(R.id.user_template_image);
            TextView name = (TextView) convertView.findViewById(R.id.user_template_name);

            name.setText(activeUsers.get(position).Nombres);
            StorageReference photo = storageReference.child(activeUsers.get(position).uid);
            photo.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profilePhoto));

            showLocationBtn.setOnClickListener( view -> {
                // Launch new intent with location information
                Intent showUserLocation = new Intent(view.getContext(), SinglePersonLocationActivity.class);
                showUserLocation.putExtra("nombre", activeUsers.get(position).Nombres);
                showUserLocation.putExtra("lat", activeUsers.get(position).Latitud);
                showUserLocation.putExtra("lng", activeUsers.get(position).Longitud);
                startActivity(showUserLocation);
            });

            // Return view
            return convertView;
        }

    }

}