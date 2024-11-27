package com.springserver.springserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@PropertySource("classpath:zookeeper.properties")
public class ZooKeeperService {
    private static final String ZNODE_PATH = "/counter";
    private static final String ZOOKEEPER_ZNODES = "zookeeper.znodes";
    private static final long START_LONG = 10000000000000L;
    private static final long EACH_SERVER_RANGE = 1000000L; // Exclusive of range end
    private static final String allZNodes = System.getProperty(ZOOKEEPER_ZNODES);
    private static final CountDownLatch connectedSignal = new CountDownLatch(1);
    private static final int RETRIES_COUNT = 10; // retries count for updating a node data

    private ZooKeeper zooKeeper;
    private long start;
    private long end;
    private long curInt;

    public ZooKeeperService() {
        connect();
        updateRange();
    }

    public Long getCurLong() {
        if (curInt >= end) {
            updateRange();
        }

        long ret = curInt;
        curInt++;
        return ret;
    }

    private void updateRange() {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return;
        }

        Stat stat = exists(ZNODE_PATH);
        if (stat == null) {
            boolean createSuccess = create(ZNODE_PATH, String.valueOf(START_LONG + EACH_SERVER_RANGE));
            if (createSuccess) {
                start = START_LONG;
                end = start + EACH_SERVER_RANGE;
                curInt = start;
                return;
            }

            stat = exists(ZNODE_PATH);
            if (stat == null) {
                log.error("Something went wrong, unable to create znode");
                return;
            } // else some other server created the node in the meantime.
        }

        boolean updateSuccess = false;
        int numRetries = 0;

        while (!updateSuccess) {
            if (numRetries > RETRIES_COUNT) {
                disconnect();
                break;
            }

            stat = new Stat();
            // multi transaction
            try {
                byte[] b = zooKeeper.getData(ZNODE_PATH, false, stat);
                String data = new String(b, StandardCharsets.UTF_8);
                long val = Long.parseLong(data);
                zooKeeper.multi(Arrays.asList(
                        Op.check(ZNODE_PATH, stat.getVersion()),   // Ensure the version matches
                        Op.setData(ZNODE_PATH, String.valueOf(val + EACH_SERVER_RANGE).getBytes(), stat.getVersion()) // Update the value
                ));

                updateSuccess = true;
                start = val;
                end = start + EACH_SERVER_RANGE;
                curInt = start;
            } catch (KeeperException.BadVersionException e) {
                log.error("Error occurred in updating data, retrying...");
            } catch (InterruptedException | KeeperException e) {
                log.error("Error occurred in updating data");
            }

            numRetries++;
        }
    }

    public void connect() {
        System.out.println(allZNodes);

        try {
            zooKeeper =  new ZooKeeper(allZNodes, 5000, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            });

            connectedSignal.await();
        } catch (IOException | InterruptedException e) {
            log.error("Error occurred: ", e);
        }
    }

    public void disconnect() {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return;
        }

        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("Error occurred: ", e);
        }
    }

    public boolean create(final String path, final String dataString) {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return false;
        }

        byte[] data = dataString.getBytes(StandardCharsets.UTF_8);
        boolean createSuccess = false;
        int numRetries = 0;
        while (!createSuccess) {
            if (numRetries > RETRIES_COUNT) {
                break;
            }

            try {
                zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
                createSuccess = true;
            } catch (KeeperException.NodeExistsException e) {
                break;
            } catch (KeeperException | InterruptedException e) {
                log.error("Error occurred in create : ", e);
            }

            numRetries++;
        }

        return createSuccess;
    }

    public Stat exists(final String path) {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return null;
        }

        try {
            return zooKeeper.exists(path, true);
        } catch (KeeperException | InterruptedException e) {
            log.error("Error occurred in exists : ", e);
        }

        return null;
    }

    public String getData(final String path) {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return null;
        }

        final Stat stat = exists(path);
        if (stat == null) return null;

        String data = null;
        try {
            byte[] b = zooKeeper.getData(path, false, stat);
            data = new String(b, StandardCharsets.UTF_8);
        } catch (KeeperException | InterruptedException e) {
            log.error("Error occurred in getData : ", e);
        }

        return data;
    }

    public void setData(final String path, final String dataString) {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return;
        }

        final Stat stat = exists(path);
        if (stat == null) {
            log.error("stat is null, may be node " + path + " is missing");
            return;
        }

        byte[] data = dataString.getBytes(StandardCharsets.UTF_8);
        try {
            zooKeeper.setData(path, data, stat.getVersion());
        } catch (KeeperException | InterruptedException e) {
            log.error("Error occurred in setData : ", e);
        }
    }

    public List<String> getChildren(final String path) {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return null;
        }

        final Stat stat = exists(path);
        if (stat == null) {
            log.error("stat is null, may be node " + path + " is missing");
            return null;
        }

        try {
            return zooKeeper.getChildren(path, false);
        } catch (KeeperException | InterruptedException e) {
            log.error("error occurred in getChildren : ", e);
        }

        return null;
    }

    public void delete(final String path) {
        if (zooKeeper == null) {
            log.error("zookeeper is null");
            return;
        }

        final Stat stat = exists(path);
        if (stat == null) {
            log.error("stat is null, may be node " + path + " is missing");
            return;
        }

        try {
            zooKeeper.delete(path, stat.getVersion());
        } catch (InterruptedException | KeeperException e) {
            log.error("error occurred in deleting : ", e);
        }
    }
}
