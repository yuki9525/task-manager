package com.example.taskmanager;

import com.example.taskmanager.entity.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPerformCrudOperationsForTasks() throws Exception {
        // Step 1: タスクを新規作成
        Task newTask = new Task("読書", "プログラミングに関する本を読む");
        MvcResult postResult = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = postResult.getResponse().getContentAsString();
        Task createdTask = objectMapper.readValue(responseBody, Task.class);
        Long createdTaskId = createdTask.getId();

        // Step 2: 全てのタスクを取得（1件増えていることを確認）
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("読書")));

        // Step 3: 特定のタスクを取得
        mockMvc.perform(get("/api/tasks/{id}", createdTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("読書")));

        // Step 4: タスクを更新
        createdTask.setTitle("読書（完了）");
        mockMvc.perform(put("/api/tasks/{id}", createdTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("読書（完了）")));

        // Step 5: タスクを削除
        mockMvc.perform(delete("/api/tasks/{id}", createdTaskId))
                .andExpect(status().isOk());

        // Step 6: 全てのタスクを取得（0件になっていることを確認）
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}