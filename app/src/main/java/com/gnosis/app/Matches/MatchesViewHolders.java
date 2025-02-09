package com.gnosis.app.Matches;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gnosis.app.Chat.ChatActivity;
import com.gnosis.app.R;

/**
 * Created by manel on 10/31/2017.
 */

public class MatchesViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView mMatchId, mMatchName;
    public ImageView mMatchImage;
    public MatchesViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchId = (TextView) itemView.findViewById(R.id.Matchid);
        mMatchName = (TextView) itemView.findViewById(R.id.MatchName);

        mMatchImage = (ImageView) itemView.findViewById(R.id.MatchImage);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ChatActivity.class); //pagka-click, start ChatActivity forwarding name and matchID
        Bundle b = new Bundle();
        b.putString("matchId", mMatchId.getText().toString());
        intent.putExtras(b);
        Bundle c = new Bundle();
        c.putString("name", mMatchName.getText().toString()); //ito yung "name" sa ChatActivity, ifoforward yung laman nito sa chat
        intent.putExtras(c);
        view.getContext().startActivity(intent);
    }

}
