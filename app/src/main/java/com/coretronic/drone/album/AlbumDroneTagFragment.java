package com.coretronic.drone.album;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.album.adapter.AlbumListViewAdapter;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.utility.AppConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by james on 15/6/1.
 */
public class AlbumDroneTagFragment extends Fragment implements DroneController.MediaCommandListener {

    private static String TAG = AlbumDroneTagFragment.class.getSimpleName();

    private Context mContext = null;
    private ProgressBar progressbar = null;
    private RecyclerView albumListView = null;
    private AlbumListViewAdapter albumListViewAdapter = null;
    private ArrayList<MediaListItem> albumMediaList = new ArrayList<MediaListItem>();
    private String albumFilePath = "";

    private TextView notFindListTV = null;
    Handler showListHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.obj == "complete") {
                Log.i(TAG, "showListHandler message:" + msg.obj);
                albumListViewAdapter.notifyDataSetChanged();
                albumListView.setVisibility(View.VISIBLE);
            }

            progressbar.setVisibility(View.GONE);

        }
    };

    Handler processUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    progressbar.setVisibility(View.GONE);
                    notFindListTV.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    progressbar.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    DroneController droneController = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "=== AlbumDroneTagFragment onCreateView===");
        View view = inflater.inflate(R.layout.fragment_album_dronetag, container, false);
        mContext = view.getContext();

        albumFilePath = AppConfig.getMediaFolderPosition(mContext);

        progressbar = (ProgressBar) view.findViewById(R.id.progressbar);
        albumListView = (RecyclerView) view.findViewById(R.id.album_list_view);
        notFindListTV = (TextView) view.findViewById(R.id.not_get_list_tv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        albumListView.setLayoutManager(linearLayoutManager);


        // create the album folder for download media
        File albumFolder = new File(albumFilePath);
        if (!albumFolder.exists()) {
            albumFolder.mkdir();
        }


        albumListViewAdapter = new AlbumListViewAdapter(mContext, albumMediaList);
        albumListView.setAdapter(albumListViewAdapter);
        albumListViewAdapter.SetOnItemClickListener(recyclerItemClickListener);

        progressbar.setVisibility(View.VISIBLE);

        droneController = ((MainActivity) getActivity()).getDroneController();
        if (droneController == null) {
            processUIHandler.sendEmptyMessage(0);
        } else {
            boolean status = droneController.getMediaContents(AlbumDroneTagFragment.this);
            if (!status) {
                Log.i(TAG, "droneController.getMediaContents FAIL");
                processUIHandler.sendEmptyMessage(0);
            } else {
                Log.i(TAG, "droneController.getMediaContents SUCCESS");
            }
        }

        return view;
    }


    AlbumListViewAdapter.OnItemClickListener recyclerItemClickListener = new AlbumListViewAdapter.OnItemClickListener() {

//        @Override
//        public void onItemDeleteClick(View view, final int position) {
//            Log.i(TAG, "delete:" + position);
//
//
//            // delete drone file
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    droneController.removeContent(albumMediaList.get(position).getMediaFileName(), AlbumDroneTagFragment.this);
//                    droneController.getMediaContents(AlbumDroneTagFragment.this);
//                }
//            }).start();
//
//        }

        // when download the file, it will open a new fragment and pass the file name
        @Override
        public void onDownloadClick(View view, int position) {
            Log.i(TAG, "download:" + position);

            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaListItemData", albumMediaList.get(position));

//            Toast.makeText(mContext, "download " + view.getTag(), Toast.LENGTH_SHORT).show();
            DownloadWarningFragment downloadWarningFragment = new DownloadWarningFragment();
            downloadWarningFragment.setArguments(bundle);

            Fragment cuttentFragment = getActivity().getSupportFragmentManager().findFragmentByTag("fragment");
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .hide(cuttentFragment)
                    .replace(R.id.frame_view, downloadWarningFragment, "DownloadWarningFragment")
                    .addToBackStack("DownloadWarningFragment")
                    .commit();
        }
    };

    private void getDate(List<MediaContent> listItems) {
        Log.i(TAG, "droneController getDate");
        albumMediaList.clear();

        for (int i = 0; i < listItems.size(); i++) {
            albumMediaList.add(new MediaListItem(listItems.get(i)));
        }
        Collections.sort(albumMediaList, new Comparator<MediaListItem>() {
            @Override
            public int compare(MediaListItem lhs, MediaListItem rhs) {
                return rhs.getMediaDate().compareTo(lhs.getMediaDate());
            }
        });

        Message message = Message.obtain();
        message = showListHandler.obtainMessage(0, "complete");
        showListHandler.sendMessage(message);

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "album drone tag fragment onDestroyView");
    }

    @Override
    public void onCommandResult(MediaCommand mediaCommand, boolean isSuccess, Object data) {
        Log.i(TAG, "droneController onCommandResult");
        if (isSuccess) {
            Log.i(TAG, "droneController onCommandResult success");
            switch (mediaCommand) {
                case LIST_CONTENTS:
                    List<MediaContent> listItems = (List<MediaContent>) data;
                    getDate(listItems);
                    break;
                case REMOVE_CONTENT:
                    Log.i(TAG, "delete file completed");
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "delete file completed", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                default:
                    break;
            }
        } else {
            Log.i(TAG, "droneController onCommandResult fail");
            processUIHandler.sendEmptyMessage(0);
        }
    }

}
