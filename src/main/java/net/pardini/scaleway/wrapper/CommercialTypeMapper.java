package net.pardini.scaleway.wrapper;

import net.pardini.scaleway.model.Image;
import net.pardini.scaleway.model.Server;

public class CommercialTypeMapper {

    public static Image.Arch archFromCommercialType(Server.CommercialType commercialType) {
        switch (commercialType) {
            case ARM_64_128_GB:
            case ARM_64_64_GB:
            case ARM_64_32_GB:
            case ARM_64_16_GB:
            case ARM_64_8_GB:
            case ARM_64_4_GB:
            case ARM_64_2_GB:
                return Image.Arch.ARM_64;

            case C_2_L:
            case C_2_M:
            case C_2_S:
            case START_1_L:
            case START_1_M:
            case START_1_S:
            case START_1_XS:
            case VC_1_L:
            case VC_1_M:
            case VC_1_S:
            case X_64_120_GB:
            case X_64_60_GB:
            case X_64_30_GB:
            case X_64_15_GB:
                return Image.Arch.X_86_64;

            case C_1:
                return Image.Arch.ARM;

            default:
                throw new RuntimeException("commercialType " + commercialType.value() + " not mapped...");
        }
    }


    public static VolumeBuilder minimalVolumesFromCommercialType(Server.CommercialType commercialType) {
        VolumeBuilder volumeBuilder = VolumeBuilder.builder();
        switch (commercialType) {
            case C_1:
                break;

            case ARM_64_128_GB:
                volumeBuilder.addVolume("datadisk1", 150);
                volumeBuilder.addVolume("datadisk2", 150);
                volumeBuilder.addVolume("datadisk3", 150);
                volumeBuilder.addVolume("datadisk4", 150);
                volumeBuilder.addVolume("datadisk5", 150);
                volumeBuilder.addVolume("datadisk6", 150);
                volumeBuilder.addVolume("datadisk7", 50);
                break;

            case ARM_64_64_GB:
                volumeBuilder.addVolume("datadisk1", 150);
                volumeBuilder.addVolume("datadisk2", 150);
                volumeBuilder.addVolume("datadisk3", 150);
                volumeBuilder.addVolume("datadisk4", 150);
                volumeBuilder.addVolume("datadisk5", 150);
                break;

            case ARM_64_32_GB:
                volumeBuilder.addVolume("datadisk1", 150);
                volumeBuilder.addVolume("datadisk2", 150);
                volumeBuilder.addVolume("datadisk3", 150);
                volumeBuilder.addVolume("datadisk4", 100);

            case ARM_64_16_GB:
                volumeBuilder.addVolume("datadisk1", 150);
                volumeBuilder.addVolume("datadisk2", 150);
                volumeBuilder.addVolume("datadisk3", 50);
            case ARM_64_8_GB:
                volumeBuilder.addVolume("datadisk", 150);
                break;

            case ARM_64_4_GB:
                volumeBuilder.addVolume("datadisk", 50);
                break;

            case ARM_64_2_GB:
                break;

            case C_2_L:
            case C_2_M:
            case C_2_S:
                break;

            case START_1_L:
                volumeBuilder.addVolume("datadisk", 150);
                break;
            case START_1_M:
                volumeBuilder.addVolume("datadisk", 50);
                break;

            case START_1_S:
            case START_1_XS:
                break;


            case VC_1_L:
            case VC_1_M:
            case VC_1_S:
                throw new RuntimeException("VC1s are not available anymore. User START1!");


            case X_64_120_GB:
                volumeBuilder.addVolume("datadisk1", 150);
                volumeBuilder.addVolume("datadisk2", 150);
                volumeBuilder.addVolume("datadisk3", 150);
                volumeBuilder.addVolume("datadisk4", 150);
                volumeBuilder.addVolume("datadisk5", 150);
                volumeBuilder.addVolume("datadisk6", 150);
                volumeBuilder.addVolume("datadisk7", 50);
                break;

            case X_64_60_GB:
                volumeBuilder.addVolume("datadisk1", 150);
                volumeBuilder.addVolume("datadisk2", 150);
                volumeBuilder.addVolume("datadisk3", 150);
                volumeBuilder.addVolume("datadisk4", 150);
                volumeBuilder.addVolume("datadisk3", 50);
                break;

            case X_64_30_GB:
                volumeBuilder.addVolume("datadisk1", 150);
                volumeBuilder.addVolume("datadisk2", 150);
                volumeBuilder.addVolume("datadisk3", 50);
                break;

            case X_64_15_GB:
                volumeBuilder.addVolume("datadisk", 150);
                break;
        }
        return volumeBuilder;


    }


}
