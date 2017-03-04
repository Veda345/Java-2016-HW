import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Class for recursive download URLs with given depth.
 */
public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int perHost;

    private ExecutorService downloadersPool;
    private ExecutorService extractorsPool;

    private Set<String> downloaded;

    private Map<String, IOException> exceptions;
    private Map<String, CustomExecutor> hosts;

    /**
     * Create an instatnce of WebCrawler class
     * @param downloader downloader of specified URL
     * @param downloaders maximum number of download threads
     * @param extractors maximum number of extractor threads
     * @param perHost maximum number of download task for one Host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloadersPool = Executors.newFixedThreadPool(downloaders);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        downloaded = Collections.newSetFromMap(new ConcurrentHashMap<>());
        exceptions = new ConcurrentHashMap<>();
        hosts = new ConcurrentHashMap<>();
    }

    /**
     * Recursive download of specified URLs with specified depth.
     * @param url specified URL
     * @param maxDepth specified depth
     * @return instance of Result class containing list of loaded URLs and map of exceptions
     * @see Result
     */
    @Override
    public Result download(String url, int maxDepth) {
        final Phaser phaser = new Phaser(1);
        final DownloadTask downloadTask = new DownloadTask(url, 1, maxDepth, phaser);
        try {
            phaser.register();
            downloadTask.addTask();
        } catch (IOException e) {
            exceptions.put(url, e);
            phaser.arrive();
        }

        phaser.arriveAndAwaitAdvance();
        List<String> res = downloaded.stream().filter((key) -> !exceptions.containsKey(key)).collect(Collectors.toList());
        return new Result(res, exceptions);
    }

    /**
     * Auto close of all downloading tasks.
     */
    @Override
    public void close() {
        downloadersPool.shutdownNow();
        extractorsPool.shutdownNow();
    }

    private class DownloadTask implements Runnable {
        private String url;
        private int depth;
        private int maxDepth;
        private Phaser phaser;

        public DownloadTask(final String url, int curDepth, int maxDepth, final Phaser phaser) {
            this.url = url;
            this.depth = curDepth;
            this.maxDepth = maxDepth;
            this.phaser = phaser;
        }

        private void addTask() throws IOException {
            final String host = URLUtils.getHost(url);
            hosts.putIfAbsent(host, new CustomExecutor(downloadersPool, perHost));
            hosts.get(host).addTask(this);
        }

        @Override
        public void run() {
            try {
                if (!downloaded.add(url)) {
                    return;
                }
                Document document = downloader.download(url);
                if (this.depth == maxDepth) {
                    return;
                }
                ExtractTask extractTask = new ExtractTask(document, url, depth, maxDepth, phaser);
                phaser.register();
                extractTask.addTask();

            } catch (IOException e) {
                exceptions.put(url, e);
            } finally {
                phaser.arrive();
            }
        }
    }

    private class ExtractTask implements Runnable {
        private Document document;
        private String url;
        private int depth;
        private int maxDepth;
        private Phaser phaser;

        public ExtractTask(final Document document, final String url, int depth, int maxDepth, final Phaser phaser) {
            this.document = document;
            this.url = url;
            this.depth = depth;
            this.maxDepth = maxDepth;
            this.phaser = phaser;
        }

        public void addTask() {
            extractorsPool.submit(this);
        }

        @Override
        public void run() {
            try {
                List<String> links = document.extractLinks();

                for (String currentLink : links) {
                    DownloadTask downloadTask = new DownloadTask(currentLink, depth + 1, maxDepth, phaser);

                    phaser.register();
                    downloadTask.addTask();
                }
            } catch (IOException e) {
                exceptions.put(url, e);
            } finally {
                phaser.arrive();
            }
        }
    }

    private static class CustomExecutor {
        private Semaphore semaphore;
        private ExecutorService service;

        public CustomExecutor(ExecutorService service, int limit) {
            this.service = service;
            this.semaphore = new Semaphore(limit);
        }

        public void addTask(Runnable task) {
            Runnable my_task = () -> {
                try {
                    semaphore.acquire();
                    task.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            };

            service.submit(my_task);
        }
    }

}
