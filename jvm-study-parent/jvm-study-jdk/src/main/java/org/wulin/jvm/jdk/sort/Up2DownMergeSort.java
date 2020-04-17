package org.wulin.jvm.jdk.sort;

/**
 * 自顶向下归并排序
 *<p> 将一个大数组分成两个小数组去求解。
 *<p> 因为每次都将问题对半分成两个子问题，这种对半分的算法复杂度一般为 O(NlogN)。
 * @author ThinkPad
 *
 * @param <T>
 */
public class Up2DownMergeSort<T extends Comparable<T>> extends MergeSort<T> {

    @Override
    public void sort(T[] nums) {
        aux = (T[]) new Comparable[nums.length];
        sort(nums, 0, nums.length - 1);
    }

    /**
     * 
     * @param nums 要排序的数组
     * @param l -> low 数组的最地位索引
     * @param h -> high 数组的最高为索引
     */
    private void sort(T[] nums, int l, int h) {
        if (h <= l) {
            return;
        }
        int mid = l + (h - l) / 2;
        sort(nums, l, mid);
        sort(nums, mid + 1, h);
        merge(nums, l, mid, h);
    }
}