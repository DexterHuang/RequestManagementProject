package com.dexter.requestmanagement;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dexter.requestmanagement.Models.Request;
import com.dexter.requestmanagement.Models.RequestStatusType;
import com.dexter.requestmanagement.requestListFragment.OnListFragmentInteractionListener;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<Request> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyItemRecyclerViewAdapter(List<Request> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Request request = mValues.get(position);
        holder.mItem = mValues.get(position);
        holder.buildingNameTextView.setText(request.getBuildingNumber());
        holder.roomTextView.setText(request.getRoomNumber());
        RequestStatusType status = request.getStatus();
        if (status == RequestStatusType.PENDING) {
            holder.statusImageView.setImageResource(R.drawable.pending);
        } else if (status == RequestStatusType.PROCESSING) {
            holder.statusImageView.setImageResource(R.drawable.processing);
        } else if (status == RequestStatusType.DONE) {
            holder.statusImageView.setImageResource(R.drawable.assigned);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView roomTextView;
        public final TextView buildingNameTextView;
        public final ImageView statusImageView;
        public Request mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            roomTextView = (TextView) view.findViewById(R.id.RoomTextView);
            buildingNameTextView = (TextView) view.findViewById(R.id.BuildingTextView);
            statusImageView = (ImageView) view.findViewById(R.id.StatusImageView);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + roomTextView.getText() + "'";
        }
    }
}
