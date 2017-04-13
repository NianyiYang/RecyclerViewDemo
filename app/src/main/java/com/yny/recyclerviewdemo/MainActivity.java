package com.yny.recyclerviewdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yny.recyclerviewdemo.recycler.CanRefreshLayout;
import com.yny.recyclerviewdemo.recycler.DYRefreshFooter;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.yny.recyclerviewdemo.R.id.refresh_container;

public class MainActivity extends AppCompatActivity implements CanRefreshLayout.OnRefreshListener, DYRefreshFooter.OnLoadMoreListener {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private CanRefreshLayout mRefreshContainer;
    private LinearLayoutManager mLayoutManager;
    private Adapter<String> mAdapter;
    private DYRefreshFooter mFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRefreshContainer = (CanRefreshLayout) findViewById(refresh_container);
        mRecyclerView = (RecyclerView) findViewById(R.id.content_view);
        mFooter = (DYRefreshFooter) findViewById(R.id.footer);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRefreshContainer.setOnRefreshListener(this);

        mRefreshContainer.setMaxFooterHeight(100);
        mRefreshContainer.setStyle(0, 0); // classic

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter<>(this);

        for (int i = 0; i < 20; i++) {
            mAdapter.add(i, i + "");
        }

        mRecyclerView.setAdapter(mAdapter);

        mFooter.attachTo(mRecyclerView);
        mFooter.setLoadMoreListener(this);
    }

    @Override
    public void onRefresh() {

        mRefreshContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.add(0, "refresh");
                mRefreshContainer.refreshComplete();
            }
        }, 1000);

    }

    @Override
    public void onLoadMore() {

        mRefreshContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.add("more1");
                mAdapter.add("more2");
                mAdapter.add("more3");
                mAdapter.add("more4");
                mAdapter.add("more5");
                mFooter.loadMoreComplete();
            }
        }, 1000);

    }

    public class Adapter<T> extends RecyclerView.Adapter<ViewHolder> {

        private Context mContext;

        private AbstractList<T> mItems;

        public Adapter(Context context) {
            mContext = context;
            mItems = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recycle_view, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setView((String) mItems.get(position));
        }

        public void add(T t) {
            mItems.add(t);
            notifyDataSetChanged();
        }

        public void add(int index, T t) {
            mItems.add(index, t);
            notifyDataSetChanged();
        }

        public void addAll(Collection<? extends T> collection) {
            if (collection != null) {
                mItems.addAll(collection);
                notifyDataSetChanged();
            }
        }

        @SafeVarargs
        public final void addAll(T... items) {
            addAll(Arrays.asList(items));
        }

        public void clear() {
            mItems.clear();
            notifyDataSetChanged();
        }

        public void remove(String object) {
            mItems.remove(object);
            notifyDataSetChanged();
        }

        public T getItem(int position) {
            return mItems.get(position);
        }

        public List<T> getItems() {
            return mItems;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mText;
        private final TextView mSubText;

        ViewHolder(View view) {
            super(view);
            mText = (TextView) view.findViewById(R.id.text);
            mSubText = (TextView) view.findViewById(R.id.sub_text);
        }

        public void setView(String tag) {
            mText.setText("标题" + tag);
            mSubText.setText("副标题");
        }
    }
}
