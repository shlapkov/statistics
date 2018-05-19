package com.n26.rest;

import com.n26.api.TransactionManager;
import com.n26.model.Statistics;
import com.n26.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API
 */
@RestController
public class TransactionRestController {

    @Autowired
    TransactionManager transactionManager;

    @GetMapping("/statistics")
    public Statistics statistics() {
        return transactionManager.getStatistic();
    }

    @PostMapping("/transactions")
    ResponseEntity<?> add(@RequestBody Transaction transaction) {
        if(transactionManager.add(transaction)) {
            return ResponseEntity.status(201).build();
        } else {
            return ResponseEntity.status(204).build();
        }
    }

}
