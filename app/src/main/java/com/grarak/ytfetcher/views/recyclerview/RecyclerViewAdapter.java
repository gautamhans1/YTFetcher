package com.grarak.ytfetcher.views.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

public class RecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List<RecyclerViewItem<VH>> items;

    public RecyclerViewAdapter(List<RecyclerViewItem<VH>> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        RecyclerViewItem<VH> item = items.get(position);
        return item.createViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        item.getLayoutXml(), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        items.get(position).bindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
