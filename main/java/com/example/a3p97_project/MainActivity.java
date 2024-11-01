package com.example.a3p97_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.*;

import com.android.billingclient.api.*;
import com.example.a3p97_project.databinding.ActivitySubsBinding;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private BillingClient billingClient;
    boolean isPremium = false;

    SearchView searchView;
    ListView listView;
    String[] nameList = {"School A","School B","School C","School D","School E","School F","School G","School H","School I","School J","School K","School L","School M","School N","School O","School P","School Q","School R","School S"};
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        query_purchase();

        searchView = findViewById(R.id.search_bar);
        listView = findViewById(R.id.list_school);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, nameList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent n = new Intent(MainActivity.this, Students.class);
                startActivity(n);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                MainActivity.this.arrayAdapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                MainActivity.this.arrayAdapter.getFilter().filter(s);
                return false;
            }
        });

    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // To be implemented in a later section.
        }
    };



    private void query_purchase(){

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {

                        try{

                            billingClient.queryPurchasesAsync(
                                    QueryPurchasesParams.newBuilder()
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build(),
                                    (billingResult1, purchaseList) -> {

                                        for (Purchase purchase: purchaseList){

                                            if (purchase != null && purchase.isAcknowledged()){

                                                isPremium = true;

                                            }

                                        }


                                    }


                            );

                        } catch (Exception e){

                            isPremium = false;

                        }

                        runOnUiThread(() -> {

                            try{
                                Thread.sleep(1000);

                            } catch (InterruptedException e){

                                e.printStackTrace();

                            }

                            if (isPremium) {

                                ConnectionClass.premium = true;
                                ConnectionClass.locked = false;

                            } else {

                                ConnectionClass.premium = false;
                                //Intent i = new Intent(MainActivity.this, Subs.class);
                                //startActivity(i);

                            }

                        });

                    });

                }

            }
        });


    }


}