package com.coretronic.drone.album;

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
import android.widget.Toast;
import com.coretronic.drone.R;
import com.coretronic.drone.album.adapter.AlbumListViewAdapter;
import com.coretronic.drone.album.model.MediaListItem;
import com.coretronic.drone.ambarlla.message.AMBACmdClient;
import com.coretronic.drone.ambarlla.message.AMBACommand;
import com.coretronic.drone.ambarlla.message.FileItem;
import com.coretronic.drone.utility.AppUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 15/6/1.
 */
public class AlbumDroneTagFragment extends Fragment {

    private static String TAG = AlbumDroneTagFragment.class.getSimpleName();

    private Context mContext = null;
    private RecyclerView albumListView = null;
    private AlbumListViewAdapter albumListViewAdapter = null;
    private ArrayList<MediaListItem> albumImgList = new ArrayList<MediaListItem>();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if( msg.obj == "complete" )
            {
                Log.i(TAG,"handler message:"+ msg.obj);
                albumListViewAdapter.notifyDataSetChanged();
            }
        }
    };

    // AMBAClient
    AMBACmdClient cmdClient = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_dronetag, container, false);
        mContext = view.getContext();


        albumListView = (RecyclerView) view.findViewById(R.id.album_list_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        albumListView.setLayoutManager(linearLayoutManager);


        Log.i(TAG, "=== AlbumDroneTagFragment onCreateView===");
        albumListViewAdapter = new AlbumListViewAdapter(mContext, albumImgList);
        albumListView.setAdapter(albumListViewAdapter);
        albumListViewAdapter.SetOnItemClickListener(recyclerItemClickListener);
//        albumListViewAdapter.notifyDataSetChanged();

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToAMBA();
            }
        }).start();

        return view;
    }


    AlbumListViewAdapter.OnItemClickListener recyclerItemClickListener = new AlbumListViewAdapter.OnItemClickListener() {

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

    private void getDate(List<FileItem> listItems) {
        albumImgList.clear();

        for (int i = 0; i < listItems.size(); i++) {
            albumImgList.add(new MediaListItem(listItems.get(i)));
//            albumImgList.get(i).setMediaFileName("coretronicDrone"+"i"+".png");
//            albumImgList.get(i).setMediaSize((int)(Math.random()*99+101)+" MB");
//            albumImgList.get(i).setMediaDate(new Date());

        }
        Message message = new Message();
        message = handler.obtainMessage(0, "complete");
        handler.sendMessage(message);

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


    private void connectToAMBA() {
        cmdClient = new AMBACmdClient();


        AMBACmdClient.CmdReceiver cmdReceiver = new AMBACmdClient.CmdReceiver() {
            @Override
            public void onMessage(AMBACommand objMessage) {
//                objMessage.toLog();
            }

        };


        AMBACmdClient.CmdListFileReceiver cmdListFileReceiver = new AMBACmdClient.CmdListFileReceiver() {
            @Override
            public void onCompleted(List<FileItem> listItems) {
                getDate(listItems);

            }
        };


        try {
            cmdClient.connectToServer(AppUtils.SERVER_IP, AppUtils.COMMAND_PORT, AppUtils.DATA_PORT);
            cmdClient.setFileSavePath("/Users/leokao/Desktop/");
            cmdClient.setClientIP("192.168.42.6");
            cmdClient.start();
            cmdClient.cmdStartSession();
            cmdClient.getFileList(cmdListFileReceiver);

        } catch (IOException e) {
            e.printStackTrace();
            cmdClient.close();
            Log.e(TAG, "connect error:" + e.getMessage());
        }


    }
}
