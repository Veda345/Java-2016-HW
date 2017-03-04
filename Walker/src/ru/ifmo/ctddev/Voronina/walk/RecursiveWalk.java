package ru.ifmo.ctddev.Voronina.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class RecursiveWalk {

    public static void main(String[] args) {
        assert args.length >= 2;

        String source = args[0];
        String dest = args[1];

//            java -cp .:out/production/:Term4_Java/:hamcrest-core-1.3.jar:junit-4.11.jar:WalkTest.jar info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.Voronina.walk.RecursiveWalk 123


        Path ps = Paths.get(source);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dest), "UTF-8")) {

            ArrayList<String> a = read(ps.toAbsolutePath().toString());
            for (final String s : a) {
                Path start = Paths.get(s);
                try {
                    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            writer.write(Hash.getHash(file.toString()) + " " + file.toString() + "\n");
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException e)
                                throws IOException {
                            if (e == null) {
                                return FileVisitResult.CONTINUE;
                            } else {
                                throw e;
                            }
                        }
                    });
                } catch (NoSuchFileException e) {
                    writer.write(Hash.getHash(s) + " " + s + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> read(String fileName) throws FileNotFoundException {
        // вернем список путей
        File file = new File(fileName);
        ArrayList<String> s = new ArrayList<String>();

        if (!file.exists()) {
            return s;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileName), "UTF-8"))) {
            String str;
            while ((str = br.readLine()) != null) {
                s.add(str);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return s;
    }

}
