package com.coretronic.drone.album;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
    private AutofitRecyclerView albumGridView = null;
    private AlbumGridViewAdapter albumGridViewAdapter = null;
    private ArrayList<MediaItem> albumImgList = new ArrayList<MediaItem>();
    private FragmentManager fragmentManager = null;
    private TextView noMediaTV = null;
//    private ProgressBar loadingPhotoProgress = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, TAG + "smartphone onCreateView ");
        View view = inflater.inflate(R.layout.fragment_album_smartphonetag, container, false);
        mContext = view.getContext();

        FILTER_MEDIA_FOLDER = AppConfig.getMediaFolderPosition();

        fragmentManager = getChildFragmentManager();


//        loadingPhotoProgress = (ProgressBar) view.findViewById(R.id.loadphoto_progressbar);
        noMediaTV = (TextView) view.findViewById(R.id.no_mediainfolder_tv);
        albumGridView = (AutofitRecyclerView) view.findViewById(R.id.album_grid_view);


//        loadingPhotoProgress.setVisibility(View.VISIBLE);
        // get media array list
        getData();

        albumGridViewAdapter = new AlbumGridViewAdapter(mContext, R.layout.album_smartphone_griditem, albumImgList);
        albumGridView.setAdapter(albumGridViewAdapter);
        albumGridViewAdapter.notifyDataSetChanged();
//        loadingPhotoProgress.setVisibility(View.GONE);
        // AlbumFragment
//        Fragment cuttentFragment = ((FragmentActivity) mContext).getSupportFragmentManager().findFragmentByTag("fragment");
//        ((AlbumFragment) cuttentFragment).updateTrashUI();

        return view;
    }

    //    private ArrayList<ImageItem> getData() {
    private void getData() {
        Log.i(TAG, "smartphone getData");
//        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        albumImgList.clear();

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        };

        int depthOfPath = FILTER_MEDIA_FOLDER.length() - FILTER_MEDIA_FOLDER.replace("/", "").length();

        String selection =
                MediaStore.Files.FileColumns.DATA + " LIKE "
                        + "'" + FILTER_MEDIA_FOLDER + "%'"
                        + " AND (SELECT LENGTH(_data) - LENGTH(REPLACE(_data, '/', ''))) = " + depthOfPath
                        + " AND ("
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + ")";


        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                mContext,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );


        Cursor cursor = cursorLoader.loadInBackground();
        if (cursor == null) {
            return;
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String fileFullPath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
//            if (!fileFullPath.contains(FILTER_MEDIA_FOLDER)) {
//                continue;
//            }
            long fileId = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
            int imgType = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)));

            long videoDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            String duration = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(videoDuration),
                    TimeUnit.MILLISECONDS.toSeconds(videoDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(videoDuration)));

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

            albumImgList.add(new MediaItem(fileFullPath, mediaDate, "Image#" + i, fileId, imgType, duration, false));
        }

        Collections.sort(albumImgList, new Comparator<MediaItem>() {
            @Override
            public int compare(MediaItem lhs, MediaItem rhs) {
                return rhs.getMediaDate().compareTo(lhs.getMediaDate());
            }
        });

        cursor.close();

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
