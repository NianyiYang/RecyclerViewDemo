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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Adapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter<>(this);

        for (int i = 0; i < 10; i++) {
            mAdapter.add(i, "");
        }

        mRecyclerView.setAdapter(mAdapter);
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
            holder.setView();
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
            setContentView(view);
            mText = (TextView) view.findViewById(R.id.text);
            mSubText = (TextView) view.findViewById(R.id.sub_text);
        }

        public void setView() {
            mText.setText("标题");
            mSubText.setText("副标题");
        }


    }
}
