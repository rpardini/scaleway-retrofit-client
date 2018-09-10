package net.pardini.scaleway.retrofit;

import net.pardini.scaleway.model.Organization;
import net.pardini.scaleway.wrapper.OrgListWrapper;
import net.pardini.scaleway.wrapper.ServerListWrapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ScalewayAccountAPI {


    @GET("/organizations")
    public Call<OrgListWrapper> getAllOrganizations();

}
