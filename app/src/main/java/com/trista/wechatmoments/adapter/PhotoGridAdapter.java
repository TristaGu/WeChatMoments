package com.trista.wechatmoments.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.trista.wechatmoments.R;
import com.trista.wechatmoments.entity.Image;
import com.trista.wechatmoments.util.DisplayUtils;

import java.util.List;

/**
 * The adapter for the photo GridView.
 * <p>
 * Created by Trista on 2018/2/26.
 */
public class PhotoGridAdapter extends BaseAdapter {
    private List<Image> mImages;

    public PhotoGridAdapter(List<Image> images) {
        mImages = images;
    }

    /**
     * Replace the data.
     *
     * @param images the images.
     */
    public void replaceData(List<Image> images) {
        mImages = images;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mImages == null ? 0 : mImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(parent.getContext());
            convertView = imageView;
        } else {
            imageView = (ImageView) convertView;
        }

        Image image = mImages.get(position);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();

        switch (mImages.size()) {
            case 1:
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                            .WRAP_CONTENT);
                    imageView.setLayoutParams(params);
                } else {
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                Picasso.with(parent.getContext())
                        .load(image.getUrl())
                        .placeholder(R.drawable.photo_placeholder)
                        .resize(600, 400)
                        .error(R.drawable.photo_placeholder)
                        .into(imageView);
                break;
            default:
                int width = DisplayUtils.dp2px(parent.getContext(), 100);
                int height = DisplayUtils.dp2px(parent.getContext(), 100);
                if (params == null) {
                    params = new ViewGroup.LayoutParams(width, height);
                    imageView.setLayoutParams(params);
                } else {
                    params.width = width;
                    params.height = height;
                }
                Picasso.with(parent.getContext())
                        .load(image.getUrl())
                        .placeholder(R.drawable.photo_placeholder)
                        .resize(200, 200)
                        .centerCrop()
                        .error(R.drawable.photo_placeholder)
                        .into(imageView);
                break;
        }
        return convertView;
    }
}
