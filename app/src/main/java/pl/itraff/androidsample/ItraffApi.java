package pl.itraff.androidsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import pl.itraff.androidsample.Event.FailureEvent;
import pl.itraff.androidsample.Event.RecognizeEvent;
import pl.itraff.androidsample.Event.SuccessEvent;

/**
 * Created by Tomasz Szymański on 2016-07-11.
 */
public class ItraffApi {

    public static final String API_URL = "http://recognize.im/v2/recognize/";
    public static final String[] MODES = {"single/", "multi/", "shelf/"};
    public static final String[] FILTER = {"all/", ""};
    public static final int[][] SIZES = {{240, 360, 480}, {640, 1280, 1920}, {}};
    public static final String ENCODING = "UTF-8";
    public static final String REQUEST_METHOD = "POST";
    public static final String HEADER_ITRAFF = "x-itraff-hash";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_APPLICATION_JSON = "application/json";

    protected String clientId;
    protected String clientKey;
    protected int mode;
    protected int filter;
    protected String response;

    /**
     * ItraffApi public constructor
     * @param clientId - client's API id
     * @param clientKey - client's API key
     * @param mode - chosen mode of recognition
     * @param filter - chosen results filter
     */
    public ItraffApi(String clientId, String clientKey, int mode, int filter) {
        this.clientId = clientId;
        this.clientKey = clientKey;
        this.mode = mode;
        this.filter = filter;
        EventBus.getDefault().register(this);
    }

    public ItraffApi setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ItraffApi setClientKey(String clientKey) {
        this.clientKey = clientKey;
        return this;
    }

    public ItraffApi setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public ItraffApi setFilter(int filter) {
        this.filter = filter;
        return this;
    }

    public URL getRequestUrl() throws MalformedURLException {
        URL url = new URL(API_URL + MODES[mode] + FILTER[filter] + clientId);
        Log.i("iTraff url", url.getPath());
        return url;
    }

    /**
     * Generates MD5 from client api key and image bytes
     * @param clientKey - client api key
     * @param image - byte[] image
     * @return md5 hash
     */
    public static String getMD5FromKeyAndImage(String clientKey, byte[] image) {
        String hash;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.reset();
            md.update(clientKey.getBytes(ENCODING));
            md.update(image);
            byte[] array = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            hash = sb.toString();
        } catch (Exception e) {
            hash = null;
        }
        return hash;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onRecognize(RecognizeEvent event) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) event.getUrl().openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setRequestProperty("Content-Type", "image/jpeg");
        String hash = ItraffApi.getMD5FromKeyAndImage(event.getClientKey(), event.getImage());
        connection.setRequestProperty(HEADER_ITRAFF, hash);
        connection.setRequestProperty(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
        OutputStream out = null;
        InputStream in = null;
        try {
            // Prepare request headers and connection mode
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setChunkedStreamingMode(0);

            // Do the request
            out = new BufferedOutputStream(connection.getOutputStream());
            out.write(event.getImage());

            // Read the response
            in = new BufferedInputStream(connection.getInputStream());
            StringBuffer stringBuffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, ENCODING));
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                stringBuffer.append(inputLine);
            }
            response = stringBuffer.toString();
            Log.i("iTraff API response:", response);
            JSONObject jsonResponse = new JSONObject(response);
            if (!jsonResponse.getString("status").equals("0")) {
                // Failure
                EventBus.getDefault().post(new FailureEvent(jsonResponse.getString("message")));
            } else {
                // Success
                EventBus.getDefault().post(new SuccessEvent(jsonResponse.getJSONArray("objects")));
            }
        } catch (IOException e) {
            EventBus.getDefault().post(new FailureEvent(e.getMessage()));
            e.printStackTrace();
        } catch (JSONException e) {
            EventBus.getDefault().post(new FailureEvent(e.getMessage()));
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connection.disconnect();
        }
    }

    public Bitmap drawBoundingBoxes(JSONArray jsonObjects, String filePath, int sampleSize) {
        Bitmap bitmap = getScaledImage(filePath, sampleSize);
        Bitmap drawableBitmap = drawObjectsOnBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), jsonObjects);
        return drawableBitmap;
    }

    public String getFormattedRecognitionData(JSONArray jsonObjects) throws JSONException {
        int l = jsonObjects.length();
        HashMap<String, Integer> matches = new HashMap<>();
        HashMap<String, String> names = new HashMap<>();

        // Aggregate by match count
        for (int i = 0; i < l; i++) {
            JSONObject obj = jsonObjects.getJSONObject(i);
            String id = obj.getString("id");
            if (matches.get(id) != null) {
                matches.put(id, matches.get(id) + 1);
            } else {
                matches.put(id, 1);
                names.put(id, obj.getString("name"));
            }
        }

        // Format the aggregated output
        String out = "Recognized objects: " + String.valueOf(l) + "\n";
        for (String id : matches.keySet()) {
            out = out.concat(id).concat(": ").concat(String.valueOf(matches.get(id))).concat("\n");
        }
        return out;
    }

    protected Bitmap getScaledImage(String filePath, int sampleSize) {
        if (sampleSize == 1) {
            return BitmapFactory.decodeFile(filePath);
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, bmOptions);
    }

    protected Bitmap drawObjectsOnBitmap(Bitmap bitmap, JSONArray jsonObjects) {
        Canvas canvas = new Canvas(bitmap);
        Paint red = new Paint();
        red.setStrokeWidth(3f);
        red.setColor(Color.GREEN);
        try {
            int l = jsonObjects.length();
            for (int i = 0; i < l; i++) {
                JSONObject jsonObject = jsonObjects.getJSONObject(i);
                JSONArray coords = jsonObject.getJSONArray("location");
                int k = coords.length();
                for (int j = 0; j < k; j++) {
                    JSONObject coord = coords.getJSONObject(j);
                    JSONObject nextCoord = coords.getJSONObject(j + 1 == k ? 0 : j + 1);
                    canvas.drawLine(coord.getInt("x"), coord.getInt("y"), nextCoord.getInt("x"), nextCoord.getInt("y"), red);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
