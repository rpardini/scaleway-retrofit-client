package net.pardini.scaleway.request;

import lombok.Builder;
import lombok.Data;
import net.pardini.scaleway.model.Image;
import net.pardini.scaleway.model.Organization;
import net.pardini.scaleway.model.Server;
import net.pardini.scaleway.wrapper.VolumeBuilder;

import java.util.List;

@Data
@Builder
public class ServerDefinition {
    private String name;
    private Server.CommercialType commercialType;

    private Image image;
    private Organization organization;

    private List<String> tags;
    private String cloudInitUrl;
    private String cloudInitRaw;
    private VolumeBuilder volumes;

    private String imageId;
    private String organizationId;
    
    @Builder.Default
    private String os = "Ubuntu Xenial";

    @Builder.Default
    private boolean waitForReady = true;
    @Builder.Default
    private boolean powerOn = true;


}
