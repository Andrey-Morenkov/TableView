/*
 * Copyright (c) 2018. Evren Coşkun
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.evrencoskun.tableview.adapter.recyclerview;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by evrencoskun on 10/06/2017.
 */

public abstract class AbstractRecyclerViewAdapter<T> extends RecyclerView
        .Adapter<AbstractViewHolder> {

    @NonNull
    protected List<T> mItemList;

    @NonNull
    protected Context mContext;

    public AbstractRecyclerViewAdapter(@NonNull Context context) {
        this(context, null);
    }

    public AbstractRecyclerViewAdapter(@NonNull Context context, @Nullable List<T> itemList) {
        mContext = context;

        if (itemList == null) {
            mItemList = new ArrayList<>();
        } else {
            setItems(itemList);
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @NonNull
    public List<T> getItems() {
        return mItemList;
    }

    public void setItems(@NonNull List<T> itemList) {
        mItemList = new ArrayList<>(itemList);

        this.notifyDataSetChanged();
    }

    public void setItems(@NonNull List<T> itemList, boolean notifyDataSet) {
        mItemList = new ArrayList<>(itemList);

        if (notifyDataSet) {
            this.notifyDataSetChanged();
        }
    }

    @Nullable
    public T getItem(int position) {
        if (mItemList.isEmpty() || position < 0 || position >= mItemList.size()) {
            return null;
        }
        return mItemList.get(position);
    }

    public void deleteItem(int position) {
        if (position != RecyclerView.NO_POSITION) {
            mItemList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void deleteSortedItems(List<Integer> positions) {
        Collections.reverse(positions);
        Log.e("AbstractRVAdapter", "reversed: " + positions.toString());

        int currPositionsPos = 0;
        Log.e("AbstractRVAdapter", "deleteSortedItems: positionsSize = " + positions.size() + ", mItemList.size() = " + mItemList.size());
        for (int i = mItemList.size() - 1; i >= 0; i--) {
            Log.e("AbstractRVAdapter", "i = " + i + ", pos = " + positions.get(currPositionsPos));
            if (i == positions.get(currPositionsPos)) {
                mItemList.remove(i);
                Log.e("AbstractRVAdapter", "remove item " + i);
                notifyItemRemoved(i);
                currPositionsPos++;
                if (currPositionsPos == positions.size()) {
                    Log.e("AbstractRVAdapter", "break!!1");
                    break;
                }
            }
        }
    }

    public void deleteItemRange(int positionStart, int itemCount) {
        for (int i = positionStart + itemCount - 1; i >= positionStart; i--) {
            if (i != RecyclerView.NO_POSITION) {
                mItemList.remove(i);
            }
        }

        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public void addItem(int position, @Nullable T item) {
        if (position != RecyclerView.NO_POSITION && item != null) {
            mItemList.add(position, item);
            notifyItemInserted(position);
        }
    }

    public void addItems(Map<Integer, T> positionsWithItems) {
        for (Map.Entry<Integer, T> positionAndItem: positionsWithItems.entrySet()) {
            if (positionAndItem.getKey() != RecyclerView.NO_POSITION) {
                Log.e("AbstractRVAdapter", "add item (" + positionAndItem.getKey() + ")");
                mItemList.add(positionAndItem.getKey(), positionAndItem.getValue());
                notifyItemInserted(positionAndItem.getKey());
            }
        }
    }

    public void addItemRange(int positionStart, @Nullable List<T> items) {
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                mItemList.add((i + positionStart), items.get(i));
            }

            notifyItemRangeInserted(positionStart, items.size());
        }
    }

    public void changeItem(int position, @Nullable T item) {
        if (position != RecyclerView.NO_POSITION && item != null) {
            mItemList.set(position, item);
            notifyItemChanged(position);
        }
    }

    public void changeItemRange(int positionStart, @Nullable List<T> items) {
        if (items != null && mItemList.size() > positionStart + items.size()) {
            for (int i = 0; i < items.size(); i++) {
                mItemList.set(i + positionStart, items.get(i));
            }
            notifyItemRangeChanged(positionStart, items.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }
}
