package top.mrxiaom.sweetmail.utils;

import java.util.ArrayList;

public class ListX<T> extends ArrayList<T> {
    private int maxCount = 0;

    public int getMaxCount() {
        return maxCount;
    }

    public int getMaxPage(int perPage) {
        return Math.max(1, (int)Math.ceil((double)maxCount / perPage));
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
}
