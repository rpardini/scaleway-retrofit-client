package net.pardini.scaleway;

import lombok.extern.slf4j.Slf4j;
import net.pardini.scaleway.model.Server;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ScalewayClientTest {

    public static final String TEST_TOKEN = "FAKEEE";
    public static final String EXISTING_SERVER_ID = "a7ca2d20-4eb1-4110-ae90-daab02752a64";

    private ScalewayClient client;

    @Before
    public void setUp() throws Exception {
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
    public void getGettingSpecificServer() {
        Server specificServer = client.getSpecificServer(EXISTING_SERVER_ID);
        log.info("Here is the thing");
        log.info(specificServer.toString());
        assertEquals("Correct server returned", "C2L", specificServer.getCommercialType());
        assertNotNull("should have a volume id", specificServer.getVolumes().getAdditionalProperties().get("0").getVolumeType());
        assertEquals("should have a volume exactly", specificServer.getVolumes().getAdditionalProperties().get("0").getVolumeType(), "l_ssd");
        
    }
}