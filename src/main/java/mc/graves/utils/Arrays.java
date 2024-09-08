package mc.graves.utils;

public class Arrays {

    public static <T> void reverse(T[] array) {
        for(int i = 0; i < array.length / 2; i++) {
            T tmp = array[i];
            int oppositIndex = array.length - 1 - i;
            array[i]= array[oppositIndex];
            array[oppositIndex]= tmp;
        }
    }

}
