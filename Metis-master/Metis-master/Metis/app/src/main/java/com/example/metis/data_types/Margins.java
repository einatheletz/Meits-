package com.example.metis.data_types;

public class Margins {
    int left;

    int top;
    int right;
    int bottom;
    public Margins(int index, int quantity, int framesPerRow,
                   int marginOut, int marginIn) {
        //TODO: fix left&right spacing of last row.
        //get the modulus but if its 0 get row length.
        final int MOD = quantity%framesPerRow == 0 ? framesPerRow : quantity%framesPerRow;
        //boolean to figure if its the last row.
        final boolean bottomRow = quantity - index <= MOD;
        //check if the index is in first column
        this.left = index % framesPerRow == 0 ? marginOut : marginIn;
        //check if the index is in first row
        this.top = index < framesPerRow ? marginOut : marginIn;
        //check if the index is in last column
        this.right = index % framesPerRow == framesPerRow - 1 ? marginOut : marginIn;
        //check if the index is in last row
        this.bottom = bottomRow ? marginOut : marginIn;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }
}
