package com.zzz.todolist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zzz.todolist.entity.ListInfo;
import com.zzz.todolist.service.ListInfoService;

@RestController
@RequestMapping("/api/todos")
public class ListInfoController {
    
    @Autowired
    private ListInfoService listInfoService;
    
    // 创建待办事项
    @PostMapping
    public ResponseEntity<ListInfo> create(@RequestBody ListInfo listInfo) {
        return ResponseEntity.ok(listInfoService.create(listInfo));
    }
    
    // 获取所有待办事项
    @GetMapping
    public ResponseEntity<List<ListInfo>> findAll() {
        return ResponseEntity.ok(listInfoService.findAll());
    }
    
    // 根据ID获取待办事项
    @GetMapping("/{id}")
    public ResponseEntity<ListInfo> findById(@PathVariable Long id) {
        return listInfoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 更新待办事项
    @PutMapping("/{id}")
    public ResponseEntity<ListInfo> update(@PathVariable Long id, @RequestBody ListInfo listInfo) {
        return listInfoService.findById(id)
                .map(existingItem -> {
                    listInfo.setId(id);
                    return ResponseEntity.ok(listInfoService.update(listInfo));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 删除待办事项
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return listInfoService.findById(id)
                .map(item -> {
                    listInfoService.delete(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 