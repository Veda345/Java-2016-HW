import info.kgeorgiy.java.advanced.implementor.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class generates non-abstract successor of a given class or interface.
 * Creates implementation of abstract methods returning default values.
 *
 * @author  Veda Voronina
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Output for implemented class
     * @see Writer
     */
    private Writer out;

    /**
     * Contains current implementanion of class
     * @see StringBuilder
     */
    private StringBuilder strB;

    /*
     * Constructor.
     * Creates Implementor Object
     */
    public Implementor() { }


    /**
     * Appends a string of modifiers of given class
     *
     * @param clazz a given class
     *
     * @see Class
     */
    private void classMod(Class clazz){
        int modifiers = clazz.getModifiers();
        String str = getModifiers(modifiers);
        if (str != null) {
            str = str.replace("abstract ", "");
            strB.append(str);
        }
    }

    /**
     * Appends a string of constructors of given class
     *
     * @throws ImplerException then given class has only private constructors
     *
     * @param clazz a given class
     *
     * @see Class
     */
    private void classConstr(Class clazz) throws ImplerException {
        boolean f = true;
        for (Constructor c : clazz.getDeclaredConstructors()) {
            if (Modifier.isPrivate(c.getModifiers()))
                continue;
            f = false;
            strB.append("\t" + getModifiers(c.getModifiers()).replace("abstract ", ""));
            strB.append(clazz.getSimpleName() + "Impl(" + getParameters(c.getParameterTypes()) + ")");
            if (c.getExceptionTypes().length > 0) {
                strB.append(" throws ");
                Class[] exc = c.getExceptionTypes();
                for (int i = 0; i < exc.length; i++) {
                    if (i > 0) strB.append(", ");
                    strB.append(exc[i].getName());
                }
            }
            strB.append("{ ");
            strB.append("super(" + getParameterNames(c.getParameterTypes()) + ");");
            strB.append("}\n");
        }
        if (f && !clazz.isInterface()) throw new ImplerException();
    }


    /**
     * Writes implementation of given class in StringBuilder
     *
     * @throws ImplerException then given a null or final class
     *
     * @param clazz a given class
     *
     * @see Class
     * @see Modifier
     */
    private void descriptionClass(Class clazz) throws ImplerException {
        if (clazz == null || Modifier.isFinal(clazz.getModifiers())) {
            throw new ImplerException();
        }

        if (clazz.getPackage() != null) {
            strB.append("package " + clazz.getPackage().getName() + ";\n");
        }

        classMod(clazz);
        strB.append("class " + clazz.getSimpleName() + "Impl ");

            if (!clazz.isInterface()) {
                strB.append("extends " + clazz.getSimpleName() + " ");
            } else {
                strB.append("implements " + clazz.getSimpleName() + " ");
            }

        strB.append(" {\n");
        classConstr(clazz);
        methods(clazz);

        strB.append("}\n");
    }

    /**
     * Appends a string of methods of a given class
     *
     * @param clazz a given class
     *
     * @see Class
     * @see Method
     */
    private void methods(Class clazz){
        HashSet<Method> methods = new HashSet<>();

        for (Method method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                methods.add(method);
            }
        }
        Class<?> current = clazz;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isAbstract(modifiers) && !Modifier.isPrivate(modifiers) && !Modifier.isPublic(modifiers)) {
                    methods.add(method);
                }
            }
            current = current.getSuperclass();
        }

        for (Method m : methods) {
            Annotation[] annotations = m.getAnnotations();
            strB.append("\t");
            for (Annotation a : annotations) {
                strB.append("@" + a.annotationType().getName() + " ");
            }
            strB.append("\n");

            String str = getModifiers(m.getModifiers()).replace("abstract ", "");
            strB.append("\t" + str);

            if (!m.getReturnType().isPrimitive()) {
                strB.append(m.getReturnType().getCanonicalName() + " " + m.getName());
            } else {
                strB.append(getType(m.getReturnType()) + " " + m.getName());
            }

            strB.append("(" + getParameters(m.getParameterTypes()) + ") ");

            Class[] ex = m.getExceptionTypes();
            if (ex.length > 0) {
                strB.append("throws ");
                for (int i = 0; i < ex.length; i++) {
                    strB.append(i == 0 ? ex[i].getCanonicalName() : ", " + ex[i].getCanonicalName());
                }
            }

            strB.append("{ \n\t\treturn ");
            Class mReturnType = m.getReturnType();
            HashMap<Class, Pair> primitives2wrappers
                    = new HashMap<>();
            primitives2wrappers.put(boolean.class, new Pair(Boolean.class, false));
            primitives2wrappers.put(byte.class, new Pair(Byte.class, 0));
            primitives2wrappers.put(char.class, new Pair(Character.class, 0));
            primitives2wrappers.put(double.class, new Pair(Double.class, 0));
            primitives2wrappers.put(float.class, new Pair(Float.class, 0));
            primitives2wrappers.put(int.class, new Pair(Integer.class, 0));
            primitives2wrappers.put(long.class, new Pair(Long.class, 0));
            primitives2wrappers.put(short.class, new Pair(Short.class, 0));
            primitives2wrappers.put(void.class, new Pair(Void.class, ""));

            Object ob;
            if (mReturnType.isPrimitive()) {
                ob = primitives2wrappers.get(mReturnType).ob;
                strB.append(ob.toString() + ";\n\t}\n");
            } else {
                strB.append("null;\n\t}\n");
            }

        }
    }

    /**
     * Returns a string of modifiers decoded from given number
     *
     * @param m a number representing modifiers
     *
     * @see Modifier
     *
     * @return a string of modifiers decoded from given number
     */
     private static String getModifiers(int m) {
        String modifiers = "";
        if (Modifier.isPublic(m)) modifiers += "public ";
        if (Modifier.isAbstract(m)) modifiers += "abstract ";
        if (Modifier.isProtected(m)) modifiers += "protected ";
        if (Modifier.isPrivate(m)) modifiers += "private ";
        if (Modifier.isStatic(m)) modifiers += "static ";
        if (Modifier.isFinal(m)) modifiers += "final ";
        if (Modifier.isNative(m)) modifiers += "native ";
        return modifiers;
    }

    /**
     * Returns a string representing a type of given class
     *
     * @param clazz a given class
     *
     * @see Class
     *
     * @return a string representing a type of given class
     */
    private static String getType(Class clazz) {
        String type = clazz.isArray()
                ? clazz.getComponentType().getName()
                : clazz.getName();
        if (clazz.isArray()) type += "[]";
        return type;
    }

    /**
     * Returns a string representing parameters from given array as string.
     * Example: "type1 type_name1, type2 type_name2...".
     *
     * @param params an array of classes of parameters
     *
     * @return a string representing parameters from given array
     */
    private static String getParameters(Class[] params) {
        String p = "";
        for (int i = 0, size = params.length; i < size; i++) {
            if (i > 0) p += ", ";
            p += getType(params[i]) + " param" + i;
        }
        return p;
    }

    /**
     * Returns a string representing names of parameters from given array in given order
     * "type_name1, type_name2..."
     *
     * @param params an array of classes of parameters
     *
     * @return a string representing names of parameters from given array in given order
     */
    private static String getParameterNames(Class[] params) {
        String p = "";
        for (int i = 0, size = params.length; i < size; i++) {
            if (i > 0) p += ", ";
            p += " param" + i;
        }
        return p;
    }

    /**
     * Creates an implementation of a given class in given directory.
     * Creates a class implementing an abstract class or interface with all abstract methods returning
     * default values (<code>"false"</code> for <code>boolean</code>, <code>"0"</code> for odd primitive types,
     * <code>"null"</code> for other types).
     *
     *
     * @param token a class to implement
     * @param root a path to create a java-file with implementation of {@code token}
     *
     * @see Class
     * @see Path
     *
     * @throws ImplerException then given a class or interface can't be implemented
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        try {
//            Path path = Paths.get("/Users/vorona/IdeaProjects/Implementor/" + token.getCanonicalName().replace('.', File.separatorChar) + "Impl.java");
            Path path = root.resolve(token.getCanonicalName().replace('.', File.separatorChar) + "Impl.java");
            Files.createDirectories(path.getParent());

            out = Files.newBufferedWriter(path);
            strB = new StringBuilder();
            descriptionClass(token);
            out.write(strB.toString());
        } catch (IOException e) {
            System.out.print("Ooops");
        } finally {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {

    }

    /**
     * Class representing a pair of Class and Object
     *
     * @see Class
     * @see Object
     *
     *
     */
    private class Pair {
        public Class c;
        public Object ob;

        Pair(Class c, Object ob) {
            this.c = c;
            this.ob = ob;
        }
    }
}

