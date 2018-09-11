package net.pardini.scaleway;

import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.Image;
import net.pardini.scaleway.model.Organization;
import net.pardini.scaleway.model.Server;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static net.pardini.scaleway.ScalewayClientReadonlyTest.TEST_TOKEN;

@Slf4j
public class ScalewayClientWriteTest {

    private ScalewayClient client;

    @Before
    public void setUp() throws Exception {
        client = new ScalewayClient(TEST_TOKEN, ScalewayRegion.AMS1);
    }


    @Test
    public void testCreatingSimpleServer() {
        Organization myOrg = client.getOneAndOnlyOrganization();
        Image bestUbuntuXenial = client.getBestArchImageByName("x86_64", "Ubuntu Xenial");
        Server createdServer = client.createServer(Server.CommercialType.START_1_S, bestUbuntuXenial.getId(), "newapi-test-123", myOrg.getId(), Arrays.asList("uma", "tag", "aqui"), "https://cloud-init.pardini.net/base");
        log.info("createdServer!" + createdServer.toString());
        
        
        client.powerOnServer(createdServer.getId());
    }
}