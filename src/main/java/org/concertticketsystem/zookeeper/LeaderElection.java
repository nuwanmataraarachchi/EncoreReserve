package org.concertticketsystem.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.concertticketsystem.NodeServer;

import java.io.IOException;

public class LeaderElection implements Watcher {
    private static final Logger logger = LogManager.getLogger(LeaderElection.class);
    private final ZooKeeper zooKeeper;
    private final String electionPath = "/election";
    private final NodeServer node;
    private String znodePath;

    public LeaderElection(ZooKeeper zooKeeper, NodeServer node) throws IOException, KeeperException, InterruptedException {
        this.zooKeeper = zooKeeper;
        this.node = node;
        if (zooKeeper.exists(electionPath, false) == null) {
            zooKeeper.create(electionPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        participate();
    }

    private void participate() throws KeeperException, InterruptedException {
        znodePath = zooKeeper.create(
                electionPath + "/n_",
                node.getNodeId().getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL
        );
        logger.info("Node {} created znode: {}", node.getNodeId(), znodePath);
        checkLeadership();
    }

    private void checkLeadership() throws KeeperException, InterruptedException {
        java.util.List<String> children = zooKeeper.getChildren(electionPath, this);
        children.sort(String::compareTo);
        String smallest = children.get(0);
        if (znodePath.endsWith(smallest)) {
            logger.info("Node {} is the leader", node.getNodeId());
        } else {
            String watchedZnode = electionPath + "/" + children.get(children.indexOf(znodePath.substring(electionPath.length() + 1)) - 1);
            zooKeeper.exists(watchedZnode, this);
            logger.info("Node {} watching znode: {}", node.getNodeId(), watchedZnode);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {
            try {
                checkLeadership();
            } catch (Exception e) {
                logger.error("Error in leader election", e);
            }
        }
    }
}