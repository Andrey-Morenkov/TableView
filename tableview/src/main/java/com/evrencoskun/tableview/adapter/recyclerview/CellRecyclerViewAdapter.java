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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder.SelectionState;
import com.evrencoskun.tableview.handler.ScrollHandler;
import com.evrencoskun.tableview.handler.SelectionHandler;
import com.evrencoskun.tableview.layoutmanager.CellLayoutManager;
import com.evrencoskun.tableview.layoutmanager.ColumnLayoutManager;
import com.evrencoskun.tableview.listener.itemclick.CellRecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by evrencoskun on 10/06/2017.
 */

public class CellRecyclerViewAdapter<C> extends AbstractRecyclerViewAdapter<C> {
    @NonNull
    private ITableView mTableView;

    @NonNull
    private final RecyclerView.RecycledViewPool mRecycledViewPool;

    // This is for testing purpose
    private int mRecyclerViewId = 0;

    public CellRecyclerViewAdapter(@NonNull Context context, @Nullable List<C> itemList, @NonNull ITableView tableView) {
        super(context, itemList);
        this.mTableView = tableView;

        // Create view pool to share Views between multiple RecyclerViews.
        mRecycledViewPool = new RecyclerView.RecycledViewPool();
        //TODO set the right value.
        //mRecycledViewPool.setMaxRecycledViews(0, 110);
    }

    @NonNull
    @Override
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Create a RecyclerView as a Row of the CellRecyclerView
        CellRecyclerView recyclerView = new CellRecyclerView(mContext);

        // Use the same view pool
        recyclerView.setRecycledViewPool(mRecycledViewPool);

        if (mTableView.isShowHorizontalSeparators()) {
            // Add divider
            recyclerView.addItemDecoration(mTableView.getHorizontalItemDecoration());
        }

        // To get better performance for fixed size TableView
        recyclerView.setHasFixedSize(mTableView.hasFixedWidth());

        // set touch mHorizontalListener to scroll synchronously
        recyclerView.addOnItemTouchListener(mTableView.getHorizontalRecyclerViewListener());

        // Add Item click listener for cell views
        if(!mTableView.isAllowClickInsideCell()) {
            recyclerView.addOnItemTouchListener(new CellRecyclerViewItemClickListener(recyclerView,
                    mTableView));
        }

        // Set the Column layout manager that helps the fit width of the cell and column header
        // and it also helps to locate the scroll position of the horizontal recyclerView
        // which is row recyclerView
        recyclerView.setLayoutManager(new ColumnLayoutManager(mContext, mTableView));

        // Create CellRow adapter
        recyclerView.setAdapter(new CellRowRecyclerViewAdapter(mContext, mTableView));

        // This is for testing purpose to find out which recyclerView is displayed.
        recyclerView.setId(mRecyclerViewId);

        mRecyclerViewId++;

        return new CellRowViewHolder(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractViewHolder holder, int yPosition) {
        CellRowViewHolder viewHolder = (CellRowViewHolder) holder;
        CellRowRecyclerViewAdapter viewAdapter = (CellRowRecyclerViewAdapter) viewHolder
                .recyclerView.getAdapter();

        // Get the list
        List<C> rowList = (List<C>) mItemList.get(yPosition);

        // Set Row position
        viewAdapter.setYPosition(yPosition);

        // Set the list to the adapter
        viewAdapter.setItems(rowList);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull AbstractViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        CellRowViewHolder viewHolder = (CellRowViewHolder) holder;

        ScrollHandler scrollHandler = mTableView.getScrollHandler();

        // The below code helps to display a new attached recyclerView on exact scrolled position.
        ((ColumnLayoutManager) viewHolder.recyclerView.getLayoutManager())
                .scrollToPositionWithOffset(scrollHandler.getColumnPosition(), scrollHandler
                        .getColumnPositionOffset());

        SelectionHandler selectionHandler = mTableView.getSelectionHandler();

        if (selectionHandler.isAnyColumnSelected()) {

            AbstractViewHolder cellViewHolder = (AbstractViewHolder) viewHolder.recyclerView
                    .findViewHolderForAdapterPosition(selectionHandler.getSelectedColumnPosition());

            if (cellViewHolder != null) {
                // Control to ignore selection color
                if (!mTableView.isIgnoreSelectionColors()) {
                    cellViewHolder.setBackgroundColor(mTableView.getSelectedColor());
                }
                cellViewHolder.setSelected(SelectionState.SELECTED);

            }
        } else if (selectionHandler.isRowSelected(holder.getAdapterPosition())) {
            selectionHandler.changeSelectionOfRecyclerView(viewHolder.recyclerView,
                    SelectionState.SELECTED, mTableView.getSelectedColor());
        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull AbstractViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        // Clear selection status of the view holder
        mTableView.getSelectionHandler().changeSelectionOfRecyclerView(((CellRowViewHolder)
                holder).recyclerView, SelectionState.UNSELECTED, mTableView.getUnSelectedColor());
    }

    @Override
    public void onViewRecycled(@NonNull AbstractViewHolder holder) {
        super.onViewRecycled(holder);

        CellRowViewHolder viewHolder = (CellRowViewHolder) holder;
        // ScrolledX should be cleared at that time. Because we need to prepare each
        // recyclerView
        // at onViewAttachedToWindow process.
        viewHolder.recyclerView.clearScrolledX();
    }

    static class CellRowViewHolder extends AbstractViewHolder {
        final CellRecyclerView recyclerView;

        CellRowViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = (CellRecyclerView) itemView;
        }
    }

    public void notifyCellDataSetChanged() {
        CellRecyclerView[] visibleRecyclerViews = mTableView.getCellLayoutManager()
                .getVisibleCellRowRecyclerViews();

        if (visibleRecyclerViews.length > 0) {
            for (CellRecyclerView cellRowRecyclerView : visibleRecyclerViews) {
                cellRowRecyclerView.getAdapter().notifyDataSetChanged();
            }
        } else {
            notifyDataSetChanged();
        }

    }

    /**
     * This method helps to get cell item model that is located on given column position.
     *
     * @param columnPosition
     */
    @NonNull
    public List<C> getColumnItems(int columnPosition) {
        List<C> cellItems = new ArrayList<>();

        for (int i = 0; i < mItemList.size(); i++) {
            List<C> rowList = (List<C>) mItemList.get(i);

            if (rowList.size() > columnPosition) {
                cellItems.add(rowList.get(columnPosition));
            }
        }

        return cellItems;
    }


    public void removeColumnItems(int column) {

        // Firstly, remove columns from visible recyclerViews.
        // To be able provide removing animation, we need to notify just for given column position.
        CellRecyclerView[] visibleRecyclerViews = mTableView.getCellLayoutManager()
                .getVisibleCellRowRecyclerViews();

        for (CellRecyclerView cellRowRecyclerView : visibleRecyclerViews) {
            ((AbstractRecyclerViewAdapter) cellRowRecyclerView.getAdapter()).deleteItem(column);
        }


        // Lets change the model list silently
        // Create a new list which the column is already removed.
        List<List<C>> cellItems = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            List<C> rowList = new ArrayList<>((List<C>) mItemList.get(i));

            if (rowList.size() > column) {
                rowList.remove(column);
            }

            cellItems.add(rowList);
        }

        // Change data without notifying. Because we already did for visible recyclerViews.
        setItems((List<C>) cellItems, false);
    }

    public void removeSortedColumnItems(List<Integer> columns) {
        // Firstly, remove columns from visible recyclerViews.
        // To be able provide removing animation, we need to notify just for given column position.

        CellRecyclerView[] visibleRecyclerViews = mTableView.getCellLayoutManager().getVisibleCellRowRecyclerViews();

        for (CellRecyclerView cellRowRecyclerView : visibleRecyclerViews) {
            if (cellRowRecyclerView != null) {
                AbstractRecyclerViewAdapter adapter = (AbstractRecyclerViewAdapter) cellRowRecyclerView.getAdapter();
                if (adapter != null) {
                    adapter.deleteSortedItems(columns);
                }
            }
        }

        // Lets change the model list silently
        // Create a new list which the column is already removed.
        List<Integer> colCopy = new ArrayList<>(columns);
        Collections.reverse(colCopy);

        List<List<C>> cellItems = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            List<C> rowList = new ArrayList<>((List<C>) mItemList.get(i));

            for (int colToRemove: colCopy) {
                try {
                    rowList.remove(colToRemove);
                } catch (IndexOutOfBoundsException e) {
                    Log.e("CellRecyclerViewAdapter", "try to remove " + colToRemove + " column, but size is " + rowList.size());
                }
            }

            cellItems.add(rowList);
        }

        // Change data without notifying. Because we already did for visible recyclerViews.
        setItems((List<C>) cellItems, false);
    }

    public void addColumnItems(int column, @NonNull List<C> cellColumnItems) {
        // It should be same size with exist model list.
        if (cellColumnItems.size() != mItemList.size() || cellColumnItems.contains(null)) {
            return;
        }

        // Firstly, add columns from visible recyclerViews.
        // To be able provide removing animation, we need to notify just for given column position.
        CellLayoutManager layoutManager = mTableView.getCellLayoutManager();
        for (int i = layoutManager.findFirstVisibleItemPosition(); i < layoutManager.findLastVisibleItemPosition() + 1; i++) {
            // Get the cell row recyclerView that is located on i position
            RecyclerView cellRowRecyclerView = (RecyclerView) layoutManager.findViewByPosition(i);

            // Add the item using its adapter.
            ((AbstractRecyclerViewAdapter) cellRowRecyclerView.getAdapter()).addItem(column,
                    cellColumnItems.get(i));
        }


        // Lets change the model list silently
        List<List<C>> cellItems = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            List<C> rowList = new ArrayList<>((List<C>) mItemList.get(i));

            if (rowList.size() > column) {
                rowList.add(column, cellColumnItems.get(i));
            }

            cellItems.add(rowList);
        }

        // Change data without notifying. Because we already did for visible recyclerViews.
        setItems((List<C>) cellItems, false);
    }

    public void addColumnsItems(Map<Integer, List<C>> cellColumnsInfo) {
        List<C> firstColumnInfo = cellColumnsInfo.values().iterator().next();

        // It should be same size with exist model list.
        if (firstColumnInfo.size() != mItemList.size() || firstColumnInfo.contains(null)) {
            Log.e("CellRVAdapter", "firstColumnInfo.size() = " + firstColumnInfo.size() + " != " + mItemList.size());
            return;
        }

        // Firstly, add columns from visible recyclerViews.
        // To be able provide removing animation, we need to notify just for given column position.
        CellLayoutManager layoutManager = mTableView.getCellLayoutManager();
        for (int i = layoutManager.findFirstVisibleItemPosition(); i < layoutManager.findLastVisibleItemPosition() + 1; i++) {
            // Get the cell row recyclerView that is located on i position
            RecyclerView cellRowRecyclerView = (RecyclerView) layoutManager.findViewByPosition(i);

            Map<Integer, C> itemsByPositions = new TreeMap<>();
            for (Map.Entry<Integer, List<C>> entry: cellColumnsInfo.entrySet()) {
                itemsByPositions.put(entry.getKey(), entry.getValue().get(i));
            }

            // Add the item using its adapter.
            ((AbstractRecyclerViewAdapter) cellRowRecyclerView.getAdapter()).addItems(itemsByPositions);
        }

        // Lets change the model list silently
        List<List<C>> cellItems = new ArrayList<>(mItemList.size());
        for (int i = 0; i < mItemList.size(); i++) {
            List<C> rowList = new ArrayList<>((List<C>) mItemList.get(i));

            Map<Integer, C> itemsByPositions = new TreeMap<>();
            for (Map.Entry<Integer, List<C>> entry: cellColumnsInfo.entrySet()) {
                itemsByPositions.put(entry.getKey(), entry.getValue().get(i));
            }

            for (Map.Entry<Integer, C> entry: itemsByPositions.entrySet()) {
                if (entry.getKey() - rowList.size() == 1) {
                    // Add entry value to the end of rowList
                    rowList.add(entry.getValue());
                    break;
                }
                if (rowList.size() >= entry.getKey()) {
                    rowList.add(entry.getKey(), entry.getValue());
                }
            }

            cellItems.add(rowList);
        }

        // Change data without notifying. Because we already did for visible recyclerViews.
        setItems((List<C>) cellItems, false);
    }
}
