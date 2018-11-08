package com.gnosis.app.Requests;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.gnosis.app.R;

import java.util.List;

/**
 * Created by manel on 10/31/2017.
 */

public class RequestsAdapter extends RecyclerView.Adapter<RequestsViewHolders>{
    private List<RequestsObject> requestList;
    private Context context;


    public RequestsAdapter(List<RequestsObject> requestList, Context context){
        this.requestList = requestList;
        this.context = context;
    }

    @Override
    public RequestsViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requests, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        RequestsViewHolders rcv = new RequestsViewHolders(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(RequestsViewHolders holder, int position) {
        holder.mMatchId.setText(requestList.get(position).getUserId());
        holder.mMatchName.setText(requestList.get(position).getName());
        if(!requestList.get(position).getProfileImageUrl().equals("default")){
            Glide.with(context).load(requestList.get(position).getProfileImageUrl()).into(holder.mMatchImage);
        }
    }

    @Override
    public int getItemCount() {
        return this.requestList.size();
    }
}
