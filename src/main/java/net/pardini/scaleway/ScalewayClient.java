package net.pardini.scaleway;

import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.Image;
import net.pardini.scaleway.model.Images;
import net.pardini.scaleway.model.Organization;
import net.pardini.scaleway.model.Server;
import net.pardini.scaleway.retrofit.ScalewayAccountAPI;
import net.pardini.scaleway.retrofit.ScalewayServerAPI;
import net.pardini.scaleway.wrapper.ServerListWrapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;

@Slf4j
public class ScalewayClient {

    private static final int DEFAULT_PAGE_SIZE = 88;
    private final ScalewayAccountAPI accountClient;
    private final ScalewayServerAPI computeClient;
    private String authToken;
    private ScalewayRegion region;

    public ScalewayClient(String authToken, ScalewayRegion region) {
        this.authToken = authToken;
        this.region = region;
        this.computeClient = this.createComputeClient();
        this.accountClient = this.createAccountClient();
    }

    @SneakyThrows
    public List<Organization> getAllOrganizations() {
        return accountClient.getAllOrganizations().execute().body().getOrganizations();
    }

    @SneakyThrows
    public List<Server> getAllServers() {
        return new ScalewayDepaginator<ServerListWrapper, Server>().depaginate(execute -> execute.body().getServers(), currPage -> computeClient.getAllServers(DEFAULT_PAGE_SIZE, currPage));
    }

    @SneakyThrows
    public List<Image> getAllImages() {
        return new ScalewayDepaginator<Images, Image>().depaginate(execute -> execute.body().getImages(), currPage -> computeClient.getAllImages(DEFAULT_PAGE_SIZE, currPage));
    }

    @SneakyThrows
    public Server getSpecificServer(String id) {
        Response<ServerSingleWrapper> execute = computeClient.getServerById(id).execute();
        return execute.body().getServer();
    }

    private ScalewayAccountAPI createAccountClient() {
        String baseUrl = "https://account.scaleway.com/";
        Retrofit retrofit = createClientForURL(baseUrl);
        return retrofit.create(ScalewayAccountAPI.class);
    }

    private ScalewayServerAPI createComputeClient() {
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
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

    }

}
