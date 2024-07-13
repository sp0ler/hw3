package org.example;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.util.SimpleRateLimiter;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Log4j2
@RequiredArgsConstructor
public class ManyThreadCrawler implements Runnable {

    private static final SimpleRateLimiter rateLimiter = new SimpleRateLimiter(5);
    private static final Set<String> visited = new ConcurrentSkipListSet<>();

    public static volatile String resultString = null;

    private final WikiClient client = new WikiClient();
    private final Queue<Node> searchQueue = new LinkedList<>();

    private final String from;
    private final String target;
    private final long timeout;
    private final TimeUnit timeUnit;
    private final int deep;

    @Override
    public void run() {
        try {
            resultString = find();
        } catch (Exception e) {
            log.error(e);
        }
    }

    private String find() throws Exception {
        long deadline = System.nanoTime() + timeUnit.toNanos(timeout);
        searchQueue.offer(new Node(from, null));

        String title;
        while (resultString == null) {
            if (deadline < System.nanoTime()) {
                throw new TimeoutException();
            }
            if (searchQueue.isEmpty()) {
                continue;
            }

            Node node = searchQueue.poll();
            log.info("Get page: {}", node.title);
            Set<String> links = rateLimiter.rateLimit(() -> client.getByTitle(node.title));
            if (links.isEmpty()) {
                //pageNotFound
                continue;
            }
            for (String link : links) {
                String currentLink = link.toLowerCase();
                if (visited.contains(currentLink)) {
                    continue;
                }
                visited.add(currentLink);

                // Проверка на глубину
                int size = node.size();
                if ((size == deep + 1) && !currentLink.equals(target)) {
                    Iterator<Node> iterator = searchQueue.iterator();
                    String badTitle = node.next.title;
                    while (iterator.hasNext()) {
                        Node iter = iterator.next();
                        title = iter.next.title;
                        if (title.equals(badTitle)) {
                            iterator.remove();
                        }
                    }
                    continue;
                }

                Node subNode = new Node(link, node);
                if (target.equalsIgnoreCase(currentLink)) {
                    return checkResult(subNode);
                }
                searchQueue.offer(subNode);
            }
        }

        return resultString;
    }

    private static String checkResult(Node result) {
        if (result == null) {
            return "not found";
        }

        List<String> resultList = new ArrayList<>();
        Node search = result;
        while (true) {
            resultList.add(search.title);
            if (search.next == null) {
                break;
            }
            search = search.next;
        }
        Collections.reverse(resultList);

        return String.join(" > ", resultList);
    }

    private static class Node {
        String title;
        Node next;

        public Node(String title, Node next) {
            this.title = title;
            this.next = next;
        }

        public int size() {
            Node node = this;
            int size = 1;
            while (node != null) {
                size++;
                node = node.next;
            }

            return size;
        }

        @Override
        public String toString() {
            return checkResult(this);
        }
    }
}
