package net.pardini.scaleway.wrapper;

import java.util.ArrayList;
import java.util.List;

public class VolumeBuilder {

    public List<VolumeWrapper> volumes = new ArrayList<>();

    public static VolumeBuilder builder() {
        return new VolumeBuilder();
    }


    public VolumeBuilder addVolume(String name, int sizeInGb) {
        volumes.add(new VolumeWrapper(name, sizeInGb));
        return this;
    }

    public List<VolumeWrapper> build() {
        return volumes;
    }
}
