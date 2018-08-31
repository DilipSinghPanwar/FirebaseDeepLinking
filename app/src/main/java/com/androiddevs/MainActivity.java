package com.androiddevs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.net.URL;
import java.net.URLDecoder;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    String TAG = MainActivity.class.getSimpleName();
    TextView tvLink;
    Uri shortLink;
    private int INVITE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLink = (TextView) findViewById(R.id.tvLink);
        parseDeepLink();
    }

    private void parseDeepLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        if (deepLink != null) {
                            tvLink.setText(deepLink.toString());
                            if (deepLink != null) {
                                Set<String> params = deepLink.getQueryParameterNames();
                                if (params.contains("status")) {
                                    Intent intent = new Intent(getApplicationContext(), SuccessActivity.class);
                                    intent.putExtra("key", deepLink.toString());
                                    startActivity(intent);
                                }
//                                contentText.setText(deepLink.toString());
                                Log.e(TAG, "onSuccess: >>" + deepLink.toString());
                            } else {
                                Log.e(TAG, "getDynamicLink: no link found>>");
                            }

                        } else {
                            Log.e(TAG, "getDynamicLink: no link found>>");
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "getDynamicLink:onFailure>>");
                    }
                });
    }

    public void buildLink(View v) {
        Uri uri = getDynamicLink(Uri.parse("http://en.proft.me/?status=100"), 0);
        Task<ShortDynamicLink> task = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLongLink(uri)
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            shortLink = task.getResult().getShortLink();
                            Log.d(TAG, "onComplete: " + shortLink.toString());
                            tvLink.setText(shortLink.toString());
                        } else {
                            tvLink.setText("Error retrieving link");
                        }
                    }
                });
    }

    public void shareLink(View v) {
        try {
            URL url = new URL(URLDecoder.decode(shortLink.toString(), "UTF-8"));
            Log.i(TAG, "URL = " + url.toString());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link");
            intent.putExtra(Intent.EXTRA_TEXT, url.toString());
            startActivityForResult(intent, INVITE_REQUEST);
        } catch (Exception e) {
            Log.i(TAG, "Could not decode Uri: " + e.getLocalizedMessage());
        }
    }

    private Uri getDynamicLink(@NonNull Uri destinationLink, int minVersion) {
        String linkdomain = "q4z78.app.goo.gl";
        DynamicLink.Builder builder = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setDynamicLinkDomain(linkdomain)
                .setIosParameters(new DynamicLink.IosParameters.Builder("com.meetntrain").build())
//                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
//                        .setTitle(dataModel.activity_title + "\n")
//                        .setDescription(mStartDate + " at " + mStartTime + "\n" + address + "\nPrice: " + dataModel.price + " AED" + "\n" + description)
////                        .setDescription(mStartDate + " at " + mStartTime + "\n" + address + "\nPrice: " + dataModel.price + " AED" + "\n" + description)
//                        .setImageUrl(Uri.parse(imagrUrl))
//                        .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(minVersion)
                        .build())
                .setLink(destinationLink);
        DynamicLink link = builder.buildDynamicLink();
        return link.getUri();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INVITE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.e(TAG, "id of sent invitation: >>" + id);
                }
            } else {
                // Failed to send invitations
            }
        }
    }
}