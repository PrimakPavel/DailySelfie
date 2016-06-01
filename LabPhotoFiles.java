package su.moy.chernihov.dailyselfie;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class LabPhotoFiles {
    private static LabPhotoFiles mLabs;
    private ArrayList<File> mPhotoFilesList;


    private LabPhotoFiles() {
        mPhotoFilesList = new ArrayList<>();
        refreshFileList();
    }

    public static LabPhotoFiles getInstance(){
        if (mLabs == null) {
            mLabs = new LabPhotoFiles();
        }
        return mLabs;
    }


    public ArrayList<File> getPhotoFilesList() {
        refreshFileList();
        return mPhotoFilesList;
    }

    private void listFilesFromDir(File dir) {
        mPhotoFilesList.clear();
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                mPhotoFilesList.add(file);
            }
        }
    }

    private void refreshFileList(){
        if ( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
            File filesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
            listFilesFromDir(filesDirectory);
        }
    }



}
