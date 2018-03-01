package com.filters.shades;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.media.FaceDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomepageActivity extends AppCompatActivity{

    public ImageView mPictureView;
    private CardView mCardView;
    private Bitmap finalBitmap;
    private SeekBar seekBarBrightness;
    private SeekBar seekBarContrast;
    private SeekBar seekBarSaturation;

    // modified image values
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;

    public static final String EXTRA_PICTURE = "com.filters.shades.picture";
    public static final String EXTRA_ORIGIN = "com.filters.shades.origin";
    public static final String EXTRA_POSITION = "com.filters.shades.position";
    public static final String TAG = "com.filters.shades";
    private static final int MAX_FACES = 1;

    public static Intent newIntent(Context packageContext, String picturePath, int uploaded) {

        Intent intent = new Intent(packageContext, HomepageActivity.class);
        intent.putExtra(EXTRA_PICTURE, picturePath);
        intent.putExtra(EXTRA_ORIGIN, uploaded);
        return intent;
    }
    public static Intent filterIntent(Context packageContext, String picturePath, int uploaded, int position) {

        Intent intent = new Intent(packageContext, HomepageActivity.class);
        intent.putExtra(EXTRA_PICTURE, picturePath);
        intent.putExtra(EXTRA_ORIGIN, uploaded);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    public void setImage(String picturePath, int uploaded, int position) {
        Uri selectedImage = Uri.parse(picturePath);
        finalBitmap = null;
        try {
            if (uploaded == 0) {
                finalBitmap = BitmapFactory.decodeFile(selectedImage.getPath());
                finalBitmap = flipBitmapHorizontally(finalBitmap);
            } else if (uploaded == 1) {
                finalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            } else {
                finalBitmap = BitmapFactory.decodeFile(selectedImage.getPath());
            }

        } catch (Exception ioe) {
            Log.d(TAG, "Error uploading the picture: " + ioe.getMessage());
        }

        if (position!=0){
            List<Filter> filters = FilterPack.getFilterPack(getBaseContext());
            finalBitmap = filters.get(position-1).processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true));
            mPictureView.setImageBitmap(finalBitmap);
        }else {
            mPictureView.setImageBitmap(finalBitmap);
        }
        publishToFaceBook(finalBitmap);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        overridePendingTransition(0, 0);

        mPictureView = (ImageView)findViewById(R.id.image_view_filters);
        String pictureToShowPath = getIntent().getStringExtra(EXTRA_PICTURE);
        Uri selectedImage = Uri.parse(pictureToShowPath);
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        int up = getIntent().getIntExtra(EXTRA_ORIGIN, 0);
        finalBitmap = null;
        try {
            if (up == 0) {
                finalBitmap = BitmapFactory.decodeFile(selectedImage.getPath());
                finalBitmap = flipBitmapHorizontally(finalBitmap);
            } else if (up == 1) {
                finalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } else {
                finalBitmap = BitmapFactory.decodeFile(selectedImage.getPath());
            }

        } catch (Exception ioe) {
            Log.d(TAG, "Error uploading the picture: " + ioe.getMessage());
        }

        if (position!=0){
            List<Filter> filters = FilterPack.getFilterPack(getBaseContext());
            finalBitmap = filters.get(position-1).processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true));
            mPictureView.setImageBitmap(finalBitmap);
        }else {
            mPictureView.setImageBitmap(finalBitmap);
        }

        mCardView = (CardView)findViewById(R.id.card_view_filters);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mCardView.getBackground().setAlpha(0);
        }
        else {
            mCardView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        }
        mCardView.setCardElevation(0);

        publishToFaceBook(finalBitmap);

        seekBarBrightness = (SeekBar)findViewById(R.id.seekbar_brightness);
        seekBarBrightness.setMax(200);
        seekBarBrightness.setProgress(100);

        // keeping contrast value b/w 1.0 - 3.0
        seekBarContrast = (SeekBar)findViewById(R.id.seekbar_contrast);
        seekBarContrast.setMax(20);
        seekBarContrast.setProgress(0);

        // keeping saturation value b/w 0.0 - 3.0
        seekBarSaturation = (SeekBar)findViewById(R.id.seekbar_saturation);
        seekBarSaturation.setMax(30);
        seekBarSaturation.setProgress(10);

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onBrightnessChanged(progress -100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onEditCompleted();
            }
        });

        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress += 10;
                float floatVal = .10f * progress;
                onContrastChanged(floatVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onEditCompleted();
            }
        });

        seekBarSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float floatVal = .10f * progress;
                onSaturationChanged(floatVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onEditCompleted();
            }
        });

        Button saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveBitmapToStorage(finalBitmap);

            }
        });
    }

    private Bitmap flipBitmapHorizontally(Bitmap source) {
        float centerX = source.getWidth() / 2;
        float centerY = source.getHeight() / 2;

        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, centerX, centerY);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void prepareBitmapForRecognition(Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        int faceWidth = bitmap.getWidth();
        int faceHeight = bitmap.getHeight();

        setFace(bitmap, faceWidth, faceHeight);
    }

    private void setFace(Bitmap bitmap, int faceWidth, int faceHeight) {
            FaceDetector fd;
            FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];
            PointF midpoint = new PointF();
            int [] fpx = null;
            int [] fpy = null;
            int count = 0;

            try {
                fd = new FaceDetector(faceWidth, faceHeight, MAX_FACES);
                count = fd.findFaces(bitmap, faces);
            } catch (Exception e) {
                Log.e(TAG, "setFace(): " + e.toString());
                return;
            }

            // check if we detect any faces
            if (count > 0) {
                fpx = new int[count];
                fpy = new int[count];

                for (int i = 0; i < count; i++) {
                    try {
                        faces[i].getMidPoint(midpoint);

                        fpx[i] = (int)midpoint.x;
                        fpy[i] = (int)midpoint.y;
                    } catch (Exception e) {
                        Log.e(TAG, "setFace(): face " + i + ": " + e.toString());
                    }
                }
            }

            //mPictureView.setDisplayPoints(fpx, fpy, count, 0);
    }

    public void publishToFaceBook(Bitmap bitmap){
        FacebookSdk.sdkInitialize(getApplicationContext());
        final ShareButton fbShareButton = (ShareButton) findViewById(R.id.share_btn);
        final Bitmap bitmapFinal = bitmap;

        SharePhoto photo = new SharePhoto.Builder().setBitmap(bitmapFinal).build();
        SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();

        fbShareButton.setShareContent(content);
    }

    public void changeFilter(int filter){
        List<Filter> filters = FilterPack.getFilterPack(getBaseContext());
        finalBitmap = filters.get(filter-1).processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true));
        mPictureView.setImageBitmap(finalBitmap);
    }

    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        finalBitmap = myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true));
        mPictureView.setImageBitmap(finalBitmap);
    }

    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        finalBitmap = myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true));
        mPictureView.setImageBitmap(finalBitmap);
    }

    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        finalBitmap = myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true));
        mPictureView.setImageBitmap(finalBitmap);
    }

    public void onEditCompleted() {
        Bitmap bitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        finalBitmap = myFilter.processFilter(bitmap);
    }

    public void saveBitmapToStorage(Bitmap bitmap){
        File pictureFile = getOutputMediaFile(1);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException fe) {
            Log.d(TAG, "File not found: " + fe.getMessage());
        } catch (IOException ioe) {
            Log.d(TAG, "Error accessing file: " + ioe.getMessage());
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, pictureFile.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, R.string.pictures_description);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, pictureFile.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, pictureFile.getName().toLowerCase(Locale.US));
        values.put("_data", pictureFile.getAbsolutePath());

        ContentResolver cr = getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    private static File getOutputMediaFile(int type) {
        //Get folder on phone's storage
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Shades");

        //Create folder if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Shades", "Failed to create directory");
                return null;
            }
        }
        //Save picture to file in the chosen folder
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }
}
