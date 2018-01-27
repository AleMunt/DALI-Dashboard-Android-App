package com.almunt.dalidashboard;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.MembersViewHolder>{
    List<DALIMember> daliMembers;

    RVAdapter(List<DALIMember> daliMembers)
    {
        this.daliMembers = daliMembers;
    }
    @Override
    public MembersViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview, viewGroup, false);
        MembersViewHolder membersViewHolder = new MembersViewHolder(v);
        return membersViewHolder;
    }
    @Override
    public void onBindViewHolder(MembersViewHolder personViewHolder, int i) {
        DALIMember daliMember=daliMembers.get(i);
        personViewHolder.name.setText(daliMember.GetName());
        personViewHolder.website.setText(daliMember.GetWebsite());
        personViewHolder.message.setText(daliMember.GetMessage());
    }
    @Override
    public int getItemCount()
    {
        return daliMembers.size();
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    public static class MembersViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView website;
        TextView message;
        MembersViewHolder(View itemView) {
//          TODO Add Support for images
            super(itemView);
            name = itemView.findViewById(R.id.name);
            website = itemView.findViewById(R.id.website);
            message = itemView.findViewById(R.id.message);
        }
    }
}