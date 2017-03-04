import java.util.ArrayList;
import java.util.List;

/**
 * Created by vorona on 22.03.16.
 */
abstract public class ThreadCreator<T, U> {
    public List<Thread> createThreads(int i, List<? extends T> list, List<U> res) {

        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ArrayList<U>> th_res = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            th_res.add(new ArrayList<>());
        }
        double cnt = Math.ceil(((double) list.size() / i));
        int shift = 0;
        for (int z = 0; z < i; z++) {
            final  int sh = shift, j = z, end = Math.min(list.size(), shift+(int)cnt);
            Thread nt = new Thread(() -> {
                if (sh >= list.size()) return;
                threadAct(list.subList(sh, end), th_res.get(j));
            });
            shift += cnt;
            threads.add(nt);
        }
        start(threads);
        if (res != null) {
                th_res.forEach(res::addAll);
            }
        return threads;
    }
    abstract void threadAct(List<? extends T> list, List<U> res);

    private void start(List<Thread> ar) {
        for (Thread t : ar) {
            t.start();
        }
        for (Thread t : ar) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("Has been Interrupted! :( ");
            }
        }
    }
}
