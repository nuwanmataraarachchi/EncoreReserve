package org.concertticketsystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties;

    private Config() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("zookeeper.properties")) {
            if (input == null) {
                throw new IOException("Unable to find zookeeper.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public String getNodeId() {
        return properties.getProperty("node.id", "node1");
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("node.port", "50051"));
    }

    public String getZooKeeperConnectString() {
        return properties.getProperty("zookeeper.connect", "localhost:2181");
    }

    public String[] getNodeAddresses() {
        String addresses = properties.getProperty("node.addresses", "localhost:50051,localhost:50052,localhost:50053");
        return addresses.split(",");
    }
}