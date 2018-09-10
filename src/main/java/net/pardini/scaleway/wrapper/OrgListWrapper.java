package net.pardini.scaleway.wrapper;

import lombok.Data;
import net.pardini.scaleway.model.Organization;

import java.util.List;

@Data
public class OrgListWrapper {
    private List<Organization> organizations;
}
