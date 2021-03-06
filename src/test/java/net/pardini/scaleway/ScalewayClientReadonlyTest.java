package net.pardini.scaleway;

import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.Image;
import net.pardini.scaleway.model.Organization;
import net.pardini.scaleway.model.Server;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class ScalewayClientReadonlyTest {

    public static final String TEST_TOKEN = System.getenv("SCW_TEST_TOKEN");
    public static final String EXISTING_SERVER_ID = "a7ca2d20-4eb1-4110-ae90-daab02752a64";

    private ScalewayReadOnlyClient client;

    @Before
    public void setUp() {
        client = new ScalewayClient(TEST_TOKEN, ScalewayRegion.PAR1);
    }

    @Test
    public void testGettingAllServers() {
        List<Server> allServers = client.getAllServers();
        log.info("Here is the server list");
        log.info(allServers.toString());
        assertTrue("has more than one server", allServers.size() > 2);
    }

    @Test
    public void testFindingExactServerByName() {
        Server theserver = client.findServerByName("logbox.par1.scaleway.miisy.me").get();
        assertEquals("found the exact server", "logbox.par1.scaleway.miisy.me", theserver.getName());
    }

    @Test
    public void testGettingAllImages() {
        List<Image> allServers = client.getAllImages();
        log.info("Here is the image list");
        log.info(allServers.toString());
        assertTrue("has more than one image", allServers.size() > 2);
    }

    @Test
    public void testGettingx64ImagesImages() {
        List<Image> allServers = client.getArchImages("x86_64");
        log.info("Here is the image list");
        log.info(allServers.toString());
        assertTrue("has more than one image", allServers.size() > 490);
    }

    @Test
    public void testGettingArm64ImagesImages() {
        List<Image> allServers = client.getArchImages("arm64");
        log.info("Here is the image list");
        log.info(allServers.toString());
        assertTrue("has more than one image", allServers.size() > 170);
    }


    @Test
    public void testGettingx64UbuntuXenialImages() {
        List<Image> allServers = client.getArchImagesByName("x86_64", "Ubuntu Xenial");
        log.info("Here is the image list");
        log.info(allServers.toString());
        assertTrue("has more than one image", allServers.size() > 100);
    }

    @Test
    public void testGettingLatestUbuntuXenialX64Image() {
        Image theImage = client.getBestArchImageByName("x86_64", "Ubuntu Xenial");
        assertNotNull("found an image", theImage);
    }

    @Test
    public void testGettingAllOrgs() {
        List<Organization> allOrganizations = client.getAllOrganizations();
        log.info("Here is the allOrganizations list");
        log.info(allOrganizations.toString());
        assertEquals("has Exactly one org", 1, allOrganizations.size());

    }

    @Test
    public void getGettingSpecificServer() {
        Server specificServer = client.getSpecificServer(EXISTING_SERVER_ID).get();
        log.info("Here is the thing");
        log.info(specificServer.toString());
        assertEquals("Correct server returned", Server.CommercialType.C_2_L, specificServer.getCommercialType());
        assertNotNull("should have a volume id", specificServer.getVolumes().getAdditionalProperties().get("0").getVolumeType());
        assertEquals("should have a volume exactly", specificServer.getVolumes().getAdditionalProperties().get("0").getVolumeType(), "l_ssd");

    }
}