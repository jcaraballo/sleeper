import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.Sleeper;
import testtools.HttpClientWrapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SleeperTest {
    private HttpClientWrapper client;
    private Sleeper sleeper;

    @Before
    public void setUp() throws Exception {
        sleeper = new Sleeper();
        sleeper.start(8082);
        client = new HttpClientWrapper();
    }

    @After
    public void tearDown() throws Exception {
        client.shutdown();
        sleeper.stop();
    }

    @Test
    public void sleeps() throws IOException, InterruptedException, ExecutionException {
        // Should be using asynchronous http client and ensuring that it holds the client
        HttpClientWrapper.Result result = client.get("http://localhost:8082/sleep/10");
        assertThat(result.statusCode, is(HttpServletResponse.SC_OK));
    }

    @Test
    public void doesNotSleepImpossibleTimes() throws IOException {
        HttpClientWrapper.Result result = client.get("http://localhost:8082/sleep/5meters");
        assertThat(result.statusCode, is(HttpServletResponse.SC_FORBIDDEN));
    }
}