package com.example.taller2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ShowUsersActivity extends AppCompatActivity {

    //--------------------------------------------------
    //                  Variables
    //--------------------------------------------------
    // ArrayList<User> 

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
        protected String lastName;
        protected String email;
        protected String password;
        protected String image;
        protected String idNumber;
        protected double latitude;
        protected double longitude;

        // Constructor(s)

    }
}