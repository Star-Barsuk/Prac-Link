package com.example.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<
        T,
        B extends ViewBinding,
        VH extends BaseListAdapter.BaseViewHolder<B>> extends BaseAdapter {
    protected final List<T> items;
    protected final LayoutInflater inflater;

    public BaseListAdapter(@NonNull Context context, @NonNull List<T> items) {
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>(items);
    }

    public void updateList(@NonNull List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return items.size(); }
    @Override
    public T getItem(int position) { return items.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final VH holder;
        if (convertView == null) {
            B binding = createBinding(inflater, parent);
            holder = createViewHolder(binding);
            convertView = binding.getRoot();
            convertView.setTag(holder);
        } else {
            holder = (VH) convertView.getTag();
        }
        bindViewHolder(holder, position);
        return convertView;
    }

    @NonNull
    protected abstract B createBinding(@NonNull LayoutInflater inflater,@NonNull ViewGroup parent);
    @NonNull
    protected abstract VH createViewHolder(@NonNull B binding);
    protected abstract void bindViewHolder(@NonNull VH holder, int position);

    public static abstract class BaseViewHolder<B extends ViewBinding> {
        @NonNull
        protected final B binding;

        protected BaseViewHolder(@NonNull B binding) {
            this.binding = binding;
        }
    }
}