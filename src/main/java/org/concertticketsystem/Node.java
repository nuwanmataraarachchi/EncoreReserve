package org.concertticketsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.concertticketsystem.model.Concert;
import org.concertticketsystem.zookeeper.LeaderElection;
import org.concertticketsystem.zookeeper.StateSynchronizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private static final Logger logger = LogManager.getLogger(Node.class);
    private final String nodeId;
    private final int port;
    private final NodeServer server;
    private final LeaderElection leaderElection;
    private final StateSynchronizer stateSynchronizer;
    private final Map<String, Concert> concerts; // Local concert state

    public Node(String nodeId, int port) {
        this.nodeId = nodeId;
        this.port = port;
        this.concerts = new ConcurrentHashMap<>();
        this.server = new NodeServer(this, port);
        this.leaderElection = new LeaderElection(this);
        this.stateSynchronizer = new StateSynchronizer(this);
    }

    public void start() throws Exception {
        server.start();
        leaderElection.start();
        stateSynchronizer.start();
        logger.info("Node {} started on port {}", nodeId, port);
    }

    public void stop() {
        stateSynchronizer.stop();
        leaderElection.stop();
        server.stop();
        logger.info("Node {} stopped", nodeId);
    }

    public String getNodeId() {
        return nodeId;
    }

    public Map<String, Concert> getConcerts() {
        return concerts;
    }

    public boolean isLeader() {
        return leaderElection.isLeader();
    }
}