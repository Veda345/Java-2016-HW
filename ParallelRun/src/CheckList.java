import java.util.List;

/**
 * Created by vorona on 23.03.16.
 */
public class CheckList<T, U> extends ThreadCreator<T, U> {
    private java.util.function.Predicate<? super T> predicate;

    protected CheckList(java.util.function.Predicate<? super T> predicate) {
        this.predicate = predicate;
    }

    @Override
    void threadAct(List<? extends T> list, List<U> res) {
        if (list == null) return;
        if (list.stream().anyMatch(predicate)) {
            synchronized (res) {
                res.add(null);
            }
        }
    }
}
