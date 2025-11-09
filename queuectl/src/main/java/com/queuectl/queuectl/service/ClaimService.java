package com.queuectl.queuectl.service;

import com.queuectl.queuectl.model.Job;
import com.queuectl.queuectl.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final MongoOperations mongo;


    public Job claimNextJob() {
        String now = TimeUtils.nowIstString(); //Get IST timestamp as String


        Query query = new Query();
        query.addCriteria(where("state").is("pending"));
        query.with(Sort.by(Sort.Direction.ASC, "createdAt")); // FIFO order

        // Update to mark job as 'running'
        Update update = new Update()
                .set("state", "running")
                .set("lockedAt", now)
                .set("updatedAt", now)
                .inc("attempts", 1);

        //Return the modified job after update
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        Job claimedJob = mongo.findAndModify(query, update, options, Job.class);

        if (claimedJob != null) {
            System.out.println("🔒 Claimed job: " + claimedJob.getId() +
                    " (" + claimedJob.getType() + ") at " + now);
        }

        return claimedJob;
    }
}
