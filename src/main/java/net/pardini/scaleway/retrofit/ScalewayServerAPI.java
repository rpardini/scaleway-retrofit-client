package net.pardini.scaleway.retrofit;

import net.pardini.scaleway.model.Actions;
import net.pardini.scaleway.model.Images;
import net.pardini.scaleway.model.Server;
import net.pardini.scaleway.model.Taskresult;
import net.pardini.scaleway.wrapper.ServerListWrapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

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

    @Headers("Content-type: application/json")
    @POST("/servers")
    public Call<ServerSingleWrapper> createServer(@Body Server serverDefinition);
    
    @Headers("Content-type: text/plain")
    @PATCH("/servers/{serverId}/user_data/cloud-init")
    public Call<ResponseBody> setServerCloudInitData(@Path("serverId") String serverId, @Body RequestBody cloudInitContents);
    
    
    @POST("/servers/{serverId}/action")
    public Call<Taskresult> powerOnServer(@Path("serverId") String serverId, @Body Actions action);

    @GET("/tasks/{taskId}")
    Call<Taskresult> getTaskStatus(@Path("taskId") String taskId);

}
