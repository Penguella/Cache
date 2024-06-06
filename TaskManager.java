package cache;

import java.io.*;
import java.util.*;

public class TaskManager {
    private static final int MAX_CACHE_SIZE = 100; // Size limit for LRU cache
    private static final long CACHE_EXPIRATION_TIME = 2 * 60 * 1000; // 2 minutes in milliseconds

    private List<Task> tasks;
    private Map<Integer, Task> taskCache;
    private int nextId = 1;
    private Timer cacheTimer;
    private String filePath;

    public TaskManager(String filePath) {
        this.filePath = filePath;
        tasks = new ArrayList<>();
        taskCache = new LinkedHashMap<Integer, Task>(MAX_CACHE_SIZE, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Integer, Task> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
        loadTasksFromFile(filePath);
        startCacheTimer();
    }

    private void startCacheTimer() {
        if (cacheTimer != null) {
            cacheTimer.cancel();
        }
        cacheTimer = new Timer(true);
        cacheTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                invalidateCache();
            }
        }, CACHE_EXPIRATION_TIME, CACHE_EXPIRATION_TIME);
    }

    public void loadTasksFromFile(String filePath) {
        tasks.clear();
        taskCache.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.err.println("Skipping invalid line: " + line);
                    continue; // Skip invalid lines
                }
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String description = parts[1].trim();
                    String status = parts[2].trim();
                    if (!status.equals("todo") && !status.equals("done")) {
                        System.err.println("Invalid status in line: " + line);
                        continue; // Skip lines with invalid status
                    }
                    Task task = new Task(id, description, status);
                    tasks.add(task);
                    taskCache.put(id, task);
                    nextId = Math.max(nextId, id + 1);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid ID in line: " + line);
                    // Skip lines with invalid ID
                }
            }
            System.out.println("Tasks loaded from file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTasksToFile() {
        File file = new File(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Task task : tasks) {
                bw.write(task.getId() + "," + task.getDescription() + "," + task.getStatus());
                bw.newLine();
            }
            System.out.println("Tasks saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTask(String description) {
        Task task = new Task(nextId++, description, "todo");
        tasks.add(task);
        taskCache.put(task.getId(), task);
        saveTasksToFile();
    }

    public void completeTask(int id) {
        Task task = taskCache.get(id);
        if (task != null) {
            task.setStatus("done");
            saveTasksToFile();
        }
    }

    public List<Task> getFilteredTasks(boolean showCompleted) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (showCompleted || task.getStatus().equals("todo")) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    public Map<Integer, Task> getTasks() {
        return taskCache;
    }

    public long measureFileReadTime(String filePath) {
        long startTime = System.nanoTime();
        loadTasksFromFile(filePath);
        long endTime = System.nanoTime();
        System.out.println("File Read Time: " + (endTime - startTime) + " ns");
        return endTime - startTime;
    }

    public long measureCacheReadTime() {
        long startTime = System.nanoTime();
        Map<Integer, Task> cachedTasks = getTasks();
        long endTime = System.nanoTime();
        System.out.println("Cache Read Time: " + (endTime - startTime) + " ns");
        return endTime - startTime;
    }

    public void invalidateCache() {
        taskCache.clear();
        System.out.println("Cache invalidated.");
        startCacheTimer(); // Reset the timer when cache is invalidated manually
    }

    public void readTasks(String filePath) {
        if (!taskCache.isEmpty()) {
            System.out.println("Tasks read from cache.");
        } else {
            loadTasksFromFile(filePath);
        }
    }
}
