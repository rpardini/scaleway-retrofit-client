package net.pardini.scaleway.retrofit;

import net.pardini.scaleway.model.Images;
import net.pardini.scaleway.wrapper.ServerListWrapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ScalewayServerAPI {


    @GET("/servers")
    public Call<ServerListWrapper> getAllServers(@Query("per_page") int per_page, @Query("page") int page);


    @GET("/servers")
    public Call<ServerListWrapper> findServerByName(@Query("per_page") int per_page, @Query("page") int page, @Query("name") String name);


    @GET("/servers/{id}")
    Call<ServerSingleWrapper> getServerById(@Path("id") String id);


    @GET("/images")
    public Call<Images> getAllImages(@Query("per_page") int per_page, @Query("page") int page);

    @GET("/images")
    public Call<Images> getArchImages(@Query("per_page") int per_page, @Query("page") int page, @Query("arch") String arch);

    @GET("/images")
    public Call<Images> getArchImagesByName(@Query("per_page") int per_page, @Query("page") int page, @Query("arch") String arch, @Query("name") String name);


}
