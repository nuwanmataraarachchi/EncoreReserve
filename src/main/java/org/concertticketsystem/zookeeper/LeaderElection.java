package org.concertticketsystem.zookeeper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;
import org.concertticketsystem.Constants;
import org.concertticketsystem.Config;
import org.concertticketsystem.Node;

import java.io.IOException;

public class LeaderElection {
    private static final Logger logger = LogManager.getLogger(LeaderElection.class);
    private final Node node;
    private ZooKeeper zooKeeper;
    private String nodePath;
    private boolean isLeader;

    public LeaderElection(Node node) {
        this.node = node;
        this.isLeader = false;
    }

    public void start() throws IOException, KeeperException, InterruptedException {
        zooKeeper = new ZooKeeper(Config.getInstance().getZooKeeperConnectString(),
                Constants.ZK_SESSION_TIMEOUT, event -> {});

        // Ensure root path exists
        if (zooKeeper.exists(Constants.ZK_ROOT_PATH, false) == null) {
            zooKeeper.create(Constants.ZK_ROOT_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        if (zooKeeper.exists(Constants.ZK_NODES_PATH, false) == null) {
            zooKeeper.create(Constants.ZK_NODES_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // Register node
        nodePath = zooKeeper.create(Constants.ZK_NODES_PATH + "/" + node.getNodeId(),
                node.getNodeId().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        // Elect leader
        electLeader();
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

    public boolean isLeader() {
        return isLeader;
    }

    private void electLeader() throws KeeperException, InterruptedException {
        // Simplified leader election: first node becomes leader
        String leaderPath = Constants.ZK_LEADER_PATH;
        try {
            zooKeeper.create(leaderPath, node.getNodeId().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            isLeader = true;
            logger.info("Node {} elected as leader", node.getNodeId());
        } catch (KeeperException.NodeExistsException e) {
            isLeader = false;
            logger.info("Node {} is follower", node.getNodeId());
        }
    }
}