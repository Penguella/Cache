package cache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        String filePath = "E:\\JavaProiecte\\Cache3\\src\\cache\\tasks.txt";
        TaskManager taskManager = new TaskManager(filePath);
        
        JFrame frame = new JFrame("Task Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 500);  // Increased height for the legend
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> taskList = new JList<>(listModel);
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Checkbox to show/hide completed tasks
        JCheckBox showCompletedCheckBox = new JCheckBox("Show Completed Tasks");
        showCompletedCheckBox.setSelected(true); // Show completed tasks by default

        // Method to update the task list display
        Runnable updateTaskList = () -> {
            listModel.clear();
            boolean showCompleted = showCompletedCheckBox.isSelected();
            for (Task task : taskManager.getFilteredTasks(showCompleted)) {
                listModel.addElement(task.toString());
            }
        };

        updateTaskList.run(); // Initial load of tasks
        
        JPanel panel = new JPanel();
        JTextField taskField = new JTextField(20);
        JButton addButton = new JButton("Add Task");
        JButton completeButton = new JButton("Complete Task");
        panel.add(taskField);
        panel.add(addButton);
        panel.add(completeButton);
        panel.add(showCompletedCheckBox);
        frame.add(panel, BorderLayout.SOUTH);
        
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String description = taskField.getText();
                taskManager.addTask(description);
                updateTaskList.run();
                taskField.setText("");
            }
        });
        
        completeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedTask = listModel.get(selectedIndex);
                    String[] parts = selectedTask.split(",");
                    int id = Integer.parseInt(parts[0].trim());
                    taskManager.completeTask(id);
                    updateTaskList.run();
                }
            }
        });

        showCompletedCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateTaskList.run();
            }
        });

        // Additional buttons and their action listeners
        JButton measureFileReadTimeButton = new JButton("Measure File Read Time");
        JButton measureCacheReadTimeButton = new JButton("Measure Cache Read Time");
        JButton invalidateCacheButton = new JButton("Invalidate Cache");
        JButton readTasksButton = new JButton("Read Tasks");

        // Color the buttons used for developer testing
        measureFileReadTimeButton.setBackground(Color.YELLOW);
        measureCacheReadTimeButton.setBackground(Color.YELLOW);
        invalidateCacheButton.setBackground(Color.YELLOW);
        readTasksButton.setBackground(Color.YELLOW);

        measureFileReadTimeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long time = taskManager.measureFileReadTime(filePath);
                JOptionPane.showMessageDialog(frame, "File Read Time: " + time + " ns");
            }
        });

        measureCacheReadTimeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long time = taskManager.measureCacheReadTime();
                JOptionPane.showMessageDialog(frame, "Cache Read Time: " + time + " ns");
            }
        });

        invalidateCacheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taskManager.invalidateCache();
                JOptionPane.showMessageDialog(frame, "Cache invalidated.");
                updateTaskList.run();
            }
        });

        readTasksButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taskManager.readTasks(filePath);
                updateTaskList.run();
            }
        });

        panel.add(measureFileReadTimeButton);
        panel.add(measureCacheReadTimeButton);
        panel.add(invalidateCacheButton);
        panel.add(readTasksButton);

        // Add a legend for developer buttons
        JPanel legendPanel = new JPanel();
        JLabel legendLabel = new JLabel("Yellow buttons are for testing purposes only and not part of the final product.");
        legendPanel.add(legendLabel);
        legendPanel.setBackground(Color.LIGHT_GRAY);
        frame.add(legendPanel, BorderLayout.NORTH);

        frame.setVisible(true);
    }
}
