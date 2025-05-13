package org.concertticketsystem;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.concertticketsystem.proto.ConcertServiceImpl;

public class NodeServer {
    private static final Logger logger = LogManager.getLogger(NodeServer.class);
    private final Node node;
    private final int port;
    private Server server;

    public NodeServer(Node node, int port) {
        this.node = node;
        this.port = port;
    }

    public void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new ConcertServiceImpl(node))
                .build()
                .start();
        logger.info("gRPC server started on port {}", port);
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
            logger.info("gRPC server stopped");
        }
    }
}