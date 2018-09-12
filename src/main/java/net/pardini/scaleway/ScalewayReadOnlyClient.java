package net.pardini.scaleway;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.depaginator.ScalewayDepaginator;
import net.pardini.scaleway.model.Image;
import net.pardini.scaleway.model.Images;
import net.pardini.scaleway.model.Organization;
import net.pardini.scaleway.model.Server;
import net.pardini.scaleway.wrapper.ServerListWrapper;
import net.pardini.scaleway.wrapper.ServerSingleWrapper;
import retrofit2.Response;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class ScalewayReadOnlyClient extends ScalewayRetrofitClient {
    ScalewayReadOnlyClient(String authToken, ScalewayRegion region, Boolean logHttpRequestsAndResponses) {
        super(authToken, region, logHttpRequestsAndResponses);
    }

    ScalewayReadOnlyClient(String authToken, ScalewayRegion region) {
        super(authToken, region, false);
    }

    @SneakyThrows
    public List<Organization> getAllOrganizations() {
        return Objects.requireNonNull(accountClient.getAllOrganizations().execute().body()).getOrganizations();
    }

    @SneakyThrows
    public Optional<Organization> getOneAndOnlyOrganization() {
        return Optional.ofNullable(this.getAllOrganizations().get(0));
    }

    @SneakyThrows
    public List<Server> getAllServers() {
        return new ScalewayDepaginator<ServerListWrapper, Server>().depaginate(execute -> Objects.requireNonNull(execute.body()).getServers(), currPage -> computeClient.getAllServers(DEFAULT_PAGE_SIZE, currPage));
    }

    @SneakyThrows
    public List<Server> findServersByName(String name) {
        return new ScalewayDepaginator<ServerListWrapper, Server>().depaginate(execute -> Objects.requireNonNull(execute.body()).getServers(), currPage -> computeClient.findServerByName(DEFAULT_PAGE_SIZE, currPage, name));
    }

    @SneakyThrows
    public Optional<Server> findServerByName(String name) {
        List<Server> serverList = new ScalewayDepaginator<ServerListWrapper, Server>().depaginate(execute -> Objects.requireNonNull(execute.body()).getServers(), currPage -> computeClient.findServerByName(DEFAULT_PAGE_SIZE, currPage, name));
        return serverList.size() == 1 ? Optional.ofNullable(serverList.get(0)) : Optional.empty();
    }

    @SneakyThrows
    public List<Image> getAllImages() {
        return new ScalewayDepaginator<Images, Image>().depaginate(execute -> Objects.requireNonNull(execute.body()).getImages(), currPage -> computeClient.getAllImages(DEFAULT_PAGE_SIZE, currPage));
    }

    @SneakyThrows
    public List<Image> getArchImages(String arch) {
        return new ScalewayDepaginator<Images, Image>().depaginate(execute -> Objects.requireNonNull(execute.body()).getImages(), currPage -> computeClient.getArchImages(DEFAULT_PAGE_SIZE, currPage, arch));
    }

    @SneakyThrows
    public List<Image> getArchImagesByName(String arch, String name) {
        return new ScalewayDepaginator<Images, Image>().depaginate(execute -> Objects.requireNonNull(execute.body()).getImages(), currPage -> computeClient.getArchImagesByName(DEFAULT_PAGE_SIZE, currPage, arch, name));
    }

    @SneakyThrows
    public Image getBestArchImageByName(String arch, String name) {
        List<Image> archImagesByName = this.getArchImagesByName(arch, name);
        // last modified first
        archImagesByName.sort(Comparator.comparing(Image::getModificationDate).reversed());
        return archImagesByName.get(0);
    }

    @SneakyThrows
    public Optional<Server> getSpecificServer(String id) {
        Response<ServerSingleWrapper> execute = computeClient.getServerById(id).execute();
        return Optional.ofNullable(execute.body() != null ? execute.body().getServer() : null);
    }
}
