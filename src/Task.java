import java.time.LocalDateTime;

public class Task {
    private int id;
    private String description;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task(int id, String description, Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Task() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "  {\n    \"id\": " + id + ",\n    \"description\": \"" + description
                + "\",\n    \"status\": \"" + status + "\",\n    \"createdAt\": \"" + createdAt
                + "\",\n    \"updatedAt\": " + updatedAt + "\n  }";
    }

    public String taskView() {
        return "Task: " + (id + 1) + ", Description: " + description + ", Status: " + status;
    }
}

