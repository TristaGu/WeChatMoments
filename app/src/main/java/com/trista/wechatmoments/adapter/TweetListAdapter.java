package com.trista.wechatmoments.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trista.wechatmoments.R;
import com.trista.wechatmoments.entity.Comment;
import com.trista.wechatmoments.entity.Tweet;
import com.trista.wechatmoments.util.DisplayUtils;

import java.util.List;

/**
 * The adapter for the tweet list.
 * <p>
 * Created by Trista on 2018/2/25.
 */
public class TweetListAdapter extends BaseAdapter {
    private List<Tweet> mTweets;

    public TweetListAdapter(List<Tweet> tweets) {
        mTweets = tweets;
    }

    /**
     * Reset the data.
     *
     * @param tweets the tweets
     */
    public void resetData(List<Tweet> tweets) {
        mTweets.clear();
        addData(tweets);
    }

    /**
     * Add data.
     *
     * @param tweets the tweets
     */
    public void addData(List<Tweet> tweets) {
        mTweets.addAll(tweets);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTweets == null ? 0 : mTweets.size();
    }

    @Override
    public Tweet getItem(int position) {
        return mTweets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tweet, parent, false);
            holder = new ViewHolder();
            holder.avatarImageView = (ImageView) convertView.findViewById(R.id.sender_avatar);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.sender_name);
            holder.contentTextView = (TextView) convertView.findViewById(R.id.tweet_content);
            holder.photoGridView = (GridView) convertView.findViewById(R.id.photo_grid);
            holder.commentContainer = (ViewGroup) convertView.findViewById(R.id.comment_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Tweet tweet = mTweets.get(position);
        Picasso.with(parent.getContext())
                .load(tweet.getSender().getAvatar())
                .placeholder(R.drawable.avatar_placeholder)
                .resize(100, 100)
                .centerCrop()
                .error(R.drawable.avatar_placeholder)
                .into(holder.avatarImageView);
        holder.nameTextView.setText(tweet.getSender().getNickname());

        // Show or hide the content.
        if (TextUtils.isEmpty(tweet.getContent())) {
            holder.contentTextView.setVisibility(View.GONE);
        } else {
            holder.contentTextView.setVisibility(View.VISIBLE);
            holder.contentTextView.setText(tweet.getContent());
        }

        // Show or hide the photos.
        if (tweet.getImages() == null) {
            holder.photoGridView.setVisibility(View.GONE);
        } else {
            holder.photoGridView.setVisibility(View.VISIBLE);

            int columnNum = getColumnNum(tweet.getImages().size());
            holder.photoGridView.setNumColumns(columnNum);

            ViewGroup.LayoutParams params = holder.photoGridView.getLayoutParams();
            if (columnNum == 1) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                // Change the grid width to be exactly the items width (including the space).
                params.width = DisplayUtils.dp2px(parent.getContext(), 100) * columnNum + DisplayUtils.dp2px(parent
                        .getContext(), 4) * (columnNum - 1);
            }
            if (holder.photoGridView.getAdapter() == null) {
                PhotoGridAdapter adapter = new PhotoGridAdapter(tweet.getImages());
                holder.photoGridView.setAdapter(adapter);
            } else {
                PhotoGridAdapter adapter = (PhotoGridAdapter) holder.photoGridView.getAdapter();
                adapter.replaceData(tweet.getImages());
            }
        }

        // Show or hide the comments.
        if (tweet.getComments() == null) {
            holder.commentContainer.setVisibility(View.GONE);
        } else {
            holder.commentContainer.setVisibility(View.VISIBLE);
            showComments(parent.getContext(), tweet.getComments(), holder.commentContainer);
        }
        return convertView;
    }

    /**
     * Get the column number of the photo grid according to the image size.
     * @param imageSize the image size
     * @return 1 if the image size is 1, 2 if the image size is 2 or 4, otherwise is 3
     */
    private int getColumnNum(int imageSize) {
        if (imageSize == 1) {
            return 1;
        }
        int rowNumWithTwoColumn = (int) Math.ceil(imageSize / 2.0);
        int rowNumWithThreeColumn = (int) Math.ceil(imageSize / 3.0);
        return rowNumWithTwoColumn <= rowNumWithThreeColumn ? 2 : 3;
    }

    /**
     * Show comments view.
     *
     * @param context          the context
     * @param comments         the comments
     * @param commentContainer the container view
     */
    private void showComments(Context context, List<Comment> comments, ViewGroup commentContainer) {
        TextView commentTextView;
        Comment comment;
        int commentSize = comments.size();
        int childCount = commentContainer.getChildCount();

        for (int i = 0; i < commentSize; i++) {
            if (i < childCount) {
                // Reuse the TextView.
                commentTextView = (TextView) commentContainer.getChildAt(i);
                commentTextView.setVisibility(View.VISIBLE);
            } else {
                // Create a new TextView.
                commentTextView = (TextView) LayoutInflater.from(context).inflate(R.layout.comment, commentContainer,
                        false);
                commentContainer.addView(commentTextView);
            }
            comment = comments.get(i);
            commentTextView.setText(context.getString(R.string.comment, comment.getSender().getNickname(), comment
                    .getContent()));
        }

        // Hide the unused TextView.
        for (int i = commentSize; i < childCount; i++) {
            commentContainer.getChildAt(i).setVisibility(View.GONE);
        }
    }

    private class ViewHolder {
        ImageView avatarImageView;
        TextView nameTextView;
        TextView contentTextView;
        GridView photoGridView;
        ViewGroup commentContainer;
    }
}
