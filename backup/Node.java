package org.concertticketsystem;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.zookeeper.ZooKeeper;
import org.concertticketsystem.service.ConcertServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class NodeServer {
    private static final Logger logger = LogManager.getLogger(NodeServer.class);
    private final Server server;
    private final ZooKeeper zooKeeper;
    private final String nodeId;

    public NodeServer(String zkConnectString, String nodeId, int port) throws IOException {
        this.nodeId = nodeId;
        this.zooKeeper = new ZooKeeper(zkConnectString, 3000, null);
        try {
            this.server = ServerBuilder.forPort(port)
                    .addService(new ConcertServiceImpl(zooKeeper))
                    .build();
            logger.info("Node {} started on port {}", nodeId, port);
        } catch (Exception e) {
            logger.error("Failed to initialize server", e);
            throw new IOException("Failed to initialize server", e);
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on {}", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server");
            stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
            logger.info("Server shut down");
        }
        try {
            if (zooKeeper != null) {
                zooKeeper.close();
                logger.info("ZooKeeper connection closed");
            }
        } catch (InterruptedException e) {
            logger.error("Error closing ZooKeeper", e);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}