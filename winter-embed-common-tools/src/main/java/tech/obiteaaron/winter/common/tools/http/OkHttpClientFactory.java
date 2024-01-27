package tech.obiteaaron.winter.common.tools.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Timeout;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class OkHttpClientFactory {

    public static final int MIN_TIMEOUT = 1;
    public static final int MIN_CONN_TOTAL = 1;
    public static final int MIN_PER_HOST_CONN = 1;

    public static CommonOkHttpClient commonOkHttpClient() {
        return new CommonOkHttpClient(createDefault());
    }

    public static OkHttpClient createDefault() {
        return create(0,
                10_000,
                5,
                10,
                false,
                false,
                null);
    }

    public static OkHttpClient createEnhanceDefault() {
        return create(0,
                10_000,
                5,
                10,
                false,
                true,
                null);
    }

    /**
     * @param connectionFromPoolTimeoutMilliseconds 从连接池获取链接的超市时间，默认不超时，传入0即可
     * @param timeOutMilliseconds                   连接、读、写超时时间，毫秒
     * @param maxConnTotal                          最大连接数量，默认10
     * @param maxConnPerHost                        每个host最大连接数量，默认5
     * @param allAllSsl                             是否允许所有 SSL，默认false，不建议使用true
     * @param syncToAsync                           是否使用同步转异步。因为OkHttp对于同步调用是当前线程直接调用，不走连接池（这一点和HttpClient的设计不同），只有异步会走连接池，
     *                                              因此maxConnTotal和maxConnPerHost只在异步下才生效，如果业务代码同步调用，可以用这个开关实现同步转异步以使并发数生效。
     *                                              但性能会比直接同步调用和直接异步调用略差一点点。
     * @param defaultHeaders                        默认的Header，比如JSON
     * @return
     */
    public static OkHttpClient create(int connectionFromPoolTimeoutMilliseconds,
                                      int timeOutMilliseconds,
                                      int maxConnTotal,
                                      int maxConnPerHost,
                                      boolean allAllSsl,
                                      boolean syncToAsync,
                                      Map<String, String> defaultHeaders) {
        try {
            timeOutMilliseconds = Math.max(timeOutMilliseconds, MIN_TIMEOUT);
            maxConnTotal = Math.max(maxConnTotal, MIN_CONN_TOTAL);
            maxConnPerHost = Math.max(maxConnPerHost, MIN_PER_HOST_CONN);

            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            if (defaultHeaders != null && !defaultHeaders.isEmpty()) {
                builder.addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public Response intercept(@NotNull Chain chain) throws IOException {
                        Request request = chain.request();
                        Request.Builder builder1 = request.newBuilder();
                        defaultHeaders.forEach(builder1::addHeader);
                        return chain.proceed(builder1.build());
                    }
                });
            }
            if (allAllSsl) {
                X509TrustManager x509TrustManager = new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                };
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
                builder.setHostnameVerifier$okhttp((message, session) -> true);
            }
            builder.readTimeout(timeOutMilliseconds, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeOutMilliseconds, TimeUnit.MILLISECONDS)
                    .connectTimeout(timeOutMilliseconds, TimeUnit.MILLISECONDS);
            if (connectionFromPoolTimeoutMilliseconds > 0) {
                // 异步排队等待时间+connect+读写时间，默认不设置
                builder.callTimeout(connectionFromPoolTimeoutMilliseconds + timeOutMilliseconds * 3L, TimeUnit.MILLISECONDS);
            }

            // 这两个参数仅异步生效，同步都是直接由调用线程直接执行的，同步请求无上限
            OkHttpClient okHttpClient = builder.build();
            okHttpClient.dispatcher().setMaxRequests(maxConnTotal);
            okHttpClient.dispatcher().setMaxRequestsPerHost(maxConnPerHost);

            if (syncToAsync) {
                return new EnhanceOkHttpClient(okHttpClient);
            }
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class EnhanceOkHttpClient extends OkHttpClient {
        private final OkHttpClient delegate;

        public EnhanceOkHttpClient(OkHttpClient delegate) {
            this.delegate = delegate;
        }


        @NotNull
        @Override
        public Builder newBuilder() {
            return delegate.newBuilder();
        }

        @NotNull
        @Override
        public Call newCall(@NotNull Request request) {
            return new EnhanceCall(delegate.newCall(request));
        }

        @NotNull
        @Override
        public WebSocket newWebSocket(@NotNull Request request, @NotNull WebSocketListener listener) {
            return delegate.newWebSocket(request, listener);
        }
    }

    private static class EnhanceCall implements Call {

        private final Call delegate;

        public EnhanceCall(Call delegate) {
            this.delegate = delegate;
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @NotNull
        @Override
        public Call clone() {
            return new EnhanceCall(delegate.clone());
        }

        @Override
        public void enqueue(@NotNull Callback callback) {
            delegate.enqueue(callback);
        }

        @NotNull
        @Override
        public Response execute() throws IOException {
            // 同步转异步，业务逻辑用同步，使用方便，实际走异步，收到最大连接数量的限制
            try {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                AtomicReference<Response> responseRef = new AtomicReference<>();
                AtomicReference<IOException> ioExceptionRef = new AtomicReference<>();
                delegate.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        ioExceptionRef.set(e);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        responseRef.set(response);
                        countDownLatch.countDown();
                    }
                });
                countDownLatch.await();
                if (ioExceptionRef.get() != null) {
                    throw ioExceptionRef.get();
                }
                return responseRef.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @NotNull
        @Override
        public Request request() {
            return delegate.request();
        }

        @NotNull
        @Override
        public Timeout timeout() {
            return delegate.timeout();
        }
    }

}
