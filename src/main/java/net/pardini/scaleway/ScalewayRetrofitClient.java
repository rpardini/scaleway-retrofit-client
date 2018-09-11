package net.pardini.scaleway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.retrofit.ScalewayAccountAPI;
import net.pardini.scaleway.retrofit.ScalewayServerAPI;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Slf4j
public class ScalewayRetrofitClient {
    protected static final int DEFAULT_PAGE_SIZE = 100;
    protected final ScalewayAccountAPI accountClient;
    protected final ScalewayServerAPI computeClient;
    protected String authToken;
    protected ScalewayRegion region;

    public ScalewayRetrofitClient(String authToken, ScalewayRegion region) {
        this.authToken = authToken;
        this.region = region;
        this.computeClient = this.createComputeClient();
        this.accountClient = this.createAccountClient();
    }
    
    @SneakyThrows
    protected void makeSureResponseSucessfull(Response someResponse) {
        if (!someResponse.isSuccessful())
            throw new RuntimeException("Scaleway Call failed: " + someResponse.message() + ": " + someResponse.errorBody().string());
    }

    protected ScalewayAccountAPI createAccountClient() {
        String baseUrl = "https://account.scaleway.com/";
        Retrofit retrofit = createClientForURL(baseUrl);
        return retrofit.create(ScalewayAccountAPI.class);
    }

    protected ScalewayServerAPI createComputeClient() {
        String baseUrl = region == ScalewayRegion.PAR1 ? "https://cp-par1.scaleway.com/" : "https://cp-ams1.scaleway.com/";
        Retrofit retrofit = createClientForURL(baseUrl);
        return retrofit.create(ScalewayServerAPI.class);
    }

    private Retrofit createClientForURL(String baseUrl) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.interceptors().clear();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("x-auth-token", authToken)
                    .build();
            return chain.proceed(request);
        });

        httpClient.addInterceptor(new HttpLoggingInterceptor(log));

        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(configureObjectMapper()))
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

    }

    private ObjectMapper configureObjectMapper() {
        final JodaModule jodaModule = new JodaModule();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(jodaModule);
        return objectMapper;
    }
}
