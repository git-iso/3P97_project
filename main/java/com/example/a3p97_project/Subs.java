package com.example.a3p97_project;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

public class Subs extends AppCompatActivity {

    private BillingClient billingClient;

    String sub_name, duration, phases, destination;
    boolean valid = false;
    ActivitySubsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySubsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        getPrice();

        if (ConnectionClass.premium){

            binding.txtSubstatus.setText("Already Subscribed");
            binding.btnSub.setVisibility(View.GONE);

        } else {

            binding.txtSubstatus.setText("Not Subscribed");

        }

    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);

                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

                binding.txtSubstatus.setText("Already Subscribed");
                valid = true;
                ConnectionClass.premium = true;
                ConnectionClass.locked = false;
                binding.btnSub.setVisibility(View.GONE);

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {

                binding.txtSubstatus.setText("FEATURE_NOT_SUPPORTED");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {

                binding.txtSubstatus.setText("BILLING_UNAVAILABLE");
/*
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {

                binding.txtSubstatus.setText("USER_CANCELLED");
*/
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {

                binding.txtSubstatus.setText("DEVELOPER_ERROR");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {

                binding.txtSubstatus.setText("ITEM_UNAVAILABLE");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {

                binding.txtSubstatus.setText("NETWORK_ERROR");

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {

                binding.txtSubstatus.setText("SERVICE_DISCONNECTED");

            } else {

                Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT);

            }
        }
    };

    void handlePurchase(final Purchase purchase) {

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = (billingResult, s) -> {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                // Handle the success of the consume operation.
            }

        };

        billingClient.consumeAsync(consumeParams, listener);

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){

            if (!verifyValidSig(purchase.getOriginalJson(), purchase.getSignature())){

                Toast.makeText(getApplicationContext(), "Error: Invalid Purchase", Toast.LENGTH_SHORT).show();
                return;

            }

            if (!purchase.isAcknowledged()){

                AcknowledgePurchaseParams ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                billingClient.acknowledgePurchase(ackParams, ackListener);
                binding.txtSubstatus.setText("Subscribed");
                valid = true;

            } else {

                binding.txtSubstatus.setText("Already Subscribed");

            }

            ConnectionClass.premium = true;
            ConnectionClass.locked = false;
            binding.btnSub.setVisibility(View.GONE);

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING){

            binding.txtSubstatus.setText("Subscription Pending");

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE){

            binding.txtSubstatus.setText("UNSPECIFIED_STATE");

        }

    }


    AcknowledgePurchaseResponseListener ackListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){

                binding.txtSubstatus.setText("Subscribed");
                valid = true;
                ConnectionClass.premium = true;
                ConnectionClass.locked = false;

            }

        }
    };

    private boolean verifyValidSig(String data, String sig){

        try {

            String b64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA03v8kebQN6xFiTfnrwzQlpcF/pgrXGSaRG7v1+xLUh6znsp0GRzAk0JuPa2DzxMnHuAI0YNPP+E2FWALWPegx2nbcwwZ82aa0CdcZCQuk3XZUsJzXYUE9/wKPGjxp++rK3YCNdjrsvtJXcFsAQYygLuKA4ASNpmdJi/OHM2qfpIYIDdIv+fW7PP4QNPTa3ELJTUk7w9J5ztdO60R4pYjI7GwBIvq47TGHAgqa5/JV8I5jbAXS+ScqN0dcFPOy6DA5I196kQtD2Qm/RuFSulLtMfXTAHmOHL0ktpHJZOIvy6kEZ7lkV8q67PGMk+pukVJvXmxirFght/WmRjwqBNbtwIDAQAB";
            return Security.verifyPurchase(b64, data, sig);

        } catch (IOException e){

            return false;

        }

    }

    private void getPrice() {

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    ExecutorService e = Executors.newSingleThreadExecutor();
                    e.execute(new Runnable() {
                        @Override
                        public void run() {

                            QueryProductDetailsParams queryProductDetailsParams =
                                    QueryProductDetailsParams.newBuilder()
                                            .setProductList(
                                                    ImmutableList.of(
                                                            QueryProductDetailsParams.Product.newBuilder()
                                                                    .setProductId("month")
                                                                    .setProductType(BillingClient.ProductType.SUBS)
                                                                    .build()))
                                            .build();

                            billingClient.queryProductDetailsAsync(
                                    queryProductDetailsParams,
                                    new ProductDetailsResponseListener() {
                                        public void onProductDetailsResponse(BillingResult billingResult,
                                                                             List<ProductDetails> productDetailsList) {

                                            for (ProductDetails productDetails : productDetailsList) {

                                                String offer = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                                                ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                                                        ImmutableList.of(
                                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                                        .setProductDetails(productDetails)
                                                                        // For one-time products, "setOfferToken" method shouldn't be called.
                                                                        // For subscriptions, to get an offer token, call
                                                                        // ProductDetails.subscriptionOfferDetails() for a list of offers
                                                                        // that are available to the user.
                                                                        .setOfferToken(offer)
                                                                        .build()
                                                        );
                                                sub_name = productDetails.getName();

                                                String formatPrice = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                                String billingPeriod = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getBillingPeriod();
                                                int recur = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getRecurrenceMode();

                                                String n, duration, bp;

                                                bp = billingPeriod;
                                                n = billingPeriod.substring(1, 2);
                                                duration = billingPeriod.substring(2, 3);

                                                if (recur == 2) {

                                                    duration = " For " + n + "Month";

                                                } else if (duration.equals("Y")) {

                                                    duration = " For " + n + "Year";

                                                } else if (duration.equals("W")) {

                                                    duration = " For " + n + "Week";

                                                } else if (duration.equals("D")) {

                                                    duration = " For " + n + "Days";

                                                } else {

                                                    if (bp.equals("P1M")) {

                                                        duration = "/Monthly";

                                                    } else if (bp.equals("P1Y")) {

                                                        duration = "/Yearly";

                                                    } else if (bp.equals("P1W")) {

                                                        duration = "/Weekly";

                                                    } else if (bp.equals("P3W")) {

                                                        duration = "Every /3 Week";
                                                    }

                                                    phases = formatPrice + " " + duration;
                                                    for (int i = 0; i <= (productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size()); i++) {

                                                        if (i > 0) {

                                                            String period = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(i).getBillingPeriod();
                                                            String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(i).getFormattedPrice();

                                                            if (period.equals("P1M")) {

                                                                duration = "/Monthly";

                                                            } else if (period.equals("P1Y")) {

                                                                duration = "/Yearly";

                                                            } else if (period.equals("P1W")) {

                                                                duration = "/Weekly";

                                                            } else if (period.equals("P3W")) {

                                                                duration = "Every /3 Week";
                                                            }

                                                            phases += "\n" + price + duration;

                                                        }


                                                    }
                                                }

                                            }
                                        }
                                    }
                            );


                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {

                                e.printStackTrace();
                            }

                            binding.txtSubid.setText(sub_name);
                            binding.txtPrice.setText("Price: " + phases);
                            //binding.txtBenefit.setText(destination);

                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });


    }

    public void btn_item1_click(View v){

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                QueryProductDetailsParams queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder()
                                .setProductList(
                                        ImmutableList.of(
                                                QueryProductDetailsParams.Product.newBuilder()
                                                        .setProductId("item1")
                                                        .setProductType(BillingClient.ProductType.INAPP)
                                                        .build()))
                                .build();

                billingClient.queryProductDetailsAsync(
                        queryProductDetailsParams,
                        new ProductDetailsResponseListener() {
                            public void onProductDetailsResponse(BillingResult billingResult,
                                                                 List<ProductDetails> productDetailsList) {

                                for (ProductDetails productDetails : productDetailsList) {

                                    String offer = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                                    ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                                            ImmutableList.of(
                                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                            .setProductDetails(productDetails)
                                                            // For one-time products, "setOfferToken" method shouldn't be called.
                                                            // For subscriptions, to get an offer token, call
                                                            // ProductDetails.subscriptionOfferDetails() for a list of offers
                                                            // that are available to the user.
                                                            //.setOfferToken(offer)
                                                            .build()
                                            );
                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                            .setProductDetailsParamsList(productDetailsParamsList)
                                            .build();

// Launch the billing flow
                                    billingClient.launchBillingFlow(Subs.this, billingFlowParams);


                                }
                            }
                        });

            }

        });


    }

    public void btn_sub_click(View v) {

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                QueryProductDetailsParams queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder()
                                .setProductList(
                                        ImmutableList.of(
                                                QueryProductDetailsParams.Product.newBuilder()
                                                        .setProductId("month")
                                                        .setProductType(BillingClient.ProductType.SUBS)
                                                        .build()))
                                .build();

                billingClient.queryProductDetailsAsync(
                        queryProductDetailsParams,
                        new ProductDetailsResponseListener() {
                            public void onProductDetailsResponse(BillingResult billingResult,
                                                                 List<ProductDetails> productDetailsList) {

                                for (ProductDetails productDetails : productDetailsList) {

                                    String offer = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                                    ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                                            ImmutableList.of(
                                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                            .setProductDetails(productDetails)
                                                            // For one-time products, "setOfferToken" method shouldn't be called.
                                                            // For subscriptions, to get an offer token, call
                                                            // ProductDetails.subscriptionOfferDetails() for a list of offers
                                                            // that are available to the user.
                                                            .setOfferToken(offer)
                                                            .build()
                                            );
                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                            .setProductDetailsParamsList(productDetailsParamsList)
                                            .build();

// Launch the billing flow
                                    billingClient.launchBillingFlow(Subs.this, billingFlowParams);


                                }
                            }
                        });

            }

        });

    }

    @Override
    protected void onDestroy(){

        super.onDestroy();

        if (billingClient != null){

            billingClient.endConnection();

        }

    }

    public void btn_story_click(View v){

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

}

