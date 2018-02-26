package com.trista.wechatmoments.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.trista.wechatmoments.R;
import com.trista.wechatmoments.adapter.TweetListAdapter;
import com.trista.wechatmoments.entity.Account;
import com.trista.wechatmoments.entity.Tweet;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import in.srain.cube.views.loadmore.LoadMoreContainer;
import in.srain.cube.views.loadmore.LoadMoreHandler;
import in.srain.cube.views.loadmore.LoadMoreListViewContainer;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import in.srain.cube.views.ptr.util.PtrLocalDisplay;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * WeChats Moments main Activity.
 */
public class MainActivity extends AppCompatActivity {
    private TextView mNameTextView;
    private ImageView mAvatarImageView;
    private ImageView mProfileImageView;

    /**
     * Pull down to refresh.
     */
    private PtrFrameLayout mPtrFrameLayout;
    /**
     * Pull up to load more.
     */
    private LoadMoreListViewContainer mLoadMoreListViewContainer;

    private TweetListAdapter mAdapter;
    /**
     * All tweets loaded first time.
     */
    private List<Tweet> mTweets = new ArrayList<>();
    /**
     * The tweet page, begins from 0.
     */
    private int mPage;
    /**
     * The number of tweet for each page.
     */
    private static final int PAGE_TWEET_NUM = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the tweet list.
        final ListView listView = (ListView) findViewById(R.id.tweet_list);
        View view = LayoutInflater.from(this).inflate(R.layout.list_header_tweet, listView, false);
        mNameTextView = (TextView) view.findViewById(R.id.name);
        mAvatarImageView = (ImageView) view.findViewById(R.id.avatar_image);
        mProfileImageView = (ImageView) view.findViewById(R.id.profile_image);
        listView.addHeaderView(view);
        mAdapter = new TweetListAdapter(new ArrayList<Tweet>());
        listView.setAdapter(mAdapter);

        // Initialize the refresh view for pulling down.
        mPtrFrameLayout = (PtrFrameLayout) findViewById(R.id.moment_ptr_frame);
        MaterialHeader header = new MaterialHeader(this);
        int[] colors = {Color.BLUE, Color.GREEN};
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, PtrLocalDisplay.dp2px(15), 0, PtrLocalDisplay.dp2px(10));
        header.setPtrFrameLayout(mPtrFrameLayout);

        mPtrFrameLayout.setLoadingMinTime(1000);
        mPtrFrameLayout.setDurationToCloseHeader(1500);
        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.addPtrUIHandler(header);
        mPtrFrameLayout.setPinContent(true);

        mPtrFrameLayout.setPtrHandler(new PtrHandler() {

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, listView, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                loadAllTweets();
            }
        });

        // Initialize the refresh view for pulling up.
        mLoadMoreListViewContainer = (LoadMoreListViewContainer) findViewById(R.id.tweet_load_more_container);
        mLoadMoreListViewContainer.useDefaultHeader();
        mLoadMoreListViewContainer.setLoadMoreHandler(new LoadMoreHandler() {
            @Override
            public void onLoadMore(LoadMoreContainer loadMoreContainer) {
                if ((mPage + 1) * PAGE_TWEET_NUM < mTweets.size()) {
                    LoadMoreTweets();
                } else {
                    mLoadMoreListViewContainer.loadMoreFinish(false, false);
                }
            }
        });

        loadAccount();
        mPtrFrameLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPtrFrameLayout.autoRefresh();
            }
        }, 100);
    }

    /**
     * Load the account.
     */
    private void loadAccount() {
        Flowable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://thoughtworks-ios.herokuapp.com/user/jsmith")
                        .build();

                Response response = client.newCall(request).execute();
                return new Gson().fromJson(response.body().string(), Account.class);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Account>() {
                    @Override
                    public void accept(Account account) throws Exception {
                        mNameTextView.setText(account.getNickname());
                        Picasso.with(MainActivity.this)
                                .load(account.getAvatar())
                                .resize(200, 200)
                                .centerCrop()
                                .into(mAvatarImageView);
                        Picasso.with(MainActivity.this)
                                .load(account.getProfile())
                                .resize(900, 500)
                                .centerCrop()
                                .error(R.drawable.profile_placeholder)
                                .into(mProfileImageView);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        // Show the error view.
                        Toast.makeText(MainActivity.this, R.string.error_load_account, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Load all tweets.
     */
    private void loadAllTweets() {
        final List<Tweet> tweets = new ArrayList<>();
        Flowable.fromCallable(new Callable<List<Tweet>>() {
            @Override
            public List<Tweet> call() throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://thoughtworks-ios.herokuapp.com/user/jsmith/tweets")
                        .build();

                Response response = client.newCall(request).execute();
                return new Gson().fromJson(response.body().string(), new TypeToken<List<Tweet>>() {
                }.getType());
            }
        })
                .flatMap(new Function<List<Tweet>, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(@NonNull List<Tweet> tweets) throws Exception {
                        return Flowable.fromArray(tweets.toArray(new Tweet[0]));
                    }
                })
                .filter(new Predicate<Object>() {
                    @Override
                    public boolean test(@NonNull Object object) throws Exception {
                        // Filter the tweet which has no content and image.
                        Tweet tweet = (Tweet) object;
                        return tweet.getContent() != null || tweet.getImages() != null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object tweet) throws Exception {
                        tweets.add((Tweet) tweet);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        // Show the error view.
                        Toast.makeText(MainActivity.this, R.string.error_load_tweets, Toast.LENGTH_SHORT).show();
                        mPtrFrameLayout.refreshComplete();
                        mLoadMoreListViewContainer.loadMoreFinish(false, true);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        mPage = 0;
                        mTweets=tweets;
                        mAdapter.resetData(mTweets.subList(0, PAGE_TWEET_NUM));
                        mPtrFrameLayout.refreshComplete();
                        mLoadMoreListViewContainer.loadMoreFinish(false, true);
                    }
                });
    }

    /**
     * Load tweets of the next page.
     */
    private void LoadMoreTweets() {
        final List<Tweet> tweets = new ArrayList<>();
        Flowable.fromArray(mTweets.toArray(new Tweet[0]))
                .skip((mPage+1) * PAGE_TWEET_NUM)
                .take(PAGE_TWEET_NUM)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Tweet>() {
                    @Override
                    public void accept(Tweet tweet) throws Exception {
                        tweets.add(tweet);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mLoadMoreListViewContainer.loadMoreFinish(false, true);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        mPage++;
                        mAdapter.addData(tweets);
                        mLoadMoreListViewContainer.loadMoreFinish(false, true);
                    }
                });
    }
}
