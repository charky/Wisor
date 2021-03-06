package com.enterprise.charky.wisor.util;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.enterprise.charky.wisor.R;

/**
 * Created by charky on 13.12.15.
 * Adapter for Interpreting an simple String-Array
 * and Handling On and Off Action by one button each
 */
public class SwitchAdapter extends RecyclerView.Adapter<SwitchAdapter.ViewHolder> implements View
        .OnClickListener, View.OnLongClickListener {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView wsCaption;
        Button btOn;
        Button btOff;
        CardView cv;
        public ViewHolder(View itemView) {
            super(itemView);
            wsCaption = (TextView)itemView.findViewById(R.id.ws_label);
            btOn = (Button)itemView.findViewById(R.id.bt_light_on);
            btOff = (Button)itemView.findViewById(R.id.bt_light_off);
            cv = (CardView) itemView.findViewById(R.id.ws_cardView);
        }
    }

    private String[] switchesNames;
    private CardButtonListener cardButtonListener;
    private View clickedView;

    public SwitchAdapter(String[] switchesNames){
        this.switchesNames = switchesNames;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.switch_cardview, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SwitchAdapter.ViewHolder holder, int position) {
        holder.wsCaption.setText(switchesNames[position]);
        holder.btOn.setTag(position);
        holder.btOn.setOnClickListener(this);
        holder.btOff.setTag(position);
        holder.btOff.setOnClickListener(this);
        holder.cv.setOnLongClickListener(this);
        holder.cv.setTag(position);
    }

    @Override
    public void onClick(View v) {
        int wsID = ((int) v.getTag());
        if(cardButtonListener != null){
            cardButtonListener.onCardButtonClick(v,wsID);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int wsID = ((int) v.getTag());
        if(cardButtonListener != null){
            cardButtonListener.onCardViewLongClick(v,wsID);
        }
        return true;
    }

    @Override
    public int getItemCount() {
        if(switchesNames == null) {
            return 0;
        }
        return switchesNames.length;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setCardButtonListener(CardButtonListener cbl){
        cardButtonListener = cbl;
    }

    public interface CardButtonListener {
        void onCardButtonClick(View view, int wsID);
        void onCardViewLongClick(View view, int wsID);
    }

}
