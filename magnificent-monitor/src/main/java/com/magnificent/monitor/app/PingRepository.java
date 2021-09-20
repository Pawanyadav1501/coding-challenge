package com.magnificent.monitor.app;

import java.time.LocalDateTime;
import java.util.List;

public interface PingRepository {

    Ping save(Ping ping);

    List<Ping> allPingsAfter(LocalDateTime timeAtBeginningOfLatestInterval);

    void deleteAll();
}
