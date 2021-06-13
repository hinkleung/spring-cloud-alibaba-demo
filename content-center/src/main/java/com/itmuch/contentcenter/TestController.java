package com.itmuch.contentcenter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.feignclient.TestUserCenterFeignClient;
import com.itmuch.contentcenter.rocketmq.MySource;
import com.itmuch.contentcenter.sentineltest.TestControllerBlockHandlerClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class TestController {
    @Resource
    private ShareMapper shareMapper;
    @Resource
    private DiscoveryClient discoveryClient;

    @GetMapping("/test2")
    private List<ServiceInstance> getDiscoveryClient() {
        List<ServiceInstance> instances = this.discoveryClient.getInstances("user-center");
        return instances;
    }

    @GetMapping("/test")
    public List<Share> testInsert() {
        Share share = new Share();
        share.setCreateTime(new Date());
        share.setUpdateTime(new Date());
        share.setTitle("xxx");
        share.setCover("xxx");
        share.setAuthor("cql");
        share.setBuyCount(1);
        this.shareMapper.insertSelective(share);
        List<Share> shares = shareMapper.selectAll();
        return shares;
    }

    @Autowired
    private TestUserCenterFeignClient testUserCenterFeignClient;

    @GetMapping("test-get")
    public UserDTO query(UserDTO userDTO) {
        return testUserCenterFeignClient.query(userDTO);
    }

    @SentinelResource("hot" +
            "")
    @GetMapping("test-hot")
    public String testHot(@RequestParam(required = false) String a
            , @RequestParam(required = false) String b) {
        return a + " " + b;
    }

    @GetMapping("test-add-flow-rule")
    public String testAdd() {
        this.initFlowQpsRule();
        return "success";
    }

    private void initFlowQpsRule() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule("/share/1");
        rule.setCount(20);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    @GetMapping("/test-sentinel-api")
    public String testSentinelAPI(@RequestParam(required = false) String a) {
        Entry entry = null;
        try {
            // 定义一个sentinel保护的资源，名称是test-sentinel-api
            entry = SphU.entry("test-sentinel-api");
            if (StringUtils.isBlank(a)) {
                throw new IllegalArgumentException("a不能为空");
            }
            // 被保护的业务逻辑
            return a;
        }
        // 如果被保护的资源被限流或者降级了，就会抛BlockException
        catch (BlockException e) {
            log.warn("限流，或者降级了", e);
            e.printStackTrace();
            return "限流，或者被降级了";
        } catch (IllegalArgumentException e2) {
            Tracer.trace(e2);
            return "参数非法";
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @GetMapping("/test-sentinel-resource")
    @SentinelResource(value = "test-sentinel-api"
            , fallback = "fallback"
            , blockHandlerClass = TestControllerBlockHandlerClass.class)
    public String testSentinelResource(@RequestParam(required = false) String a) {
        Entry entry = null;
        if (StringUtils.isBlank(a)) {
            throw new IllegalArgumentException("a不能为空");
        }
        // 被保护的业务逻辑
        return a;

    }

    public String fallback(String a) {
        return "限流，或者降级了 fallback";
    }

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/test-rest-template-sentinel/{userId}")
    public UserDTO test(@PathVariable Integer userId) {
        return this.restTemplate.getForObject("http://user-center/users/{userId}"
                , UserDTO.class, userId);

    }

    @Autowired
    private Source source;

    @GetMapping("/test-stream")
    public String testStream() {
        source.output()
                .send(MessageBuilder.withPayload("消息体").build());
        return "success";
    }

    @Autowired
    private MySource mySource;

    @GetMapping("/test-stream-2")
    public String testStream2() {
        mySource.output()
                .send(MessageBuilder.withPayload("消息体").build());
        return "success";
    }

    @GetMapping("/tokenRelay/{userId}")
    public ResponseEntity<UserDTO> tokenRelay(@PathVariable Integer userId) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("X-Token");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Token", token);
        return this.restTemplate.exchange("http://user-center/users/{userId}"
                , HttpMethod.GET
                , new HttpEntity<>(headers)
                , UserDTO.class,
                userId);
    }


}
