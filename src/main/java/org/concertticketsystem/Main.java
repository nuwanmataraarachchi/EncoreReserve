package org.concertticketsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        String zkConnectString = "localhost:2181";
        String nodeId = "node1";
        int port = 50051;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--node-id")) {
                nodeId = args[++i];
            } else if (args[i].equals("--port")) {
                port = Integer.parseInt(args[++i]);
            }
        }

        try {
            NodeServer server = new NodeServer(zkConnectString, nodeId, port);
            server.start();
            server.blockUntilShutdown();
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            System.exit(1);
        }
    }
}