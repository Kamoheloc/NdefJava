package com.mydomain.ndefjava;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    //Variables
    TextView tagInfo;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Tag myTag;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        tagInfo = findViewById(R.id.tagInfo);// write tag reads into textView


        //check whether adapter is available
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
        {
            Toast.makeText(this, "This device doesn't support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }

        readFromIntent(getIntent());//method call
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);// get categories for tag
    }

    // Init read when onCreate is executed, reader is listening for tag
    private void readFromIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
        || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
        || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
        )
        {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length ; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    //Get data, analyse it and display it
    private void buildTagViews(NdefMessage[] msgs)
    {

        if (msgs == null || msgs.length == 0) return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0 ) ?"UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

         try {
             text = new String(payload, languageCodeLength+1, payload.length - languageCodeLength - 1, textEncoding);
         }
         catch (Exception e)
         {
             Log.e("UnsupportedEncoding", e.toString());
         }
         tagInfo.setText("Content: "+text);
    }

    //Read from intent if newIntent is called
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }
}