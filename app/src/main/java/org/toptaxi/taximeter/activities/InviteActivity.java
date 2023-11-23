package org.toptaxi.taximeter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

public class InviteActivity extends AppCompatActivity {
    String inviteType;
    String inviteCaption;
    String inviteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            inviteType = arguments.getString("type", "driver");
        }
        else {
            inviteType = "driver";
        }
        if (inviteType.equals("driver")){
            inviteCaption = MainApplication.getInstance().getPreferences().getDriverInviteCaption();
            inviteText = MainApplication.getInstance().getPreferences().getDriverInviteText();
        } else if (inviteType.equals("client")) {
            inviteCaption = MainApplication.getInstance().getPreferences().getClientInviteCaption();
            inviteText = MainApplication.getInstance().getPreferences().getClientInviteText();
        }
        setContentView(R.layout.activity_invite);
        ((TextView)findViewById(R.id.tvInviteCaption)).setText(inviteCaption);
    }

    public void btnInviteClick(View view){
        MainApplication.getInstance().getRestService().httpGetThread("/invite/click");
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, inviteText);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }



}
