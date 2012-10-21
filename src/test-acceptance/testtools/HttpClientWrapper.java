package testtools;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpClientWrapper {
    private final DefaultHttpClient httpClient = new DefaultHttpClient();

    public void shutdown() {
        httpClient.getConnectionManager().shutdown();
    }

    public Result get(String uri) throws IOException {
        HttpResponse response = httpClient.execute(new HttpGet(uri));
        int statusCode = response.getStatusLine().getStatusCode();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        response.getEntity().writeTo(byteArrayOutputStream);

        return new Result(statusCode, byteArrayOutputStream.toString("UTF-8"));
    }

    public static class Result {
        public final int statusCode;
        public final String body;

        public Result(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    public static Matcher<Result> hasStatusCode(final Matcher<Integer> statusCodeMatcher) {
        return new TypeSafeDiagnosingMatcher<Result>() {
            @Override
            protected boolean matchesSafely(Result actual, Description mismatchDescription) {
                if (statusCodeMatcher.matches(actual.statusCode)) {
                    mismatchDescription.appendText(" status code ");
                    statusCodeMatcher.describeMismatch(actual.statusCode, mismatchDescription);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Response with status code ").appendDescriptionOf(statusCodeMatcher);
            }
        };
    }
}