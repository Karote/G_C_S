package com.coretronic.drone.album;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface BtnClickListener{
        public abstract void downloadBtnClickListener(int position);
    }

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


        albumListViewAdapter = new AlbumListViewAdapter(mContext, albumImgList);
        albumListView.setAdapter(albumListViewAdapter);
        albumListViewAdapter.notifyDataSetChanged();
        return view;
    }

    private void getDate()
    {
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

}
