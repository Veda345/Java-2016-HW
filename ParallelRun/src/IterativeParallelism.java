import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

/**
 * This class provides multi-threaded operations on list.
 * Such as finding first minimum/maximum, checking all elements with presicate, applying a function to all elements.
 */
public class IterativeParallelism<T extends Comparable<T>> implements ScalarIP, ListIP {

    /**
     * Creates an IterativeParallelism object.
     */
    public IterativeParallelism() {
    }

    /**
     * Returns first minimum in given list with given comparator
     *
     * @param i          the number of threads to execute this method at one time
     * @param list       list of elements
     * @param comparator given comparator
     * @return first minimum in {@code list}
     */
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        ArrayList<T> res = new ArrayList<>();
        (new FindMin<T, T>(comparator)).createThreads(i, list, res);
        T min = res.get(0);
        for (T elem : res) {
            if (comparator.compare(elem, min) < 0) {
                min = elem;
            }
        }
        return min;
    }

    /**
     * Returns first maximum in given list with given comparator
     *
     * @param i          the number of threads to execute this method at one time
     * @param list       list of elements
     * @param comparator given comparator
     * @return first maximum in {@code list}
     */
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Comparator<T> comp = (o1, o2) -> -comparator.compare(o1, o2);
        return minimum(i, list, comp);
    }

    /**
     * Checks, if all elements in given list satisfy the given predicate.
     *
     * @param i         the number of threads to execute this method at one time
     * @param list      list of elements
     * @param predicate given predicate
     * @return {@code true} if all elements in {@code list} satisfy the predicate, {@code false} overwise.
     */
    public <T> boolean all(int i, List<? extends T> list, java.util.function.Predicate<? super T> predicate) throws InterruptedException {
        Predicate<? super T> pred = t -> !predicate.test(t);
        return !any(i, list, pred);
    }

    /**
     * Checks, if any element in given list satisfies the given predicate.
     *
     * @param i         the number of threads to execute this method at one time
     * @param list      list of elements
     * @param predicate given predicate
     * @return {@code true} if exists element in {@code list} which satisfies the predicate, {@code false} overwise.
     */
    public <T> boolean any(int i, List<? extends T> list, java.util.function.Predicate<? super T> predicate) throws InterruptedException {
        ArrayList<T> res = new ArrayList<>();
        (new CheckList<T, T>(predicate)).createThreads(i, list, res);
        return (res.size() > 0);
    }

    /**
     * Returns list containing all elements of given list satisfying the predicate.
     *
     * @param i         the number of threads to execute this method at one time
     * @param list      list of elements
     * @param predicate given predicate
     * @return Returns list containing all elements of {@code list} which satisfy the {@code predicate}.
     */
    @Override
    public <T> List<T> filter(int i, List<? extends T> list, java.util.function.Predicate<? super T> predicate) throws InterruptedException {
        ArrayList<T> res = new ArrayList<>();
        (new FilList<T, T>(predicate)).createThreads(i, list, res);
        return res;
    }

    /**
     * Returns list containing all elements of given list satisfying the predicate.
     *
     * @param i        the number of threads to execute this method at one time
     * @param list     list of elements
     * @param function given function
     * @return Returns list containing results of applying {@code function} to all elements of the {@code list}.
     */
    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, java.util.function.Function<? super T, ? extends U> function) throws InterruptedException {
        ArrayList<U> res = new ArrayList<>();
        (new FilList<>(function)).createThreads(i, list, res);
        return res;
    }

    /**
     * Returns String containing all elements of given list in their string view concatinated  list.
     *
     * @param i    the number of threads to execute this method at one time
     * @param list list of elements
     * @return Returns list containing all elements of {@code list} in their string view in their order in the {@code list}.
     */
    public String join(int i, List<?> list) throws InterruptedException {
        ArrayList<String> res = new ArrayList<>();
        (new StringJoin<Object, String>()).createThreads(i, list, res);
        StringBuilder sb = new StringBuilder();
        for (Object elem: list) {
            sb.append(elem.toString());
        }
        return sb.toString();
    }
}
