package com.zzz.todolist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zzz.todolist.entity.ListInfo;

public interface ListInfoRepository extends JpaRepository<ListInfo, Long> {
    List<ListInfo> findByListType(String listType);
    List<ListInfo> findByIsRepeatTrue();
} 