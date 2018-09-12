package net.pardini.scaleway.retrofit;

import net.pardini.scaleway.wrapper.OrgListWrapper;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ScalewayAccountAPI {


    @GET("/organizations")
    Call<OrgListWrapper> getAllOrganizations();

}
