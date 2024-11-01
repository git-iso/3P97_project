package com.example.a3p97_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Students extends AppCompatActivity {

    SearchView searchView;
    ListView listView;
    String[] nameList = {"Club A","Club B","Club C","Club D","Club E","Club F","Club G","Club H","Club I","Club J","Club K","Club L","Club M","Club N","Club O","Club P","Club Q","Club R","Club S"};
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);

        searchView = findViewById(R.id.search_bar);
        listView = findViewById(R.id.list_club);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, nameList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent n = new Intent(Students.this, Subs.class);
                startActivity(n);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Students.this.arrayAdapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Students.this.arrayAdapter.getFilter().filter(s);
                return false;
            }
        });

    }


}