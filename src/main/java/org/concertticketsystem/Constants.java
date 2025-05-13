package org.concertticketsystem;

public class Constants {
    public static final String ZK_ROOT_PATH = "/concert-tickets";
    public static final String ZK_NODES_PATH = ZK_ROOT_PATH + "/nodes";
    public static final String ZK_LEADER_PATH = ZK_ROOT_PATH + "/leader";
    public static final String ZK_CONCERTS_PATH = ZK_ROOT_PATH + "/concerts";
    public static final int ZK_SESSION_TIMEOUT = 3000;
    public static final int ZK_CONNECTION_TIMEOUT = 5000;
}