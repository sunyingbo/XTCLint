package com.xtc.lint;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * 下载音乐列表适配器
 * Created by OuyangPeng on 2017/2/17.
 */
public class DownloadMusicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_HEADER = 2;

    private List<DbNECMusic> mDbNECMusicList;
    private LayoutInflater inflater;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener = null;

    public DownloadMusicListAdapter(Context context, List<DbNECMusic> dbNECMusicList) {
        this.mContext = context;
        this.mDbNECMusicList = dbNECMusicList;
        inflater = LayoutInflater.from(context);
    }

    public void setDbNECMusicList(List<DbNECMusic> dbNECMusicList) {
        this.mDbNECMusicList = dbNECMusicList;
    }

    public List<DbNECMusic> getDbNECMusicList() {
        return mDbNECMusicList;
    }


    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_HEADER;
            default:
                return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new DownloadMusicListAdapter.RecyclerHeaderViewHolder(inflater.inflate(R.layout.item123_recycler_header, parent, false));
            default:
                return new DownloadMusicListAdapter.MyViewHolder(inflater.inflate(R.layout.item123_music_download, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                break;
            default:
                DownloadMusicListAdapter.MyViewHolder myViewHolder = (DownloadMusicListAdapter.MyViewHolder) holder;
                final DbNECMusic dbNECMusic = mDbNECMusicList.get(position -1 ); //header
                String name = dbNECMusic.getName();
                String albumArtistName = dbNECMusic.getAlbumArtistName();
                myViewHolder.title.setText(name + " " +albumArtistName);

                Integer downloadStatus = dbNECMusic.getDownloadStatus();
                myViewHolder.image.clearAnimation();
                myViewHolder.image.setVisibility(View.GONE);

                //将数据保存在itemView的Tag中，以便点击时进行获取
                myViewHolder.itemView.setTag(dbNECMusic);
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemClickListener != null) {
                            //注意这里使用getTag方法获取数据
                            mOnItemClickListener.onItemClick(v, (DbNECMusic) v.getTag());
                        }
                    }
                });
                myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mOnItemLongClickListener != null) {
                            //注意这里使用getTag方法获取数据
                            mOnItemLongClickListener.onItemLongClick(v, (DbNECMusic) v.getTag());
                        }
                        return true;
                    }
                });
                break;
        }
    }

    private int getBasicItemCount() {
        return null == mDbNECMusicList ? 0 : mDbNECMusicList.size();
    }

    @Override
    public int getItemCount() {
        return getBasicItemCount() + 1;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, DbNECMusic data);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, DbNECMusic data);
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_download_song_name);
            image = (ImageView) itemView.findViewById(R.id.iv_download_downloading);
        }
    }

    private class RecyclerHeaderViewHolder extends RecyclerView.ViewHolder {
        RecyclerHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
