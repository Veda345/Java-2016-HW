import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by vorona on 14.03.16.
 */
public class Main {

    public static void main(String[] args)  {
        System.out.println("ha");
        Implementor impl = new Implementor();
        try {
            Class clazz = Class.forName(args[0]);
            Path path = Paths.get(args[1]);
            impl.implement(clazz, path);
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound");
        }
        catch (ImplerException e) {
            System.out.println("Implementation exception");
        }


    }
}
