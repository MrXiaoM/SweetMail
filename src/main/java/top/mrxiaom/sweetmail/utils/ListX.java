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

    /**
     * 获取最大页数。如果无法计算最大数量，即 <code>maxCount == -1</code> 时，则返回 <code>0</code>
     */
    @Range(from=0, to=Integer.MAX_VALUE)
    public int getMaxPage(int perPage) {
        if (maxCount == 0) return 1;
        if (maxCount == -1) return 0;
        return Math.max(1, (int)Math.ceil((double)maxCount / perPage));
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
}
