package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // タスクをすべて取得するAPI（キャッシュ適用）
    @GetMapping
    @Cacheable("tasksCache") // ここを追加
    public List<Task> getAllTasks() {
        System.out.println("データベースからタスクを取得中...");
        return taskRepository.findAll();
    }

    // タスクを新規作成するAPI（キャッシュクリア）
    @PostMapping
    @CacheEvict(value = "tasksCache", allEntries = true) // ここを追加
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

    // 特定のIDのタスクを取得するAPI
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 特定のIDのタスクを更新するAPI（キャッシュクリア）
    @PutMapping("/{id}")
    @CacheEvict(value = "tasksCache", allEntries = true) // ここを追加
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            Task existingTask = task.get();
            existingTask.setTitle(taskDetails.getTitle());
            existingTask.setDescription(taskDetails.getDescription());
            existingTask.setCompleted(taskDetails.isCompleted());
            return ResponseEntity.ok(taskRepository.save(existingTask));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 特定のIDのタスクを削除するAPI（キャッシュクリア）
    @DeleteMapping("/{id}")
    @CacheEvict(value = "tasksCache", allEntries = true) // ここを追加
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}