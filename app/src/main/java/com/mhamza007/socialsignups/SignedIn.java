package com.mhamza007.socialsignups;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class SignedIn extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signed_in);

        String username = getIntent().getStringExtra("username");

        TextView textView = findViewById(R.id.text);
        textView.setText(username);
    }
}
