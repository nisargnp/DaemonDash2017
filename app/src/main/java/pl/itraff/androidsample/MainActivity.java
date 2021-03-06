package pl.itraff.androidsample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.*;
import pl.itraff.androidsample.DaemonDashCrawler.InfoGetter;
import pl.itraff.androidsample.Event.FailureEvent;
import pl.itraff.androidsample.Event.SuccessEvent;


public class MainActivity extends AppCompatActivity {

    final String[] BRANDS = {
            "Mcdonald",
            "Chik-fil-A",
            "Starbucks",
            "Taco Bell",
            "Chipotle",
            "Nike",
            "Staple",
            "Target",
            "Wendy",
            "Pizza Hut",
            "Best Buy",
            "Book",
            "Fact",
            "FactSet",
            "Google",
            "Cipher",
            "Lockheed Martin",
            "Capital One",
            "National Security Agency",
            "Hamilton",
            "Dark"
    };

    public static final int REQUEST_TAKE_PHOTO = 1337;
    public static final int MY_PERMISSIONS_REQUEST = 666;
    public static final String PREF_ID = "USER_ID";
    public static final String PREF_KEY = "USER_KEY";
    public static final String PREF_MODE = "USER_MODE";
    public static final String PREF_SIZE = "USER_SIZE";
    protected static final String FILE_PROVIDER_NAME = "pl.itraff.fileprovider";
    public static DBHandler database;

    File imageFile = null;
    Button btnViewResult;
    Button btnTakePic;
    TextView textViewResult;
    ProgressBar progressBar;
    int sampleSize;
    ItraffApi iTraffApi;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    protected void permissionCheck() {
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
            }, MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    permissionCheck();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_action_main);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionCheck();
        btnTakePic = (Button)findViewById(R.id.btn_take_pic);
        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    String[] modeNames = getResources().getStringArray(R.array.array_modes);
                    String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + modeNames[0] + ".jpeg";
                    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "pl.itraff.androidsample/");
                    dir.mkdirs();
                    imageFile = new File(dir, fileName);
                    Uri photoUri = FileProvider.getUriForFile(MainActivity.this, FILE_PROVIDER_NAME, imageFile);
                    grantUriPermission("pl.itraff.androidsample", photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        });

        // Assign form inputs, buttons and actions
        textViewResult = (TextView) findViewById(R.id.txt_result);
        textViewResult.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

//        btnViewResult = (Button) findViewById(R.id.btn_view_result);
//        btnViewResult.setVisibility(View.GONE);
//        btnViewResult.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Uri resultImageUri = FileProvider.getUriForFile(MainActivity.this, FILE_PROVIDER_NAME, imageFile);
//                grantUriPermission("pl.itraff.androidsample", resultImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setDataAndType(resultImageUri, "image/jpeg");
//                startActivity(intent);
//            }
//        });

        // Updates form fields with data from SharedPreferences
        readSettings();

        // Assigns onClickListeners to hint buttons
        assignHintActions();

        AssetManager am = getApplicationContext().getAssets();

        database = new DBHandler(this);

        try {
            File file = File.createTempFile("temp", ".xls");
            file.deleteOnExit();
            InputStream is = am.open("data.xls");
            Log.d("qwerty", "OPENED!!!");
            OutputStream outputStream = new FileOutputStream(file);
            if (outputStream == null)
                Log.d("adfasdf", "outputStream is null");
            IOUtils.copy(is, outputStream);
            Log.d("qwerty", "WROTE TO FILE!!!");
            Log.d("log file", file.toString());
            Parser.read(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("fdsa", "Can't open the stream");
        }

        Log.d("Hello!", "Bye!");
    }

    /**
     * Handles "?" buttons and Toast messages displayed when clicked
     */
    protected void assignHintActions() {
        View.OnClickListener oclCredentials = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, getString(R.string.hint_credentials), Toast.LENGTH_LONG).show();
            }
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestFailure(FailureEvent event) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestSuccess(SuccessEvent event) {
        progressBar.setVisibility(View.GONE);

        // Draw bounding boxes on picture
        Bitmap resultBitmap = iTraffApi.drawBoundingBoxes(event.getObjects(), imageFile.getAbsolutePath(), sampleSize);

        // Save new picture to the External Storage
        try {
            saveBitmapToExternalStorage(resultBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Display "view result" button
        btnViewResult.setVisibility(View.VISIBLE);

        // Display string-formatted recognition data
        try {
            textViewResult.setVisibility(View.VISIBLE);
            textViewResult.setText(iTraffApi.getFormattedRecognitionData(event.getObjects()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves given image to ExternalStorage
     * @param bitmap - image to be saved
     * @throws IOException
     */
    protected void saveBitmapToExternalStorage(Bitmap bitmap) throws IOException {
        FileOutputStream out = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.getFD().sync();
        out.close();
        bitmap.recycle();
    }

    /**
     * Populates given Spinner object found by spinnerViewId resource id, populates it with data
     * from spinnerData resource id and returns the Spinner instance
     * @param spinnerViewId - resource id of the spinner to be populated with data
     * @param spinnerData - resource id of the data to populate the spinner with
     * @return Spinner
     */
    protected Spinner populateSpinner(int spinnerViewId, int spinnerData) {
        Spinner spinner = (Spinner) findViewById(spinnerViewId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, spinnerData, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            final Intent intent = new Intent(MainActivity.this, SecondActivity.class);

            new Thread() {
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnTakePic.setEnabled(false);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });

                    try {

                        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
                        File file = new File(imageFile.getAbsolutePath());
                        RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image_request[locale]", "en-US")
                                .addFormDataPart("image_request[image]","user.jpg", RequestBody.create(MEDIA_TYPE_JPG, file)).build();

                        Request request = new Request.Builder()
                                .url("http://api.cloudsightapi.com/image_requests")
                                .post(req)
                                .header("Authorization", "CloudSight YSYo6GdXOixs_fAvGaAkTw")
                                .build();

                        Log.d("status: ", "0");

                        OkHttpClient client = new OkHttpClient();
                        Response response = client.newCall(request).execute();
                        String resp = new String(response.body().bytes());
                        String token = resp.split("\"")[3];

                        Log.d("status: ", "1");

                        while(resp.contains("not completed")){
                            request = new Request.Builder()
                                    .url("https://api.cloudsightapi.com/image_responses/" + token)
                                    .header("Authorization", "CloudSight YSYo6GdXOixs_fAvGaAkTw")
                                    .build();
                            resp = new String(client.newCall(request).execute().body().bytes());
                            Thread.sleep(100);
                        }
                        String product = resp.split("\"")[17];

                        Log.d("status: ", "2 -- " + product);

                        String company = null;
                        for (String brand : BRANDS) {
                            if(product.toLowerCase().contains(brand.toLowerCase())) {
                                company = brand;
                                break;
                            }
                        }

                        if (company != null && company.toLowerCase().equals("Fact".toLowerCase()))
                            company = "FactSet";
                        if (company != null && company.toLowerCase().equals("Nike".toLowerCase()))
                            company = "Nike, Inc";
                        if (company != null && company.toLowerCase().equals("Mcdonald".toLowerCase()))
                            company = "Mcdonald's";
                        if (company != null && company.toLowerCase().equals("Chipotle".toLowerCase()))
                            company = "Chipotle Mexican Grill";
                        if (company != null && company.toLowerCase().equals("Staple".toLowerCase()))
                            company = "Staples Inc";
                        if (company != null && company.toLowerCase().equals("Target".toLowerCase()))
                            company = "Target Corporation";
                        if (company != null && company.toLowerCase().contains("Agency".toLowerCase()))
                            company = "National Security Agency";
                        if (company != null && company.toLowerCase().contains("Hamilton".toLowerCase()))
                            company = "Booz Allen Hamilton";

                        if (company == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Please Try Again.", Toast.LENGTH_LONG).show();
                                    btnTakePic.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                            return;
                        }

//                        Log.d("status: ", "3 -- " + database.getShopsCount());

                        ArrayList<String> products = new ArrayList<String>();
                        ArrayList<String> reviews = new ArrayList<String>();

                        if (company != null && company.toLowerCase().equals("Nike, Inc".toLowerCase()))
                            company = "Nike";
                        if (company != null && company.toLowerCase().equals("Mcdonald's".toLowerCase()))
                            company = "Mcdonald";
                        if (company != null && company.toLowerCase().equals("Chipotle Mexican Grill".toLowerCase()))
                            company = "Chipotle";
                        if (company != null && company.toLowerCase().equals("Staples Inc".toLowerCase()))
                            company = "Staple";
                        if (company != null && company.toLowerCase().equals("Target Corporation".toLowerCase()))
                            company = "Target";
                        if (company != null && company.toLowerCase().equals("National Security Agency".toLowerCase()))
                            company = "NSA";
                        if (company != null && company.toLowerCase().contains("Booz Allen Hamilton".toLowerCase()))
                            company = "Hamilton";

                        for (Shop s : database.getAllShops())
                        {
                            try {
                                s.genHashMaps();
                                Log.d("passed hash maps:", "yes");

                                Log.d(company, s.getName());

                                if (s.getName().toLowerCase().contains(company.toLowerCase())) {
                                    Log.d("", s.toString());
                                    products = s.getProductArray();
                                    reviews = s.getReviewArray();
                                    break;
                                }
                            } catch (Exception exe) {
                                exe.printStackTrace();
                            }
                        }

                        if (company != null && company.toLowerCase().equals("Nike".toLowerCase()))
                            company = "Nike, Inc";
                        if (company != null && company.toLowerCase().equals("Mcdonald".toLowerCase()))
                            company = "Mcdonald's";
                        if (company != null && company.toLowerCase().equals("Chipotle".toLowerCase()))
                            company = "Chipotle Mexican Grill";
                        if (company != null && company.toLowerCase().equals("Staple".toLowerCase()))
                            company = "Staples Inc";
                        if (company != null && company.toLowerCase().equals("Target Corporation".toLowerCase()))
                            company = "Target";
                        if (company != null && company.toLowerCase().equals("NSA".toLowerCase()))
                            company = "National Security Agency";
                        if (company != null && company.toLowerCase().contains("Hamilton".toLowerCase()))
                            company = "Booz Allen Hamilton";

                        Log.d("RESP", "MADE IT 1");

                        InfoGetter inf = new InfoGetter(company);

                        Log.d ("RESP", "MADE IT 2");

                        if (company.toLowerCase().contains("book"))
                            company = "BookHolders";
                        if (company.toLowerCase().contains("cipher"))
                            company = "Cipher Tech Solutions";

                        Bundle b = new Bundle();
                        b.putString("COMPANY", company);
                        if (!company.toLowerCase().contains("book") && !company.toLowerCase().contains("cipher"))
                            b.putString("SUMMARY", inf.getSummary());
                        else if (company.toLowerCase().contains("book"))
                            b.putString("SUMMARY", "Most students find as they go through college " +
                                    "they get ripped off more and more for outrageous textbook " +
                                    "prices. To top it off when they try to sell back that " +
                                    "textbook they get literally pennies. We realized that local " +
                                    "and global textbook stores have no solution to this " +
                                    "growing problem so BookHolders.com was formed to help " +
                                    "students buy and sell their books.\n");
                        else if (company.toLowerCase().contains("cipher"))
                            b.putString("SUMMARY", "Cipher Tech Solutions, Inc. (Cipher Tech) was" +
                                    " founded in 2006 by two engineering students from" +
                                    " Northeastern University who were passionate about national" +
                                    " security, software development, and entrepreneurship." +
                                    " Alongside classwork, Cipher Tech began to bridge the gap" +
                                    " between the nation’s best academics and students, and the" +
                                    " defense community. Just a few years later, in true “David" +
                                    " and Goliath” fashion, Cipher Tech became known as a fierce" +
                                    " competitor by defense and intelligence firms many times" +
                                    " our size.");
                        b.putStringArrayList("ARTICLES", inf.getArticleURLs());
                        b.putStringArrayList("STOCKS", inf.getStockQuote());
                        b.putStringArrayList("PRODUCTS", products);
                        b.putStringArrayList("REVIEWS", reviews);
                        intent.putExtras(b); //Put your id to your next Intent

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTakePic.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                        startActivity(intent);

                        Log.d("status: ", "5");

                        // finish();

                    } catch(Exception e){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTakePic.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                        System.out.println(e.getStackTrace().toString());
                    }
                }
            }.start();
        }
    }

    /**
     * Loads scaled down image to the memory to prevent our of memory exception
     * @param photoPath - absolute path to the image resource
     * @param targetLongEdge - dimension of the longer edge that the image should be rescaled to
     * @return
     */
    protected Bitmap getDownscaledBitmap(String photoPath, int targetLongEdge) {

        // Get current dimensions of the Bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine the scale multiplier for the image
        sampleSize = Math.max(photoW, photoH) / targetLongEdge;

        // Decode the image file into a downscaled Bitmap
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    /**
     * Converts bitmap to byte array
     * @param bitmap - bitmap to be converted
     * @return JPEG compressed byte array
     */
    protected byte[] bitmap2byteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Reads data from private SharedPreferences storage if available and assigns values to form inputs
     */
    protected void readSettings() {
    }

    /**
     * Reads data from inputs and saves it to private SharedPreferences storage
     */
    protected void saveSettings() {
    }

}
