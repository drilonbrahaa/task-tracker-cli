import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {

    public static void main(String[] args) {
        File database = new File("database.json");
        Scanner input = new Scanner(System.in);

        if (!database.exists()) {
            createDatabase(database);
        }

        while (true) {
            List<Task> tasks = getTasks(database);
            System.out.print("Enter command: ");
            String command = input.nextLine().trim();
            Matcher matcher;
            if (command.equals("exit")) {
                break;
            } else if (command.equals("list")) {
                listTasks(tasks);
            } else if ((matcher = Patterns.listFilter.matcher(command)).find()) {
                switch (matcher.group(1)) {
                    case "todo":
                        listTasksFiltered(tasks, Status.TO_DO);
                        break;
                    case "in-progress":
                        listTasksFiltered(tasks, Status.IN_PROGRESS);
                        break;
                    case "done":
                        listTasksFiltered(tasks, Status.DONE);
                        break;
                    default:
                        break;
                }
            } else if ((matcher = Patterns.add.matcher(command)).find()) {
                newTask(database, tasks, matcher.group(1).trim());
            } else if ((matcher = Patterns.update.matcher(command)).find()) {
                updateTask(database, tasks, Integer.parseInt(matcher.group(1)) - 1, matcher.group(2).trim());
            } else if ((matcher = Patterns.delete.matcher(command)).find()) {
                deleteTask(database, tasks, Integer.parseInt(matcher.group(1)) - 1);
            } else if ((matcher = Patterns.markInProgress.matcher(command)).find()) {
                markInProgress(database, tasks, Integer.parseInt(matcher.group(1)) - 1);
            } else if ((matcher = Patterns.markDone.matcher(command)).find()) {
                markDone(database, tasks, Integer.parseInt(matcher.group(1)) - 1);
            } else {
                System.out.println("Invalid command!");
            }
        }
    }

    private static class Patterns {
        private static final Pattern add = Pattern.compile("^add\\s+\"(.+)\"$");
        private static final Pattern update = Pattern.compile("^update\\s+([1-9]|10)\\s+\"(.+)\"$");
        private static final Pattern delete = Pattern.compile("^delete\\s+([1-9]|10)$");
        private static final Pattern markInProgress = Pattern.compile("^mark-in-progress\\s+([1-9]|10)$");
        private static final Pattern markDone = Pattern.compile("^mark-done\\s+([1-9]|10)$");
        private static final Pattern listFilter = Pattern.compile("^list\\s+(todo|in-progress|done)$");
    }

    public static List<Task> getTasks(File database) {
        List<Task> tasks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(database))) {
            Task task = new Task();
            String line;
            while ((line = br.readLine()) != null && !(line.equals("  }") || line.equals("]"))) {
                if (!(line.equals("[") || line.equals("  {") || line.equals("  },"))) {
                    if (line.startsWith("    \"id\"")) {
                        task.setId(Character.getNumericValue(line.charAt(10)));
                    } else if (line.startsWith("    \"description\"")) {
                        task.setDescription(line.substring(20, line.length() - 2));
                    } else if (line.startsWith("    \"status\"")) {
                        task.setStatus(Status.valueOf(line.substring(15, line.length() - 2)));
                    } else if (line.startsWith("    \"createdAt\"")) {
                        task.setCreatedAt(LocalDateTime.parse(line.substring(18, line.length() - 2)));
                    } else if (line.startsWith("    \"updatedAt\"")) {
                        if (line.charAt(17) == '"') {
                            task.setUpdatedAt(LocalDateTime.parse(line.substring(18, line.length() - 1)));
                        } else {
                            task.setUpdatedAt(null);
                        }
                        tasks.add(task);
                        task = new Task();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    public static void createDatabase(File database) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(database))) {
            bw.write("[");
            bw.newLine();
            bw.write("]");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveTasks(File database, List<Task> tasks) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(database))) {
            bw.write("[");
            bw.newLine();
            for (int i = 0; i < tasks.size() - 1; i++) {
                bw.write(tasks.get(i).toString() + ',');
                bw.newLine();
            }
            bw.write(tasks.getLast().toString());
            bw.newLine();
            bw.write("]");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void newTask(File database, List<Task> tasks, String description) {
        if (tasks.size() == 10) {
            System.out.println("Task table is full!");
        } else if (description.isEmpty()) {
            System.out.println("Description must not be empty!");
        } else {
            tasks.add(new Task(tasks.size(), description, Status.TO_DO, LocalDateTime.now(), null));
            saveTasks(database, tasks);
        }
    }

    public static void updateTask(File database, List<Task> tasks, int id, String description) {
        if (tasks.size() < id) {
            System.out.println("No task with ID: " + (id + 1));
        } else if (description.isEmpty()) {
            System.out.println("Description must not be empty!");
        } else {
            tasks.get(id).setDescription(description);
            tasks.get(id).setUpdatedAt(LocalDateTime.now());
            saveTasks(database, tasks);
        }
    }

    public static void deleteTask(File database, List<Task> tasks, int id) {
        if (tasks.size() < id) {
            System.out.println("No task with ID: " + (id + 1));
        } else {
            tasks.remove(id);
            saveTasks(database, tasks);
        }
    }

    public static void markInProgress(File database, List<Task> tasks, int id) {
        if (tasks.size() < id) {
            System.out.println("No task with ID: " + (id + 1));
        } else {
            tasks.get(id).setStatus(Status.IN_PROGRESS);
            saveTasks(database, tasks);
        }
    }

    public static void markDone(File database, List<Task> tasks, int id) {
        if (tasks.size() < id) {
            System.out.println("No task with ID: " + (id + 1));
        } else {
            tasks.get(id).setStatus(Status.DONE);
            saveTasks(database, tasks);
        }
    }

    public static void listTasks(List<Task> tasks) {
        tasks.forEach(task -> System.out.println(task.taskView()));
    }

    public static void listTasksFiltered(List<Task> tasks, Status status) {
        tasks.forEach(task -> {
            if (task.getStatus().equals(status)) {
                System.out.println(task.taskView());
            }
        });
    }
}
