package com.queuectl.queuectl.repository;

import com.queuectl.queuectl.model.DLQItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DLQRepository extends MongoRepository<DLQItem, String> {
}
