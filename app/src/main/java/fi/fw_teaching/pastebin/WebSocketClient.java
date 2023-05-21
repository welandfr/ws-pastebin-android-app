package fi.fw_teaching.pastebin;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient extends WebSocketListener {

    // Callback Interface
    public interface WebSocketListenerCallback {
        void onMessageReceived(String message);
        void onConnectionOpened();
        void onConnectionClosed();
    }

    private WebSocketListenerCallback callback;
    private WebSocket webSocket;
    private String url;

    public WebSocketClient(String url) {
        this.url = url;
    }

    public void setWebSocketListenerCallback(WebSocketListenerCallback callback) {
        this.callback = callback;
    }

    public void connectWebSocket() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[] {};
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            webSocket = client.newWebSocket(request, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String json) {
        webSocket.send(json);
    }

    @Override
    public void onOpen(WebSocket webSocket, okhttp3.Response response) {
        // WebSocket connection opened
        // Perform necessary actions after successful connection
        System.out.println("WebSocket CONNECTION OPENED");
        System.out.println(response);
        if (callback != null) callback.onConnectionOpened();
    }

    @Override
    public void onMessage(WebSocket webSocket, String json) {
        // Handle incoming messages from the WebSocket server
        System.out.println("WebSocket MESSAGE: " + json);
        if (callback != null) callback.onMessageReceived(json);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        // WebSocket connection closed
        System.out.println("WebSocket CONNECTION CLOSED: " + reason);
        if (callback != null) callback.onConnectionClosed();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
        // Handle WebSocket connection failure
        System.out.println("WebSocket FAILURE: " + response);
    }
}
