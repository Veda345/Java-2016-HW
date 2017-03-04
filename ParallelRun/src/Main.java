import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vorona on 18.03.16.
 */
public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(4, 100, 7, 1, 100, 14, -31, -99, -102, 87, 100));
        IterativeParallelism<Integer> ip = new IterativeParallelism<Integer>();
        Comparator<Integer> comparator = Comparator.naturalOrder();
        java.util.function.Predicate<Integer> predicate = o -> {
            return o>0;
        };
        try {
//            System.out.println(ip.minimum(2, list, comparator));

            for (int j = 0; j < 10; j++) {
                List<Integer> ar = ip.filter(j+1, list, predicate);
                for (int a : ar) {
                    System.out.print(a + " ");

                }
                System.out.println();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
