package com.example.a3p97_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        query_purchase();

    }

    public void btnClick(View v){

        Intent i = new Intent(MainActivity.this, Students.class);
        startActivity(i);

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