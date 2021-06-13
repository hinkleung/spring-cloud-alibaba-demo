package com.itmuch.contentcenter.domain.entity.transactionlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "rocketmq_transaction_log")
public class RocketmqTransactionLog {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name = "transaction_id")
    private String transactionId;

    private String log;

}