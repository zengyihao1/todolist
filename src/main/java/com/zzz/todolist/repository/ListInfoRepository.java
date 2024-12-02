package com.zzz.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zzz.todolist.entity.ListInfo;

public interface ListInfoRepository extends JpaRepository<ListInfo, Long> {
} 