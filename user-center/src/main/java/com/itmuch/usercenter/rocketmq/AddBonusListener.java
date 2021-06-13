package com.itmuch.usercenter.rocketmq;

import com.itmuch.usercenter.dao.bonus.BonusEventLogMapper;
import com.itmuch.usercenter.dao.user.UserMapper;
import com.itmuch.usercenter.domain.entity.bonus.BonusEventLog;
import com.itmuch.usercenter.domain.entity.user.User;
import com.itmuch.usercenter.domain.entity.user.dto.messaging.UserAddBonusMsgDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
@RocketMQMessageListener(consumerGroup = "consumer-group", topic = "add-bonus")
public class AddBonusListener implements RocketMQListener<UserAddBonusMsgDTO> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private BonusEventLogMapper bonusEventLogMapper;


    @Override
    public void onMessage(UserAddBonusMsgDTO message) {
        // 当收到消息的时候，执行的业务
        // 1.为用户加积分
        Integer userId = message.getUserId();
        Integer bonus = message.getBouns();
        User user = this.userMapper.selectByPrimaryKey(userId);

        user.setBonus(user.getBonus() + message.getBouns());
        this.userMapper.updateByPrimaryKeySelective(user);

        // 2.记录日志到bonus_event_log表里面
        this.bonusEventLogMapper.insert(BonusEventLog.builder()
                .userId(userId)
                .value(bonus)
                .event("CONTRIBUTE")
                .createTime(new Date())
                .description("投稿加积分")
                .build());
        log.info("积分添加完毕...");
    }
}
