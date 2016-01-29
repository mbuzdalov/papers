package ru.ifmo.eps.util;

public class Miscellaneous {
    public static double destructiveKth(double[] array, int left, int right, int index) {
        while (true) {
            double pivot = (array[left] + array[right]) / 2;
            int l = left, r = right;
            while (l <= r) {
                while (array[l] < pivot) ++l;
                while (array[r] > pivot) --r;
                if (l <= r) {
                    double tmp = array[l];
                    array[l] = array[r];
                    array[r] = tmp;
                    ++l;
                    --r;
                }
            }
            if (index <= r) {
                right = r;
            } else if (l <= index) {
                left = l;
            } else {
                return array[index];
            }
        }
    }

    public static double destructiveMedian(double[] array, int left, int right) {
        return destructiveKth(array, left, right, (left + right) >>> 1);
    }
}
