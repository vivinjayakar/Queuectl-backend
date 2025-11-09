package com.queuectl.queuectl.repository;

import com.queuectl.queuectl.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByState(String state);

    // Check for duplicates
    boolean existsByTypeAndPayloadAndStateIn(String type, Map<String, Object> payload, List<String> states);
}
