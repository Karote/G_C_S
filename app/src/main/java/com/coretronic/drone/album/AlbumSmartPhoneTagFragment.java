package com.coretronic.drone.album;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.album.adapter.AlbumGridViewAdapter;
import com.coretronic.drone.album.model.MediaItem;
import com.coretronic.drone.utility.AppConfig;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by james on 15/6/1.
 * test git
 */
public class AlbumSmartPhoneTagFragment extends UnBindDrawablesFragment {

    //    private final static String FILTER_MEDIA_FOLDER = "/DCIM/100ANDRO/";
//    private final static String FILTER_MEDIA_FOLDER = "external/";
    private String FILTER_MEDIA_FOLDER = "";
    private static String TAG = AlbumSmartPhoneTagFragment.class.getSimpleName();
    private Context mContext = null;
    private RecyclerView albumGridView = null;
    private AlbumGridViewAdapter albumGridViewAdapter = null;
    private ArrayList<MediaItem> albumImgList = new ArrayList<MediaItem>();
    private FragmentManager fragmentManager = null;
    private TextView noMediaTV = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, TAG + " onCreateView ");
        View view = inflater.inflate(R.layout.fragment_album_smartphonetag, container, false);
        mContext = view.getContext();

        FILTER_MEDIA_FOLDER = AppConfig.getMediaFolderPosition(mContext);

        fragmentManager = getChildFragmentManager();


        noMediaTV = (TextView) view.findViewById(R.id.no_mediainfolder_tv);
        albumGridView = (RecyclerView) view.findViewById(R.id.album_grid_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
        albumGridView.setLayoutManager(gridLayoutManager);


        // get media array list
        getData();

        albumGridViewAdapter = new AlbumGridViewAdapter(mContext, R.layout.album_smartphone_griditem, albumImgList);
        albumGridView.setAdapter(albumGridViewAdapter);
        albumGridViewAdapter.notifyDataSetChanged();

        // AlbumFragment
//        Fragment cuttentFragment = ((FragmentActivity) mContext).getSupportFragmentManager().findFragmentByTag("fragment");
//        ((AlbumFragment) cuttentFragment).updateTrashUI();

        return view;
    }

    //    private ArrayList<ImageItem> getData() {
    private void getData() {
//        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        albumImgList.clear();

        ContentResolver cr = mContext.getContentResolver();


        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        };

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


        Uri queryUri = MediaStore.Files.getContentUri("external");
//        Uri queryUri = MediaStore.Files.getContentUri( Environment.DIRECTORY_DCIM );
//        Log.i(TAG,"URI:"+mContext.getExternalCacheDir() +"/BDT/");
//        Log.i(TAG,"-URI:"+Environment.getExternalStorageDirectory().getAbsolutePath()  +"/DCIM/BDT/");
//        Uri queryUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()  +"/DCIM/BDT/");
//        Uri queryUri = Uri.parse( "content://media/external/file/" );

        CursorLoader cursorLoader = new CursorLoader(
                mContext,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );


        Cursor cursor = cursorLoader.loadInBackground();
//        Log.i(TAG,"cursor.getCount():"+cursor.getCount());
        if (cursor == null) {
            return;
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String fileFullPath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            Log.i(TAG, "fileFullPath:" + fileFullPath);
            if (!fileFullPath.contains(FILTER_MEDIA_FOLDER)) {
                return;
            }
            long fileId = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
            int imgType = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)));

            String fileAddDate = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(fileAddDate) * 1000);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String timeStamp = simpleDateFormat.format(calendar.getTime());

            Date mediaDate = null;
            try {
                mediaDate = simpleDateFormat.parse(timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "filePath/fileId/imgType/mediaFileDate:" + fileFullPath + fileId + imgType + timeStamp);

            albumImgList.add(new MediaItem(fileFullPath, mediaDate, "Image#" + i, fileId, imgType, false));
        }

        cursor.close();

/*

//        String[] projection = { MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA};

         String[] projection2 = {MediaStore.Images.Thumbnails.DATA, MediaStore.Video.Thumbnails.DATA};
         cursor = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,projection2,null,null,null);
        Log.i(TAG,"=======");
        Log.i(TAG,"cursor.getCount():"+cursor.getCount());
        for( int i = 0 ; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            Log.i(TAG,"filePath:"+filePath);
            File file = new File(filePath);

            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            imageItems.add( new ImageItem(myBitmap, "Image#" + i, null , null) );
        }


        cursor = cr.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,projection2,null,null,null);
        Log.i(TAG,"cursor.getCount():"+cursor.getCount());
        for( int i = 0 ; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
            Log.i(TAG,"filePath:"+filePath);
            File file = new File(filePath);

//            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

//            imageItems.add( new ImageItem(myBitmap, "Image#" + i) );
        }



        cursor.close();
*/

        /*
//        TypedArray imgs = mContext.getResources().obtainTypedArray(R.array.image_ids);
//
//        for (int i = 0; i < imgs.length(); i++) {
//            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), imgs.getResourceId(i, -1));
//            imageItems.add(new ImageItem(bitmap, "Image#" + i));
//        }

        File path = new File(Environment.getExternalStorageDirectory(), "/DCIM/100ANDRO");

        if (path.exists()) {
            String[] fileNames = path.list();

            for (int i = 0; i < fileNames.length; i++) {

                Log.i("info", "path.getPath():" + path.getPath() + "/" + fileNames[i]);
                String imgPath = path.getPath() + "/" + fileNames[i];


                String[] fileNameSplit = fileNames[i].split("\\.(?=[^\\.]+$)");
                Log.i("info", "fileNamesplit:" + fileNames[0]);
//                Log.i("info", "fileNamesplit.length:" + fileNameSplit.length);
                String extension = fileNameSplit[fileNameSplit.length -1];
                Log.i("info"," extension:"+extension);

//                if ((extension.equals("jpg")) || (extension.equals("png")) || (extension.equals("jpeg") ) )
//                {
//                    Bitmap mBitmap = Utility.rotateAndResizeBitmap(imgPath, 150, 150);
//                    imageItems.add( new ImageItem(mBitmap, "Image#" + i) );

                    File file = new File( imgPath );
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(file.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] imageData=exif.getThumbnail();
                    Bitmap  thumbnail= BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    imageItems.add( new ImageItem(thumbnail, "Image#" + i) );
//                }
//                else if( extension.equals("mov") || extension.equals("mp4") )
//                {
//
//
//                }
            }
        }*/
//        return imageItems;

        if (albumImgList.size() == 0) {
            noMediaTV.setVisibility(View.VISIBLE);
            albumGridView.setVisibility(View.GONE);
        } else {
            noMediaTV.setVisibility(View.GONE);
            albumGridView.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        hideDeleteOption();
        deleteSelectedPathAryList();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, TAG + " onResume");

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void showDeleteOption() {
        // show delete option elements visible
        albumGridViewAdapter.setIsShowDeleteOption(true);
        refreshData();
    }

    public void hideDeleteOption() {
        // hide delete option elements visible
        albumGridViewAdapter.setIsShowDeleteOption(false);
        refreshData();
    }

    public void deleteSelectedPathAryList() {
        albumGridViewAdapter.deleteSelectedPathAryList();
        albumGridViewAdapter.notifyDataSetChanged();
    }

    public void deleteSelectMediaFile() {
        albumGridViewAdapter.deleteSelectMediaFile();
//        albumGridViewAdapter.notifyDataSetChanged();
        Log.i(TAG, "path list:" + albumGridViewAdapter.getSelectedPathAryList());
    }


    public void refreshData() {
        getData();
        albumGridViewAdapter.notifyDataSetChanged();
    }

    public ArrayList<MediaItem> getAlbumImgList() {
        return albumImgList;
    }
}
