package com.nexastudio.preview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process Manager for managing preview processes.
 * Handles starting, stopping, and monitoring of dev server processes.
 */
@Component
public class ProcessManager {

    private static final Logger log = LoggerFactory.getLogger(ProcessManager.class);

    private final Map<UUID, ProcessInfo> processes = new ConcurrentHashMap<>();

    /**
     * Register a process
     */
    public void registerProcess(UUID projectId, Process process) {
        processes.put(projectId, new ProcessInfo(process, System.currentTimeMillis()));
        log.info("Registered process for project: {}", projectId);
    }

    /**
     * Stop a process
     */
    public void stopProcess(UUID projectId) {
        ProcessInfo info = processes.remove(projectId);
        if (info != null && info.process.isAlive()) {
            info.process.destroyForcibly();
            log.info("Stopped process for project: {}", projectId);
        }
    }

    /**
     * Check if a process is running
     */
    public boolean isRunning(UUID projectId) {
        ProcessInfo info = processes.get(projectId);
        return info != null && info.process.isAlive();
    }

    /**
     * Get process uptime in milliseconds
     */
    public long getUptime(UUID projectId) {
        ProcessInfo info = processes.get(projectId);
        if (info != null) {
            return System.currentTimeMillis() - info.startTime;
        }
        return 0;
    }

    /**
     * Stop all processes
     */
    public void stopAll() {
        processes.keySet().forEach(this::stopProcess);
        log.info("Stopped all preview processes");
    }

    /**
     * Get count of running processes
     */
    public int getRunningCount() {
        return (int) processes.values().stream()
                .filter(p -> p.process.isAlive())
                .count();
    }

    /**
     * Process info holder
     */
    private record ProcessInfo(Process process, long startTime) {}
}
