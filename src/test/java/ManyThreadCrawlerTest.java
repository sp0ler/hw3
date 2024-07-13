import org.example.ManyThreadCrawler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты иногда могут закончится неуспешно, т.к. найдется быстрее другой путь, не который ожидается
 */
class ManyThreadCrawlerTest {

    private static final int delay = 10;
    private static final int deep = 5;
    private static final TimeUnit minutes = TimeUnit.MINUTES;

    /**
     * Быстрый тест, время выполнения около 3-5 СЕКУНД на 5 потоках
     */
    @Test
    void findTest1() throws Exception {
        List<String> list = List.of("Insulin", "Carbohydrate_metabolism", "Vitamin_C");
        String answer = String.join(" > ", list);

        String result = getResult(list, 5);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(answer, result)
        );
    }

    /**
     * Быстрый тест, время выполнения около 3-5 сек на 5 потоках
     */
    @Test
    void findTest2() throws Exception {
        List<String> list = List.of("Natural_monopoly", "Monopoly", "Michael_Oakeshott");
        String answer = String.join(" > ", list);

        String result = getResult(list, 5);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(answer, result)
        );
    }

    /**
     * Быйстрый тест, время выполнения около 5-7 СЕКУНД на 5 потоках
     */
    @Test
    void findTest3() throws Exception {
        List<String> list = List.of("Matter", "Ancient_India", "Cavalry");
        String answer = String.join(" > ", list);

        String result = getResult(list, 5);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(answer, result)
        );
    }

    /**
     * Долгий тест, время выполнения около 5 МИНУТ на 30 потоках, CPU = 100%, rateLimiter = 5
     * Часто выдает разную цепочку ответов!
     */
    @Test
    void findTest4() throws Exception {
        List<String> list = List.of("Carbon_dioxide", "Lithium", "Soybean", "Toddler");
        String answer = String.join(" > ", list);

        String result = getResult(list, 30);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(answer, result)
        );
    }

    private String getResult(List<String> list, int threadNumber) throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadNumber; i++) {
            threads.add(new Thread(new ManyThreadCrawler(list.getFirst(), list.getLast(), delay, minutes, deep)));
        }
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        return ManyThreadCrawler.resultString;
    }
}
