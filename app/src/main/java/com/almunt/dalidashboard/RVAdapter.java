/*
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        personViewHolder.name.setText(daliMember.getName());
        personViewHolder.website.setText(daliMember.getUrl());
        personViewHolder.message.setText(daliMember.getMessage());
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