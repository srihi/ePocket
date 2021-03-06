package com.sharad.epocket.cards;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharad.epocket.R;

import java.util.List;

/**
 * Created by Sharad on 12-Sep-15.
 */
public class CardsRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CardItem> mItemList;

    public CardsRecycler(List<CardItem> itemList) {
        mItemList = itemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_list, parent, false);
        view.setMinimumWidth((int)(parent.getMeasuredWidth() * 0.8));
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        DetailViewHolder holder = (DetailViewHolder) viewHolder;
        //holder.setText(mItemList.get(position).get_text());
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    class DetailViewHolder extends RecyclerView.ViewHolder {
        //private TextView _text;


        public DetailViewHolder(final View parent) {
            super(parent);
            //_text = (TextView) parent.findViewById(R.id.dt_text);
        }

        //public void setText(CharSequence text) {   _text.setText(text);   }
    }
}
