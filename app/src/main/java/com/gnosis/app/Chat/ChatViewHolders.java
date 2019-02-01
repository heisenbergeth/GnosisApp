package com.gnosis.app.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gnosis.app.R;

/**
 * Created by manel on 10/31/2017.
 */

public class ChatViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView mMessage;
    public RelativeLayout mContainer;
    public LinearLayout mContainer1;
    public int color;
    public int mBlue;
    public int mGray;

    public ChatViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mBlue = ContextCompat.getColor(itemView.getContext(), R.color.chat1);
        mGray = ContextCompat.getColor(itemView.getContext(), R.color.chat2);

        mMessage = itemView.findViewById(R.id.message);
        mContainer = itemView.findViewById(R.id.container);
        mContainer1 = itemView.findViewById(R.id.container1);
    }

    @Override
    public void onClick(View view) {
    }
}
