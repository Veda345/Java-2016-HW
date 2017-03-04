import java.util.Comparator;
import java.util.List;

/**
 * Created by vorona on 22.03.16.
 */
public class FindMin <T, U> extends ThreadCreator<T, U> {
    private Comparator<T> comparator;

    protected FindMin(Comparator comparator) {
        this.comparator = comparator;
    }
    @Override
    void threadAct(List<? extends T> list, List<U> res) {
        if (list == null) return;
        T min = list.get(0);
        for (T elem: list) {
            if (comparator.compare(elem, min) < 0) {
                min = elem;
            }
        }
        synchronized (res) {
            res.add((U)min);
        }
    }
}
