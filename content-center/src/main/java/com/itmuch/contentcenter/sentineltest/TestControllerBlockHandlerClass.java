package com.itmuch.contentcenter.sentineltest;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestControllerBlockHandlerClass {

    // 如果被保护的资源被限流或者降级了，就会抛BlockException
    public static String block(String a, BlockException e) {
        log.warn("限流，或者降级了", e);
        e.printStackTrace();
        return "限流，或者被降级了";
    }
}
