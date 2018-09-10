package net.pardini.scaleway.retrofit;

import net.pardini.scaleway.wrapper.ServerListWrapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ScalewayServerAPI {


    @GET("/servers")
    public Call<ServerListWrapper> getAllServers();


    @GET("/servers/{id}")
    Call<ServerSingleWrapper> getServerById(@Path("id") String id);

}
