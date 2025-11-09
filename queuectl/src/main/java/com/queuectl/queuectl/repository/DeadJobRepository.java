package com.queuectl.queuectl.repository;

import com.queuectl.queuectl.model.DeadJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadJobRepository extends MongoRepository<DeadJob, String> {}
