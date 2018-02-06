/*
 * Copyright (C) 2018 Alexandru Munteanu
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

/**
 * A RecyclerView adapter that runs all of the individual CardViews in a RecyclerView
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.MembersViewHolder> {
    List<DALIMember> daliMembers;
    Bitmap frameImage;
    Context context;

    /**
     * The constructor for the RVAdapter
     * @param daliMembers A list of DALI members
     * @param frameImage The frame image that is used for all DALI members
     * @param context The context of the MainActivity which is used to an intent for the Member Activity
     */
    RVAdapter(List<DALIMember> daliMembers, Bitmap frameImage, Context context) {
        this.daliMembers = daliMembers;
        this.frameImage = frameImage;
        this.context = context;
    }

    /**
     * Establishes the layout for the CardView
     * @param viewGroup The group holding the CardViews
     * @param i
     * @return
     */
    @Override
    public MembersViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview, viewGroup, false);
        MembersViewHolder membersViewHolder = new MembersViewHolder(v);
        return membersViewHolder;
    }

    /**
     *
     * @param personViewHolder The ViewHolder with the current CardView
     * @param i The position of the CardView
     */
    @Override
    public void onBindViewHolder(MembersViewHolder personViewHolder, int i) {
        final DALIMember daliMember = daliMembers.get(i);
        personViewHolder.name.setText("Name: " + daliMember.getName());
        String url = daliMember.getUrl();
        if (url.startsWith("//"))
            personViewHolder.website.setText("Web: " + url.substring(2));
        else
            personViewHolder.website.setText(("Web: mappy.dali.dartmouth.edu/" + url));
        personViewHolder.message.setText("\"" + daliMember.getMessage() + "\"");
        personViewHolder.icon.setImageBitmap(daliMembers.get(i).getBitmap());
        personViewHolder.frame.setImageBitmap(frameImage);

        // When the CardView of a DALI Member is clicked member data is sent to a Member Activity
        // and shown.
        personViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap temporaryImage = daliMember.getBitmap();
                daliMember.setBitmap(null);
                Intent intent = new Intent(context, MemberActivity.class);
                intent.putExtra("member", new Gson().toJson(daliMember));
                daliMember.setBitmap(temporaryImage);
                context.startActivity(intent);
            }
        });
    }

    /**
     *
     * @return The number of DALI Members in the RecyclerView
     */
    @Override
    public int getItemCount() {
        return daliMembers.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Establishes the variable names of parts of the cardview.xml layout
     */
    public static class MembersViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView website;
        TextView message;
        ImageView icon;
        ImageView frame;
        CardView cardView;

        MembersViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            website = itemView.findViewById(R.id.website);
            message = itemView.findViewById(R.id.message);
            icon = itemView.findViewById(R.id.imageView);
            frame = itemView.findViewById(R.id.imageViewFrame);
            cardView = itemView.findViewById(R.id.card);
        }
    }
}