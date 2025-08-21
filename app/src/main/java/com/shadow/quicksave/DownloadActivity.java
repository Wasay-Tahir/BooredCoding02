package com.shadow.quicksave;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.material.switchmaterial.SwitchMaterial;
import com.yausername.youtubedl_android.BuildConfig;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;

import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;


public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartDownload;
    private Button btnTestPython;
    private EditText etUrl;
    private SwitchMaterial useConfigFile;
    private ProgressBar progressBar;
    private TextView tvDownloadStatus;
    private TextView tvCommandOutput;
    private ProgressBar pbLoading;


    private boolean downloading = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    Function3<Float, Long, String, Unit> callback = new Function3<Float, Long, String, kotlin.Unit>() {
        @Override
        public Unit invoke(Float aFloat, Long aLong, String s) {
            int intValue = Math.round(aFloat); // Rounds to the nearest integer
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                      progressBar.setProgress(intValue);
                      tvDownloadStatus.setText(s);
                }
            });
            return null;
        }
    };

    private static final String TAG = DownloadActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloading_example);

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //change app bar title
        getSupportActionBar().setTitle("Download");

        initViews();
        initListeners();
        
        // Initialize YoutubeDL and check Python environment
        initYoutubeDL();
    }

    private void initYoutubeDL() {
        try {
            // Initialize YoutubeDL in background
            Disposable disposable = Observable.fromCallable(() -> {
                YoutubeDL.getInstance().init(this);
                return true;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(success -> {
                Log.d(TAG, "YoutubeDL initialized successfully");
                Toasty.success(this, "YoutubeDL ready", Toast.LENGTH_SHORT).show();
            }, e -> {
                Log.e(TAG, "Failed to initialize YoutubeDL", e);
                Toasty.error(this, "Failed to initialize YoutubeDL: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
            compositeDisposable.add(disposable);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing YoutubeDL", e);
            Toasty.error(this, "Error initializing YoutubeDL", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        btnStartDownload = findViewById(R.id.btn_start_download);
        btnTestPython = findViewById(R.id.btn_test_python);
        etUrl = findViewById(R.id.et_url);
        useConfigFile = findViewById(R.id.use_config_file);
        progressBar = findViewById(R.id.progress_bar);
        tvDownloadStatus = findViewById(R.id.tv_status);
        pbLoading = findViewById(R.id.pb_status);
        tvCommandOutput = findViewById(R.id.tv_command_output);
    }

    private void initListeners() {
        btnStartDownload.setOnClickListener(this);
        btnTestPython.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start_download) {
            if (!isStoragePermissionGranted()) {
                showStoragePermissionDialog();
            } else {
                startDownload();
            }
        } else if (v.getId() == R.id.btn_test_python) {
            testPythonEnvironment();
        }
    }

    private void startDownload() {
        Log.e("asdjasjdhajkdhjas","dasdhsajkdhjkashdjkas");
        if (downloading) {
            Toast.makeText(DownloadActivity.this, "cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            Toasty.error(DownloadActivity.this, "Cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isStoragePermissionGranted()) {
            Toasty.info(DownloadActivity.this, "Storage permission required. Please grant permission to continue.", Toast.LENGTH_LONG).show();
            showStoragePermissionDialog();
            return;
        }

        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            etUrl.setError(getString(R.string.url_error));
            return;
        }

        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        File config = new File(youtubeDLDir, "config.txt");

        if (useConfigFile.isChecked() && config.exists()) {
            request.addOption("--config-location", config.getAbsolutePath());
        } else {
            request.addOption("--no-mtime");
            request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
            request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        }

        showStart();

        downloading = true;
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, (String) null,  callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    pbLoading.setVisibility(View.GONE);
                    progressBar.setProgress(100);
                    tvDownloadStatus.setText(getString(R.string.download_complete));
                    tvCommandOutput.setText(youtubeDLResponse.getOut());
                    Toast.makeText(DownloadActivity.this, "download successful", Toast.LENGTH_LONG).show();
//                    Toasty.success(DownloadingExampleActivity.this, "Download successful", Toast.LENGTH_LONG).show();
                    downloading = false;
                }, e -> {
                    if (BuildConfig.DEBUG) Log.e(TAG, "failed to download", e);
                    pbLoading.setVisibility(View.GONE);
                    tvDownloadStatus.setText(getString(R.string.download_failed));
                    tvCommandOutput.setText(e.getMessage());

                    // Better error handling for Python-related issues
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains("Python")) {
                        if (errorMessage.contains("unsupported version") || errorMessage.contains("Python 3.9")) {
                            Toasty.error(DownloadActivity.this,
                                "Python version issue: This app requires Python 3.9+. Please update the app to the latest version.", 
                                Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("ImportError")) {
                            Toasty.error(DownloadActivity.this,
                                "Python module import error. Please check app installation or update the app.", 
                                Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("Python 3.8")) {
                            Toasty.error(DownloadActivity.this,
                                "Python 3.8 is not supported. Please update the app to get Python 3.9+ support.", 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toasty.error(DownloadActivity.this,
                                "Python environment error: " + errorMessage, 
                                Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toasty.error(DownloadActivity.this, "Download failed", Toast.LENGTH_LONG).show();
                    }
                    
                    downloading = false;
                });
        compositeDisposable.add(disposable);

    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    //get back on the home screen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    private File getDownloadLocation() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeDLDir = new File(downloadsDir, "youtubedl-android");
        
        // Check if we can access the downloads directory
        if (!downloadsDir.exists() || !downloadsDir.canWrite()) {
            // Fallback to app's external files directory
            File appDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (appDir != null) {
                youtubeDLDir = new File(appDir, "youtubedl-android");
            }
        }
        
        if (!youtubeDLDir.exists()) {
            boolean created = youtubeDLDir.mkdirs();
            if (!created) {
                // If we still can't create the directory, use app's internal files directory
                youtubeDLDir = new File(getFilesDir(), "downloads");
                youtubeDLDir.mkdirs();
            }
        }
        
        return youtubeDLDir;
    }

    private void showStart() {
        tvDownloadStatus.setText(getString(R.string.download_start));
        progressBar.setProgress(0);
        pbLoading.setVisibility(View.VISIBLE);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            boolean hasPermission = Environment.isExternalStorageManager();
            Log.d(TAG, "Android 11+ storage permission: " + hasPermission);
            return hasPermission;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6+ (API 23+) to Android 10 (API 29)
            boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Android 6-10 storage permission: " + hasPermission);
            return hasPermission;
        } else {
            // Android 5 and below
            Log.d(TAG, "Android 5 and below - no runtime permissions needed");
            return true;
        }
    }

    public void requestStoragePermission() {
        Log.d(TAG, "Requesting storage permission for Android " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - request MANAGE_EXTERNAL_STORAGE permission
            Log.d(TAG, "Requesting MANAGE_EXTERNAL_STORAGE permission");
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2000);
            } catch (Exception e) {
                Log.w(TAG, "Failed to open app-specific settings, trying general settings", e);
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2000);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6+ - request runtime permissions
            Log.d(TAG, "Requesting WRITE_EXTERNAL_STORAGE runtime permission");
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show explanation dialog
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Storage Permission Required")
                    .setMessage("This app needs storage permission to download videos to your device. Please grant the permission to continue.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                            1000);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            } else {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    1000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            Log.d(TAG, "Runtime permission result received");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with download
                Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission granted");
                Toasty.success(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();
                startDownload();
            } else {
                // Permission denied
                Log.w(TAG, "WRITE_EXTERNAL_STORAGE permission denied");
                Toasty.error(this, "Storage permission is required to download files", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2000) {
            Log.d(TAG, "MANAGE_EXTERNAL_STORAGE permission result received");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted, proceed with download
                    Log.d(TAG, "MANAGE_EXTERNAL_STORAGE permission granted");
                    Toasty.success(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();
                    startDownload();
                } else {
                    // Permission denied
                    Log.w(TAG, "MANAGE_EXTERNAL_STORAGE permission denied");
                    Toasty.error(this, "Storage permission is required to download files", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean canWriteToExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            File testFile = new File(Environment.getExternalStorageDirectory(), "test_write_permission");
            try {
                if (testFile.createNewFile()) {
                    testFile.delete();
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        }
    }

    private String getClipboardText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                return clip.getItemAt(0).getText().toString();
            }
        }
        return "";
    }

    private void showStoragePermissionDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs access to your device's storage to download videos. " +
                       "For Android 11+ devices, you'll need to grant 'All files access' permission in Settings. " +
                       "For older devices, you'll need to grant 'Storage' permission.")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                requestStoragePermission();
            })
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Help", (dialog, which) -> {
                showPermissionHelpDialog();
            })
            .show();
    }

    private void showPermissionHelpDialog() {
        String helpText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            helpText = "For Android 11+:\n\n" +
                      "1. Go to Settings > Apps > All-in-One Video Downloader\n" +
                      "2. Tap 'Permissions'\n" +
                      "3. Enable 'All files access'\n" +
                      "4. Return to the app and try downloading again";
        } else {
            helpText = "For Android 6-10:\n\n" +
                      "1. Tap 'Grant Permission' when prompted\n" +
                      "2. Select 'Allow' in the permission dialog\n" +
                      "3. Try downloading again";
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("How to Grant Permission")
            .setMessage(helpText)
            .setPositiveButton("Got it", null)
            .show();
    }

    private void testPythonEnvironment() {
        try {
            // Try to get Python version info
            YoutubeDLRequest request = new YoutubeDLRequest("--version");
            request.addOption("--version");
            
            Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    String version = response.getOut();
                    Log.d(TAG, "Python/yt-dlp version: " + version);
                    Toasty.success(this, "Python environment OK: " + version, Toast.LENGTH_SHORT).show();
                }, e -> {
                    Log.e(TAG, "Python environment check failed", e);
                    Toasty.error(this, "Python environment issue: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            compositeDisposable.add(disposable);
        } catch (Exception e) {
            Log.e(TAG, "Failed to check Python environment", e);
            Toasty.error(this, "Failed to check Python environment", Toast.LENGTH_SHORT).show();
        }
    }
}
