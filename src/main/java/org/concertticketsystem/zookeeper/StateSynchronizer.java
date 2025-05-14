package org.concertticketsystem.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.concertticketsystem.NodeServer;
import org.concertticketsystem.proto.Concert;

import java.util.ArrayList;
import java.util.List;

public class StateSynchronizer implements Watcher {
    private static final Logger logger = LogManager.getLogger(StateSynchronizer.class);
    private final ZooKeeper zooKeeper;
    private final NodeServer node;
    private final String znodePath = "/concerts";

    public StateSynchronizer(ZooKeeper zooKeeper, NodeServer node) throws KeeperException, InterruptedException {
        this.zooKeeper = zooKeeper;
        this.node = node;
        if (zooKeeper.exists(znodePath, false) == null) {
            zooKeeper.create(znodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        syncState();
    }

    private void syncState() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(znodePath, this);
        List<Concert> concerts = new ArrayList<>();
        for (String child : children) {
            byte[] data = zooKeeper.getData(znodePath + "/" + child, false, null);
            try {
                Concert concert = Concert.parseFrom(data);
                concerts.add(concert);
            } catch (Exception e) {
                logger.error("Error parsing concert data for {}", child, e);
            }
        }
        logger.info("Node {} synced {} concerts", node.getNodeId(), concerts.size());
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                syncState();
            } catch (Exception e) {
                logger.error("Error syncing state", e);
            }
        }
    }
}