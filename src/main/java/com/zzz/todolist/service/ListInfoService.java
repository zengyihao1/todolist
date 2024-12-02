package com.zzz.todolist.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zzz.todolist.entity.ListInfo;
import com.zzz.todolist.repository.ListInfoRepository;

@Service
public class ListInfoService {
    
    @Autowired
    private ListInfoRepository listInfoRepository;
    
    // 创建待办事项
    public ListInfo create(ListInfo listInfo) {
        return listInfoRepository.save(listInfo);
    }
    
    // 获取所有待办事项
    public List<ListInfo> findAll() {
        return listInfoRepository.findAll();
    }
    
    // 根据ID获取待办事项
    public Optional<ListInfo> findById(Long id) {
        return listInfoRepository.findById(id);
    }
    
    // 更新待办事项
    public ListInfo update(ListInfo listInfo) {
        return listInfoRepository.save(listInfo);
    }
    
    // 删除待办事项
    public void delete(Long id) {
        listInfoRepository.deleteById(id);
    }
} 