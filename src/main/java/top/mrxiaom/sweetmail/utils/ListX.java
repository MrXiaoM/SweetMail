package top.mrxiaom.sweetmail.utils;

import org.jetbrains.annotations.Range;

import java.util.ArrayList;

public class ListX<T> extends ArrayList<T> {
    private int maxCount = 0;
    public ListX() {}
    public ListX(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    @Range(from=0, to=Integer.MAX_VALUE)
    public int getMaxPage(int perPage) {
        if (maxCount == 0) return 0;
        if (maxCount == -1) return 1;
        return Math.max(1, (int)Math.ceil((double)maxCount / perPage));
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
}
