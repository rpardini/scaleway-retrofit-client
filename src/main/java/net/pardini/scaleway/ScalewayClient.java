package net.pardini.scaleway;

import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.Server;
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
@AllArgsConstructor
public class ScalewayClient {

    private String authToken;
    private ScalewayRegion region;
    private ScalewayServerAPI client;

    public ScalewayClient(String authToken, ScalewayRegion region) {
        this.authToken = authToken;
        this.region = region;
        this.createClient();
    }

    @SneakyThrows
    public List<Server> getAllServers() {
        Response<ServerListWrapper> execute = client.getAllServers().execute();
        return execute.body().getServers();
    }

    @SneakyThrows
    public Server getSpecificServer(String id) {
        Response<ServerSingleWrapper> execute = client.getServerById(id).execute();
        return execute.body().getServer();
    }

    private void createClient() {
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(region == ScalewayRegion.PAR1 ? "https://cp-par1.scaleway.com/" : "https://cp-ams1.scaleway.com/")
                .client(httpClient.build())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        client = retrofit.create(ScalewayServerAPI.class);
    }

}
