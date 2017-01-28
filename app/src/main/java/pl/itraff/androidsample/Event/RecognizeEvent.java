package pl.itraff.androidsample.Event;

import java.net.URL;

public class RecognizeEvent {
    protected byte[] image;
    protected String clientKey;
    protected URL url;

    public RecognizeEvent(byte[] image, String clientKey, URL url) {
        this.image = image;
        this.clientKey = clientKey;
        this.url = url;
    }

    public byte[] getImage() {
        return image;
    }

    public String getClientKey() {
        return clientKey;
    }

    public URL getUrl() {
        return url;
    }
}
