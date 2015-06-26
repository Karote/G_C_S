package com.coretronic.drone.album;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.coretronic.drone.R;
import com.coretronic.drone.album.adapter.AlbumGridViewAdapter;
import com.coretronic.drone.album.adapter.AlbumListViewAdapter;
import com.coretronic.drone.album.model.MediaItem;
import com.coretronic.drone.album.model.MediaListItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by james on 15/6/1.
 */
public class AlbumDroneTagFragment extends Fragment {

    private static String TAG = AlbumDroneTagFragment.class.getSimpleName();

    private Context mContext = null;
    private RecyclerView albumListView = null;
    private AlbumListViewAdapter albumListViewAdapter = null;
    private ArrayList<MediaListItem> albumImgList = new ArrayList<MediaListItem>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_dronetag, container, false);
        mContext = view.getContext();

        getDate();

        albumListView = (RecyclerView) view.findViewById(R.id.album_list_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        albumListView.setLayoutManager(linearLayoutManager);


        Log.i(TAG, "=== AlbumDroneTagFragment onCreateView===");
        albumListViewAdapter = new AlbumListViewAdapter(mContext, albumImgList);
        albumListView.setAdapter(albumListViewAdapter);
        albumListViewAdapter.SetOnItemClickListener(recyclerItemClickListener);
        albumListViewAdapter.notifyDataSetChanged();
        return view;
    }



    AlbumListViewAdapter.OnItemClickListener recyclerItemClickListener = new AlbumListViewAdapter.OnItemClickListener()
    {

        @Override
        public void onItemDeleteClick(View view, int position) {
            Log.i(TAG, "delete:" + position);
            Toast.makeText(mContext, "delete " + view.getTag(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDownloadClick(View view, int position) {
            Log.i(TAG, "download:" + position);

            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaListItemData", albumImgList.get(position));

            Toast.makeText(mContext, "download " + view.getTag(), Toast.LENGTH_SHORT).show();
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

    private void getDate()
    {
        albumImgList.clear();
        for( int i = 0 ; i<= 99;i++)
        {

            albumImgList.add(new MediaListItem("coretronicDrone"+ i +".png",
                    (int)(Math.random()*99+101)+" MB",
                    (new SimpleDateFormat("yyyy/MM/dd")).format(new Date())));
//            albumImgList.get(i).setMediaFileName("coretronicDrone"+"i"+".png");
//            albumImgList.get(i).setMediaSize((int)(Math.random()*99+101)+" MB");
//            albumImgList.get(i).setMediaDate(new Date());

        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


//
//    public void refreshListData()
//    {
//        getDate();
//        albumListViewAdapter.notifyDataSetChanged();
//    }
}
