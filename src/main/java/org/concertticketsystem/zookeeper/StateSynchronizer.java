package org.concertticketsystem.zookeeper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;
import org.concertticketsystem.Constants;
import org.concertticketsystem.Config;
import org.concertticketsystem.Node;
import org.concertticketsystem.model.Concert;
import org.concertticketsystem.model.SeatTier;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateSynchronizer {
    private static final Logger logger = LogManager.getLogger(StateSynchronizer.class);
    private final Node node;
    private ZooKeeper zooKeeper;

    public StateSynchronizer(Node node) {
        this.node = node;
    }

    public void start() throws IOException, KeeperException, InterruptedException {
        zooKeeper = new ZooKeeper(Config.getInstance().getZooKeeperConnectString(),
                Constants.ZK_SESSION_TIMEOUT, event -> {});

        // Ensure concerts path exists
        if (zooKeeper.exists(Constants.ZK_CONCERTS_PATH, false) == null) {
            zooKeeper.create(Constants.ZK_CONCERTS_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // Sync initial state
        syncState();

        // Watch for changes
        zooKeeper.getChildren(Constants.ZK_CONCERTS_PATH, event -> {
            if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                try {
                    syncState();
                } catch (Exception e) {
                    logger.error("Error syncing state", e);
                }
            }
        });
    }

    public void stop() {
        try {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        } catch (InterruptedException e) {
            logger.error("Error closing ZooKeeper", e);
        }
    }

    private void syncState() throws KeeperException, InterruptedException {
        List<String> concertIds = zooKeeper.getChildren(Constants.ZK_CONCERTS_PATH, false);
        Map<String, Concert> newConcerts = new HashMap<>();

        for (String concertId : concertIds) {
            byte[] data = zooKeeper.getData(Constants.ZK_CONCERTS_PATH + "/" + concertId, false, null);
            Concert concert = deserializeConcert(data, concertId);
            newConcerts.put(concertId, concert);
        }

        node.getConcerts().clear();
        node.getConcerts().putAll(newConcerts);
        logger.info("Node {} synced {} concerts", node.getNodeId(), newConcerts.size());
    }

    private Concert deserializeConcert(byte[] data, String concertId) {
        // Simplified deserialization
        String[] parts = new String(data).split("\\|");
        Map<String, SeatTier> seatTiers = new HashMap<>();
        seatTiers.put("Regular", new SeatTier("Regular", 100, 50.0));
        seatTiers.put("VIP", new SeatTier("VIP", 50, 100.0));
        return new Concert(concertId, parts[1], parts[2], seatTiers, Integer.parseInt(parts[3]));
    }
}