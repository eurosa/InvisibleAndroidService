package com.googleapi.invisible;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private List<AppInfo> appList;
    private OnAppSelectedListener listener;

    public interface OnAppSelectedListener {
        void onAppSelected(AppInfo appInfo);
    }

    public AppListAdapter(List<AppInfo> appList, OnAppSelectedListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo appInfo = appList.get(position);
        holder.bind(appInfo);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView appName;
        private ImageView appIcon;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);

            itemView.setOnClickListener(v -> listener.onAppSelected(appList.get(getAdapterPosition())));
        }

        public void bind(AppInfo appInfo) {
            appName.setText(appInfo.getAppName());
            appIcon.setImageDrawable(appInfo.getAppIcon());
        }
    }
}