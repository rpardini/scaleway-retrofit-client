package net.pardini.scaleway.wrapper;

import lombok.Data;
import net.pardini.scaleway.model.Server;

import java.util.List;

@Data
public class ServerListWrapper {

    private List<Server> servers;
}
