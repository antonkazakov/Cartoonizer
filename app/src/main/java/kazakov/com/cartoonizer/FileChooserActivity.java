package kazakov.com.cartoonizer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.bitmap;

public class FileChooserActivity extends AppCompatActivity {

    Button btnCamera;
    String TAG = "CARTOON";
    private ImageView cartoonView;


    Bitmap cartoonBMP;
    CartoonFilter cf;

    Mat output;
    Mat imgMat;
    Button btnGallery;
    static{ System.loadLibrary("opencv_java3"); }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);

        cartoonView = (ImageView) findViewById(R.id.imageView);

        btnCamera = (Button) findViewById(R.id.btn_camera);
        btnGallery = (Button)findViewById(R.id.btn_gallery);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 10002);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri("tempPhoto.jpg"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 10001);
                }
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10002:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(data.getData());

                        init(getResizedBitmap(BitmapFactory.decodeStream(imageStream),800));

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 10001:
                if (resultCode == Activity.RESULT_OK) {
                    Uri takenPhotoUri = getPhotoFileUri("tempPhoto.jpg");
                    init(getResizedBitmap(BitmapFactory.decodeFile(takenPhotoUri.getPath()),800));
                }
                break;
        }
    }


    @Override
    public void onPause()
    {
        super.onPause();
    }


    private void init(Bitmap bitmap){
        imgMat = new Mat();
        output = new Mat();

        Utils.bitmapToMat(bitmap,imgMat);

        cf = new CartoonFilter();
        cf.processFrame(imgMat, output);

        Bitmap tmp = Bitmap.createBitmap(output.cols(), output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(output, tmp);


        cartoonView.setImageBitmap(tmp);
    }

    public  Uri getPhotoFileUri(String fileName) {


        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"tempPhoto.jpg");

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                // Log.d(APP_TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public  Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}
