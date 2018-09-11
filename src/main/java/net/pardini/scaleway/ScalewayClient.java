package net.pardini.scaleway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.depaginator.ScalewayDepaginator;
import net.pardini.scaleway.model.*;
import net.pardini.scaleway.retrofit.ScalewayAccountAPI;
import net.pardini.scaleway.retrofit.ScalewayServerAPI;
import net.pardini.scaleway.wrapper.ServerListWrapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import net.pardini.scaleway.wrapper.VolumeWrapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class ScalewayClient {

    private static final int DEFAULT_PAGE_SIZE = 100;
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
    Server powerOnServer(String serverId) {
        // Check to see if the server is not already powered-on...
        Server currentServerInfo = this.getSpecificServer(serverId);
        if (currentServerInfo.getState().equals("running")) {
            log.warn("Server " + serverId + " (" + currentServerInfo.getName() + ") is already running. Not powering it on again.");
            return currentServerInfo;
        }

        // Actually power it on, then.
        Actions action = new Actions();
        action.setAction(Actions.Action.POWERON);
        Response<Taskresult> taskresultResponse = computeClient.powerOnServer(serverId, action).execute();
        makeSureResponseSucessfull(taskresultResponse);

        log.info(taskresultResponse.toString());
        String taskId = taskresultResponse.body().getTask().getId();
        Task.Status status = taskresultResponse.body().getTask().getStatus();


        currentServerInfo = this.getSpecificServer(serverId);
        log.info("Current server status: " + currentServerInfo.getState() + " detail: " + currentServerInfo.getStateDetail());

        while (status == Task.Status.PENDING) {

            log.info("Waiting for task result...");
            Thread.sleep(5 * 1000);

            Response<Taskresult> newTaskStatusResponse = computeClient.getTaskStatus(taskId).execute();
            makeSureResponseSucessfull(newTaskStatusResponse);

            log.info("New task result: " + newTaskStatusResponse.body().getTask());
            status = newTaskStatusResponse.body().getTask().getStatus();

            currentServerInfo = this.getSpecificServer(serverId);
            log.info("Current server status: " + currentServerInfo.getState() + " detail: " + currentServerInfo.getStateDetail());

        }

        return currentServerInfo;

    }


    @SneakyThrows
    public Server createServer(Server.CommercialType commercialType, String imageId, String name, String orgId, List<String> tags, String cloudInitUrl) {
        return this.createServer(commercialType, imageId, name, orgId, tags, cloudInitUrl, null);
    }

    @SneakyThrows
    public Server createServer(Server.CommercialType commercialType, String imageId, String name, String orgId, List<String> tags, String cloudInitUrl, List<VolumeWrapper> extraVolumes) {
        // First try and find the server by name, to avoid duplicating...?
        Server serverByName = this.findServerByName(name);
        if (serverByName != null) {
            log.warn("Server " + name + " already exists. Not creating a new one.");
            return serverByName;
        }

        Server serverDefinition = new Server();
        serverDefinition.setCommercialType(commercialType);
        serverDefinition.setImage(imageId);
        serverDefinition.setName(name);
        serverDefinition.setOrganization(orgId);
        serverDefinition.setTags(tags);

        // If there are extra volumes, convert them.
        if (extraVolumes != null && extraVolumes.size() > 0) {
            Volumes vols = new Volumes();
            int volCounter = 1;
            for (VolumeWrapper extraVolume : extraVolumes) {
                String volumeKey = String.valueOf(volCounter);
                log.info(String.format("Adding extra volume '%s' of %dGb with key '%s'.", extraVolume.getName(), extraVolume.getSizeInGb(), volumeKey));
                VolumesProperty volumesProperty = new VolumesProperty();
                volumesProperty.setName(extraVolume.getName());
                volumesProperty.setSize(BigInteger.valueOf((extraVolume.getSizeInGb() * 1000 * 1000 * 1000L)));
                volumesProperty.setOrganization(orgId);
                volumesProperty.setVolumeType(extraVolume.getType());
                vols.getAdditionalProperties().put(volumeKey, volumesProperty);
                volCounter++;
            }
            serverDefinition.setVolumes(vols);
        }

        // Some fixed stuff, to make life easier.
        serverDefinition.setEnableIpv6(false);
        serverDefinition.setBootType("local"); // boot from "grub" locally installed we hope
        serverDefinition.setExtraNetworks(null);

        Call<ServerSingleWrapper> call = computeClient.createServer(serverDefinition);
        Response<ServerSingleWrapper> callExecution = call.execute();
        makeSureResponseSucessfull(callExecution);

        Server createdServer = callExecution.body().getServer();
        String createdServerId = createdServer.getId();

        if (cloudInitUrl != null) {
            log.info("Setting cloud-init URL for server " + createdServerId);
            String cloudInitString = "#include" + "\n" + cloudInitUrl + "\n";
            Response cloudInitResponse = computeClient.setServerCloudInitData(createdServerId, RequestBody.create(MediaType.parse("text/plain"), cloudInitString)).execute();
            makeSureResponseSucessfull(cloudInitResponse);
        } else {
            log.warn("Cloud-init URL not specified for server " + createdServerId);
        }

        return createdServer;
    }

    @SneakyThrows
    private void makeSureResponseSucessfull(Response someResponse) {
        // @TODO: better error handling.
        if (!someResponse.isSuccessful())
            throw new RuntimeException("Call execution failed: " + someResponse.message() + ": " + someResponse.errorBody().string());
    }

    @SneakyThrows
    public List<Organization> getAllOrganizations() {
        return accountClient.getAllOrganizations().execute().body().getOrganizations();
    }

    @SneakyThrows
    public Organization getOneAndOnlyOrganization() {
        return this.getAllOrganizations().get(0);
    }

    @SneakyThrows
    public List<Server> getAllServers() {
        return new ScalewayDepaginator<ServerListWrapper, Server>().depaginate(execute -> execute.body().getServers(), currPage -> computeClient.getAllServers(DEFAULT_PAGE_SIZE, currPage));
    }

    @SneakyThrows
    public List<Server> findServersByName(String name) {
        return new ScalewayDepaginator<ServerListWrapper, Server>().depaginate(execute -> execute.body().getServers(), currPage -> computeClient.findServerByName(DEFAULT_PAGE_SIZE, currPage, name));
    }

    @SneakyThrows
    public Server findServerByName(String name) {
        List<Server> serverList = new ScalewayDepaginator<ServerListWrapper, Server>().depaginate(execute -> execute.body().getServers(), currPage -> computeClient.findServerByName(DEFAULT_PAGE_SIZE, currPage, name));
        return serverList.size() == 1 ? serverList.get(0) : null;
    }


    @SneakyThrows
    public List<Image> getAllImages() {
        return new ScalewayDepaginator<Images, Image>().depaginate(execute -> execute.body().getImages(), currPage -> computeClient.getAllImages(DEFAULT_PAGE_SIZE, currPage));
    }

    @SneakyThrows
    public List<Image> getArchImages(String arch) {
        return new ScalewayDepaginator<Images, Image>().depaginate(execute -> execute.body().getImages(), currPage -> computeClient.getArchImages(DEFAULT_PAGE_SIZE, currPage, arch));
    }

    @SneakyThrows
    public List<Image> getArchImagesByName(String arch, String name) {
        return new ScalewayDepaginator<Images, Image>().depaginate(execute -> execute.body().getImages(), currPage -> computeClient.getArchImagesByName(DEFAULT_PAGE_SIZE, currPage, arch, name));
    }

    public Image getBestArchImageByName(String arch, String name) {
        List<Image> archImagesByName = this.getArchImagesByName(arch, name);
        // sort... ?
        archImagesByName.sort(Comparator.comparing(Image::getModificationDate).reversed());
        return archImagesByName.get(0);
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
