package net.pardini.scaleway.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class VolumeWrapper {
    private final String name;
    private final int sizeInGb;
    private String type = "l_ssd";
}
