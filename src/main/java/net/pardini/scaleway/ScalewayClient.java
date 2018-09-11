package net.pardini.scaleway;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.*;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import net.pardini.scaleway.wrapper.VolumeWrapper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ScalewayClient extends ScalewayReadOnlyClient {

    public ScalewayClient(String authToken, ScalewayRegion region) {
        super(authToken, region);
    }

    @SneakyThrows
    Server powerOnServer(String serverId) {
        // Check to see if the server is not already powered-on...
        Server currentServerInfo = this.getSpecificServer(serverId).orElseThrow(() -> {
            throw new RuntimeException("Server to power on not found.");
        });
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


        currentServerInfo = this.getSpecificServer(serverId).orElseThrow(RuntimeException::new);
        log.info("Current server status: " + currentServerInfo.getState() + " detail: " + currentServerInfo.getStateDetail());

        while (status == Task.Status.PENDING) {

            log.info("Waiting for task result...");
            Thread.sleep(5 * 1000);

            Response<Taskresult> newTaskStatusResponse = computeClient.getTaskStatus(taskId).execute();
            makeSureResponseSucessfull(newTaskStatusResponse);

            log.info("New task result: " + newTaskStatusResponse.body().getTask());
            status = newTaskStatusResponse.body().getTask().getStatus();

            currentServerInfo = this.getSpecificServer(serverId).orElseThrow(RuntimeException::new);
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
        Optional<Server> serverByName = this.findServerByName(name);
        if (serverByName.isPresent()) {
            log.warn("Server " + name + " already exists. Not creating a new one.");
            return serverByName.get();
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


}
