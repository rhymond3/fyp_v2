package com.example.fyp_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp_v2.Class.Receipt;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Result extends AppCompatActivity {

    private EditText inputDescription, inputTotal;
    private ImageView imageView;
    private Bitmap bitmap;
    private Context context;
    private ProgressDialog p;
    private Uri fileUri;
    private Button submit;
    private DatePicker datePicker;
    private ProgressBar progressBar;

    private DatabaseReference receiptDatabase;
    private StorageReference imageStorage;
    private String userID;

    private String receiptID;
    private String UriPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        inputDescription = findViewById(R.id.description);
        inputTotal = findViewById(R.id.total);
        imageView = findViewById(R.id.imageView);
        submit = findViewById(R.id.submit);
        datePicker = findViewById(R.id.datePicker);
        progressBar = findViewById(R.id.progressBar3);
        context = Result.this;

        progressBar.setPadding(500,300,500,1500);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.i("ID", userID);
        imageStorage = FirebaseStorage.getInstance().getReference("upload/");
        receiptDatabase = FirebaseDatabase.getInstance().getReference("Receipt").child(userID);

        String filename = getIntent().getStringExtra("filePath");
        fileUri = Uri.parse(filename);

        try {
            InputStream is = context.getContentResolver().openInputStream(fileUri);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(is, null, options);
            bitmap = scaleToFill(bitmap, 864, 1152);

        } catch (Exception ex) {
            Log.i(getClass().getSimpleName(), ex.getMessage());
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }

        runTextRecognition();
        imageView.setImageBitmap(bitmap);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitReceipt();
            }
        });

    }

    private void submitReceipt() {
        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1;
        int day = datePicker.getDayOfMonth();

        final String description = inputDescription.getText().toString();
        final String total = inputTotal.getText().toString();
        final String date = day + "/" + month + "/" + year;



        if(TextUtils.isEmpty(description)){
            Toast.makeText(getApplicationContext(), "Enter description!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(date)){
            Toast.makeText(getApplicationContext(), "Enter date!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        final StorageReference fileReference = imageStorage.child(System.currentTimeMillis() + "." + getFileExtension(fileUri));

        fileReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                UriPath = uri.toString();
                                Log.i("imageUri", UriPath);
                                receiptID = receiptDatabase.push().getKey();
                                Receipt receipt = new Receipt(receiptID, description, date, total, UriPath);
                                receiptDatabase.child(receiptID).setValue(receipt);
                            }
                        });
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Toast.makeText(Result.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Result.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Toast.makeText(Result.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static Bitmap scaleToFill(Bitmap b, int width, int height) {
        float factorH = height / (float) b.getHeight();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
                (int) (b.getHeight() * factorToUse), true);
    }

    private void runTextRecognition() {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionDocumentTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudDocumentTextRecognizer();
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });

    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {

        String elementText = "";
        String blockText = "";
        String lineText = "";
        List<RecognizedLanguage> lineLanguages;

        for (FirebaseVisionText.TextBlock block: texts.getTextBlocks()) {
            blockText += block.getText() +"\r";
            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line: block.getLines()) {
                lineText += line.getText()+ "\n";
                Float lineConfidence = line.getConfidence();
                lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    elementText += element.getText() + "\n";
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsoluteFile() + "/text/");
        dir.mkdir();
        File file = new File(dir, "sample.txt");
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(elementText.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputTotal.setText(elementText);
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
}

