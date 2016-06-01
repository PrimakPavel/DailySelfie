package su.moy.chernihov.dailyselfie;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class OpenFileActivity extends Activity {
    private ImageView mPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);
        mPicture = (ImageView) findViewById(R.id.activity_open_file_picture);
        Intent intent = getIntent();
        File file = (File) intent.getSerializableExtra(PhotoListActivity.INTENT_FOR_OPEN_FILE);
        mPicture.setImageURI(Uri.parse(file.getAbsolutePath()));

    }
}
