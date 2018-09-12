package net.pardini.scaleway;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.*;
import net.pardini.scaleway.request.ServerDefinition;
import net.pardini.scaleway.wrapper.CommercialTypeMapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import net.pardini.scaleway.wrapper.VolumeWrapper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Response;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
@Slf4j
class ScalewayClient extends ScalewayReadOnlyClient {

    public ScalewayClient(String authToken, ScalewayRegion region, Boolean logHttpRequestsAndResponses) {
        super(authToken, region, logHttpRequestsAndResponses);
    }

    public ScalewayClient(String authToken, ScalewayRegion region) {
        super(authToken, region, false);
    }

    @SneakyThrows
    private Server powerOnServer(String serverId, boolean waitForReady) {
        // Check to see if the server is not already powered-on...
        Server currentServerInfo = this.getSpecificServer(serverId).orElseThrow(() -> {
            throw new RuntimeException("Server to power on not found.");
        });
        if (currentServerInfo.getState().equals("running")) {
            log.warn(String.format("Server %s (%s) is already running in state %s (%s). Not powering it on again.", serverId, currentServerInfo.getName(), currentServerInfo.getState(), currentServerInfo.getStateDetail()));
            return currentServerInfo;
        }

        // Actually power it on, then.
        Actions action = new Actions();
        action.setAction(Actions.Action.POWERON);
        Response<Taskresult> taskresultResponse = computeClient.powerOnServer(serverId, action).execute();
        makeSureResponseSucessfull(taskresultResponse);

        String taskId = Objects.requireNonNull(taskresultResponse.body()).getTask().getId();
        Task.Status status = taskresultResponse.body().getTask().getStatus();

        currentServerInfo = this.getSpecificServer(serverId).orElseThrow(RuntimeException::new);
        log.info(String.format("Current server status: %s (%s) [%s]", currentServerInfo.getState(), currentServerInfo.getStateDetail(), currentServerInfo.getId()));

        if (!waitForReady) return currentServerInfo;

        while (status == Task.Status.PENDING) {

            log.info(String.format("Waiting for Scaleway task result for server %s...", currentServerInfo.getId()));
            Thread.sleep(20 * 1000);

            Response<Taskresult> newTaskStatusResponse = computeClient.getTaskStatus(taskId).execute();
            makeSureResponseSucessfull(newTaskStatusResponse);

            status = newTaskStatusResponse.body().getTask().getStatus();

            currentServerInfo = this.getSpecificServer(serverId).orElseThrow(RuntimeException::new);
            log.info(String.format("Current server status: %s (%s) [%s]", currentServerInfo.getState(), currentServerInfo.getStateDetail(), currentServerInfo.getId()));
        }

        return currentServerInfo;

    }


    @SneakyThrows
    public Server startServer(ServerDefinition definition) {

        // First try and find the server by name, to avoid duplicating...?
        String name = definition.getName();
        if (StringUtils.isEmpty(name))
            throw new RuntimeException("name is required, and is a key for servers in the same region");

        Optional<Server> serverByName = this.findServerByName(name);
        if (serverByName.isPresent()) {
            log.warn(String.format("Server '%s' already exists. Not creating a new one.", name));
            return definition.isPowerOn() ? this.powerOnServer(serverByName.get().getId(), definition.isWaitForReady()) : serverByName.get();
        }

        if (definition.getCommercialType() == null)
            throw new RuntimeException("commercialType is required.");

        String orgId = definition.getOrganizationId();
        if (definition.getOrganization() != null) {
            orgId = definition.getOrganization().getId();
        }
        if (orgId == null) {
            log.debug("Using default/first organization as default.");
            Organization myOrg = this.getOneAndOnlyOrganization().get();
            log.debug(String.format("Using organization '%s'.", myOrg.getName()));
            orgId = myOrg.getId();
        }

        String imageId = definition.getImageId();
        if (definition.getImage() != null) {
            imageId = definition.getImage().getId();
        }
        if (imageId == null && StringUtils.isNotEmpty(definition.getOs())) {
            log.debug(String.format("Finding latest '%s' image....", definition.getOs()));
            Image bestOsImage = this.getBestArchImageByName(CommercialTypeMapper.archFromCommercialType(definition.getCommercialType()).value(), definition.getOs());
            log.info(String.format("... using '%s' modified at %s [%s}", bestOsImage.getName(), bestOsImage.getModificationDate().toString(), bestOsImage.getArch()));
            imageId = bestOsImage.getId();
        }


        Server scalewayCreateCall = new Server();
        scalewayCreateCall.setCommercialType(definition.getCommercialType());
        scalewayCreateCall.setImage(imageId);
        scalewayCreateCall.setName(name);
        scalewayCreateCall.setOrganization(orgId);
        scalewayCreateCall.setTags(definition.getTags());


        // Check for minimum volumes; some commercialTypes require a certain amount of volumes to be present
        // to avoid frustration we add them automatically if not explictly done by the caller
        List<VolumeWrapper> extraVolumes = CommercialTypeMapper.minimalVolumesFromCommercialType(definition.getCommercialType()).build();
        if (definition.getVolumes() != null) {
            // If there are extra volumes, convert them, but then it is the user's responsability to match them to SW's will.
            extraVolumes = definition.getVolumes().build();
        }


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
        scalewayCreateCall.setVolumes(vols);

        int sumOfGbsSpecified = extraVolumes.stream().map(VolumeWrapper::getSizeInGb).mapToInt(Integer::intValue).sum();
        log.info(String.format("Total volume size %dGb.", sumOfGbsSpecified));

        // Some fixed stuff, to make life easier.
        scalewayCreateCall.setEnableIpv6(false);
        scalewayCreateCall.setBootType("local"); // boot from "grub" locally installed; does not work on baremetal types, nor ARM i guess...
        scalewayCreateCall.setExtraNetworks(null);

        Call<ServerSingleWrapper> call = computeClient.createServer(scalewayCreateCall);
        Response<ServerSingleWrapper> callExecution = call.execute();
        makeSureResponseSucessfull(callExecution);

        Server createdServer = Objects.requireNonNull(callExecution.body()).getServer();
        String createdServerId = createdServer.getId();

        String cloudInitString = null;
        if (definition.getCloudInitUrl() != null) {
            log.debug("Using cloud-init URL " + definition.getCloudInitUrl());
            cloudInitString = "#include" + "\n" + definition.getCloudInitUrl() + "\n";
        }

        if (cloudInitString == null && StringUtils.isNotEmpty(definition.getCloudInitRaw())) {
            log.debug("Using raw cloudinit string " + definition.getCloudInitRaw());
            cloudInitString = definition.getCloudInitRaw();
        }

        if (cloudInitString != null) {
            log.debug("Setting cloud-init URL for server " + createdServerId);
            Response cloudInitResponse = computeClient.setServerCloudInitData(createdServerId, RequestBody.create(MediaType.parse("text/plain"), cloudInitString)).execute();
            makeSureResponseSucessfull(cloudInitResponse);
        } else {
            log.warn("No Cloud-init URL nor Raw specified for server " + createdServerId);
        }

        return definition.isPowerOn() ? this.powerOnServer(createdServerId, definition.isWaitForReady()) : createdServer;
    }
}
