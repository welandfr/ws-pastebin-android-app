package fi.fw_teaching.pastebin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity implements WebSocketClient.WebSocketListenerCallback {

    TextView pasteOutput, wsStatus;
    EditText pasteInput;
    Button btnReconnect;
    WebSocketClient webSocketClient;
    Gson gson = new Gson();
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pasteInput = findViewById(R.id.pasteInput);
        pasteOutput = findViewById(R.id.pasteOutput);
        wsStatus = findViewById(R.id.wsStatus);
        btnReconnect = findViewById(R.id.btnReconnect);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        onConnectionClosed();
        openWebSocket();

        btnReconnect.setEnabled(false);
    }

    public void openWebSocket() {
        // Gets URL from SharedPrefs (SettingsActivity)
        webSocketClient = new WebSocketClient(prefs.getString("url", null));
        webSocketClient.setWebSocketListenerCallback(this);
        webSocketClient.connectWebSocket();
    }

    public void handleSend(View view) {
        PastebinObj obj = new PastebinObj();
        obj.setVal(pasteInput.getText().toString());
        String json = gson.toJson(obj);
        webSocketClient.send(json);
        pasteInput.setText("");
    }

    public void handleReconnect(View view) {
        openWebSocket();
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    @Override
    public void onMessageReceived(String json) {
        System.out.println("CALLBACK onMessageReceived");
        PastebinObj obj = gson.fromJson(json, PastebinObj.class);
        pasteOutput.setText(obj.val);
    }

    @Override
    public void onConnectionOpened() {
        wsStatus.setText("Connection OPEN");
        btnReconnect.setEnabled(false);
    }

    @Override
    public void onConnectionClosed() {
        wsStatus.setText("Connection CLOSED");
        btnReconnect.setEnabled(true);
    }

}