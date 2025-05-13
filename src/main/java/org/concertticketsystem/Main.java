package org.concertticketsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Load configuration
            Config config = Config.getInstance();
            String nodeId = config.getNodeId();
            int port = config.getPort();

            logger.info("Starting node {} on port {}", nodeId, port);

            // Initialize node
            Node node = new Node(nodeId, port);
            node.start();

            // Keep the application running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down node {}", nodeId);
                node.stop();
            }));

            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error starting node", e);
            System.exit(1);
        }
    }
}