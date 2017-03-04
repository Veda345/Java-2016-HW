import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by vorona on 23.03.16.
 */
public class FilList <T, U> extends ThreadCreator<T, U>  {
    private Predicate<? super T> predicate;
    private Function<? super T, ? extends U> function;
    boolean fill;

    protected FilList(Predicate<? super T> predicate) {
        this.predicate = predicate;
        fill = true;
    }

    protected FilList(Function<? super T, ? extends U> function) {
        this.function = function;
    }
    @Override
    void threadAct(List<? extends T> list, List<U> res) {
        if (list == null) return;
        if (fill) {
            list.stream().filter(elem -> predicate.test(elem)).forEach(elem -> {
                synchronized (res) {
                    res.add((U) elem);
                }
            });
        } else {
            list.stream().map(function).forEach(elem -> {
                synchronized (res) {
                    res.add(elem);
                }
            });
        }
    }
}
