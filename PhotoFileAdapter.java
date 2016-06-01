package su.moy.chernihov.dailyselfie;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PhotoFileAdapter extends ArrayAdapter<File> {
    private Context mContext;
    private LayoutInflater inflater;

    public PhotoFileAdapter(Context context, ArrayList<File> photoFilesList) {
        super(context, 0, photoFilesList);
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_photo_file, null);
        }

        File photoFile = getItem(position);

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photo_file_item_list_imageView);
        photoImageView.setImageURI(Uri.parse(photoFile.getAbsolutePath()));

        TextView photoNameTextView = (TextView) convertView.findViewById(R.id.photo_file_item_list_textView);
        photoNameTextView.setText(photoFile.getName().replace(".jpg","").replace("IMG_",""));


        return convertView;
    }
}


