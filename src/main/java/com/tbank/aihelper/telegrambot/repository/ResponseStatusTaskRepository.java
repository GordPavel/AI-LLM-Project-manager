package com.tbank.aihelper.telegrambot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tbank.aihelper.telegrambot.entity.ResponseStatusTask;

public interface ResponseStatusTaskRepository extends JpaRepository<ResponseStatusTask, Long> {
    
    @Query(value = """
            SELECT r.* FROM response_status_task r
            INNER JOIN (
                SELECT tg_user_id, MAX(updated_at) AS max_updated_at
                FROM response_status_task
                WHERE job_id = :jobId
                GROUP BY tg_user_id
            ) latest
            ON r.tg_user_id = latest.tg_user_id
                AND r.updated_at = latest.max_updated_at
            WHERE r.job_id = :jobId
            """, nativeQuery = true)
    public List<ResponseStatusTask> findLatestByJobId(@Param("jobId") Long jobId);
}
