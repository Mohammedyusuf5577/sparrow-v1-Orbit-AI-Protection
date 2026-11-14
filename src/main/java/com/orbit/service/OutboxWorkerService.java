package com.orbit.service;

public interface OutboxWorkerService {
    void processOutboxBatch();
}
