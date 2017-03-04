import java.util.List;

/**
 * Created by vorona on 23.03.16.
 */
public class StringJoin<T, U> extends ThreadCreator<T, U> {
    @Override
    void threadAct(List<? extends T> list, List<U> res) {
        if (list == null) return;
            StringBuilder sb = new StringBuilder();
            for (T elem:list) {
                sb.append(elem);
            }
        synchronized (res) {
            res.add((U)sb.toString());
        }
    }
}
