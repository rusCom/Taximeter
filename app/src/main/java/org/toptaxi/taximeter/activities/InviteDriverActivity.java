package org.toptaxi.taximeter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

public class InviteDriverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_driver);
        ((TextView)findViewById(R.id.tvShareDriverInfo)).setText(MainApplication.getInstance().getPreferences().getDriverInviteCaption());
    }

    public void btnShareClick(View view){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, MainApplication.getInstance().getPreferences().getDriverInviteText());
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }



}
