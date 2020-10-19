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

package com.evrencoskun.tableview.handler;

import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.evrencoskun.tableview.ITableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by evrencoskun on 24.12.2017.
 */

public class VisibilityHandler {
    private static final String LOG_TAG = VisibilityHandler.class.getSimpleName();

    @NonNull
    private ITableView mTableView;
    @NonNull
    private SparseArray<Row> mHideRowList = new SparseArray<>();
    @NonNull
    private SparseArray<Column> mHideColumnList = new SparseArray<>();

    public VisibilityHandler(@NonNull ITableView tableView) {
        this.mTableView = tableView;
    }

    public void hideRow(int row) {
        int viewRow = convertIndexToViewIndex(row, mHideRowList);

        if (mHideRowList.get(row) == null) {
            // add row the list
            mHideRowList.put(row, getRowValueFromPosition(row));

            // remove row model from adapter
            mTableView.getAdapter().removeRow(viewRow);
        } else {
            Log.e(LOG_TAG, "This row is already hidden.");
        }
    }

    public void hideSortedRows(List<Integer> rows) {
        for (int row: rows) {
            if (mHideRowList.get(row) == null) {
                // add row the list
                mHideRowList.put(row, getRowValueFromPosition(row));
            } else {
                Log.e(LOG_TAG, "This row (" + row + ") is already hidden.");
            }
        }

        Log.e(LOG_TAG, "add rows to hide row list: " + rows.toString());

        // remove row models from adapter
        mTableView.getAdapter().removeSortedRows(rows);
    }

    public void showRow(int row) {
        showRow(row, true);
    }

    private void showRow(int row, boolean removeFromList) {
        Row hiddenRow = mHideRowList.get(row);

        if (hiddenRow != null) {
            // add row model to the adapter
            mTableView.getAdapter().addRow(row, hiddenRow.getRowHeaderModel(),
                    hiddenRow.getCellModelList());
        } else {
            Log.e(LOG_TAG, "This row is already visible.");
        }

        if (removeFromList) {
            mHideRowList.remove(row);
        }
    }

    public void clearHideRowList() {
        mHideRowList.clear();
    }

    public void showAllHiddenRows() {
        Log.e(LOG_TAG, "show all hidden rows (" + mHideRowList.size() + ")");
        for (int i = 0; i < mHideRowList.size(); i++) {
            int row = mHideRowList.keyAt(i);
            showRow(row, false);
        }

        clearHideRowList();
    }

    public boolean isRowVisible(int row) {
        return mHideRowList.get(row) == null;
    }

    public void hideColumn(int column) {
        int viewColumn = convertIndexToViewIndex(column, mHideColumnList);

        if (mHideColumnList.get(column) == null) {
            // add column the list
            mHideColumnList.put(column, getColumnValueFromPosition(column));

            // remove row model from adapter
            mTableView.getAdapter().removeColumn(viewColumn);
        } else {
            Log.e(LOG_TAG, "This column is already hidden.");
        }
    }

    public void hideSortedColumns(List<Integer> columns) {
        for (int column: columns) {
            if (mHideColumnList.get(column) == null) {
                // add column the list
                mHideColumnList.put(column, getColumnValueFromPosition(column));
            } else {
                Log.e(LOG_TAG, "This column (" + column + ") is already hidden.");
            }
        }

        Log.e(LOG_TAG, "add columns to hide column list: " + columns.toString());

        // remove column models from adapter
        mTableView.getAdapter().removeSortedColumns(columns);
    }

    public void showColumn(int column) {
        showColumn(column, true);
    }

    private void showColumn(int column, boolean removeFromList) {
        Column hiddenColumn = mHideColumnList.get(column);

        if (hiddenColumn != null) {
            // add column model to the adapter
            mTableView.getAdapter().addColumn(column, hiddenColumn.getColumnHeaderModel(),
                    hiddenColumn.getCellModelList());
        } else {
            Log.e(LOG_TAG, "This column is already visible.");
        }

        if (removeFromList) {
            mHideColumnList.remove(column);
        }
    }

    public void showSortedColumns(List<Integer> columns) {
        showSortedColumns(columns, true);
    }

    private void showSortedColumns(List<Integer> columns, boolean removeFromList) {
        TreeMap<Integer, Object> hiddenColumnsHeaderInfo = new TreeMap<>();
        TreeMap<Integer, List<Object>> hiddenColumnsCellsInfo = new TreeMap<>();

        for (int col: columns) {
            Column hiddenColumn = mHideColumnList.get(col);
            if (hiddenColumn != null) {
                hiddenColumnsHeaderInfo.put(col, hiddenColumn.getColumnHeaderModel());
                hiddenColumnsCellsInfo.put(col, hiddenColumn.getCellModelList());
            } else {
                Log.e(LOG_TAG, "This column (" + col + ")is already visible.");
            }
        }

        // add columns model to the adapter
        mTableView.getAdapter().addSortedColumns(hiddenColumnsHeaderInfo, hiddenColumnsCellsInfo);

        if (removeFromList) {
            Collections.reverse(columns);
            for (int col: columns) {
                mHideColumnList.remove(col);
            }
        }
    }

    public void clearHideColumnList() {
        mHideColumnList.clear();
    }

    public void showAllHiddenColumns() {
        List<Integer> columnsToShow = new ArrayList<>(mHideColumnList.size());
        for (int i = 0; i < mHideColumnList.size(); i++) {
            columnsToShow.add(mHideColumnList.keyAt(i));
        }

        Log.e(LOG_TAG, "show all hidden columns (" + columnsToShow.size() + ")");

        Collections.sort(columnsToShow);
        showSortedColumns(columnsToShow, false);
        clearHideColumnList();
    }

    public boolean isColumnVisible(int column) {
        return mHideColumnList.get(column) == null;
    }


    /**
     * Hiding row and column process needs to consider the hidden rows or columns with a smaller
     * index to be able hide the correct index.
     *
     * @param index, stands for column or row index.
     * @param list,  stands for HideRowList or HideColumnList
     */
    private int getSmallerHiddenCount(int index, SparseArray list) {
        int count = 0;
        for (int i = 0; i < index; i++) {
            int key = list.keyAt(i);
            // get the object by the key.
            if (list.get(key) != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * It converts model index to View index considering the previous hidden rows or columns. So,
     * when we add or remove any item of RecyclerView, we need to view index.
     */
    private int convertIndexToViewIndex(int index, SparseArray list) {
        return index - getSmallerHiddenCount(index, list);
    }

    static class Row {
        private int mYPosition;
        @Nullable
        private Object mRowHeaderModel;
        @Nullable
        private List<Object> mCellModelList;

        public Row(int row, @Nullable Object rowHeaderModel, @Nullable List<Object> cellModelList) {
            this.mYPosition = row;
            this.mRowHeaderModel = rowHeaderModel;
            this.mCellModelList = cellModelList;
        }

        public int getYPosition() {
            return mYPosition;
        }

        @Nullable
        public Object getRowHeaderModel() {
            return mRowHeaderModel;
        }

        @Nullable
        public List<Object> getCellModelList() {
            return mCellModelList;
        }

    }

    static class Column {
        private int mYPosition;
        @Nullable
        private Object mColumnHeaderModel;
        @NonNull
        private List<Object> mCellModelList;

        public Column(int yPosition, @Nullable Object columnHeaderModel,
                      @NonNull List<Object> cellModelList) {
            this.mYPosition = yPosition;
            this.mColumnHeaderModel = columnHeaderModel;
            this.mCellModelList = cellModelList;
        }

        public int getYPosition() {
            return mYPosition;
        }

        @Nullable
        public Object getColumnHeaderModel() {
            return mColumnHeaderModel;
        }

        @NonNull
        public List<Object> getCellModelList() {
            return mCellModelList;
        }

    }

    @NonNull
    private Row getRowValueFromPosition(int row) {

        Object rowHeaderModel = mTableView.getAdapter().getRowHeaderItem(row);
        List<Object> cellModelList = (List<Object>) mTableView.getAdapter().getCellRowItems(row);

        return new Row(row, rowHeaderModel, cellModelList);
    }

    @NonNull
    private Column getColumnValueFromPosition(int column) {
        Object columnHeaderModel = mTableView.getAdapter().getColumnHeaderItem(column);
        List<Object> cellModelList =
                (List<Object>) mTableView.getAdapter().getCellColumnItems(column);

        return new Column(column, columnHeaderModel, cellModelList);
    }

    @NonNull
    public SparseArray<Row> getHideRowList() {
        return mHideRowList;
    }

    @NonNull
    public SparseArray<Column> getHideColumnList() {
        return mHideColumnList;
    }

    public void setHideRowList(@NonNull SparseArray<Row> rowList) {
        this.mHideRowList = rowList;
    }

    public void setHideColumnList(@NonNull SparseArray<Column> columnList) {
        this.mHideColumnList = columnList;
    }
}
