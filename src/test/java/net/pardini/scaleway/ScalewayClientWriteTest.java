package net.pardini.scaleway;

import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.Server;
import net.pardini.scaleway.request.ServerDefinition;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static net.pardini.scaleway.ScalewayClientReadonlyTest.TEST_TOKEN;

@Slf4j
public class ScalewayClientWriteTest {

    public static final String CLOUD_INIT_URL = "https://cloud-init.pardini.net/base";
    private ScalewayClient client;

    @Before
    public void setUp() throws Exception {
        client = new ScalewayClient(TEST_TOKEN, ScalewayRegion.AMS1);
    }

    @Test
    public void testCreatingSimpleServer() {
        Server createdServer = client.startServer(ServerDefinition.builder()
                .name("newapi-test-123")
                .cloudInitUrl(CLOUD_INIT_URL)
                .commercialType(Server.CommercialType.START_1_S)
                .os("Ubuntu Xenial")
                .tags(Arrays.asList("uma", "tag", "aqui"))
                .build());
        assertAndLogCreatedServerDetails(createdServer);
    }

    private void assertAndLogCreatedServerDetails(Server createdServer) {
        log.info(createdServer.toString());
        log.info("IP address is: " + createdServer.getPublicIp().getAddress());
    }

    @Test
    public void testCreatingComplexServer() {
        Server createdServer = client.startServer(ServerDefinition.builder()
                .name("newapi-test-123-large")
                .cloudInitUrl(CLOUD_INIT_URL)
                .commercialType(Server.CommercialType.START_1_L)
                .os("Ubuntu Xenial")
                .tags(Arrays.asList("uma", "tag", "large", "desta vez eh large"))
                .build());
        assertAndLogCreatedServerDetails(createdServer);
    }


    @Test
    public void testCreatingBaremetalServer() {
        Server createdServer = client.startServer(ServerDefinition.builder()
                .name("newapi-test-123-baremetal")
                .cloudInitUrl(CLOUD_INIT_URL)
                .commercialType(Server.CommercialType.C_2_L)
                .os("Ubuntu Xenial")
                .tags(Arrays.asList("bare", "metal", "Stuff"))
                .build());
        assertAndLogCreatedServerDetails(createdServer);
    }


}