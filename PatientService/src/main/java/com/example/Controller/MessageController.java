package com.example.Controller;

import com.example.conmon.result.Result;
import com.example.pojo.dto.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class MessageController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "patient:message:";

    @PostMapping("/{patientId}/messages")
    public Result<?> getUnsentMessages(@PathVariable String patientId) {
        String key = REDIS_KEY_PREFIX + patientId;
        List<Object> allMessages = redisTemplate.opsForList().range(key, 0, -1);
        
        List<Map<String, String>> unsentList = new ArrayList<>();
        boolean flag = false;

        if (allMessages != null && !allMessages.isEmpty()) {
            for (int i = 0; i < allMessages.size(); i++) {
                MessageDto msg = (MessageDto) allMessages.get(i);
                // 检查状态是否为 unsent
                if ("unsent".equals(msg.getStatus())) {
                    flag = true;
                    // 添加到返回列表
                    Map<String, String> item = new HashMap<>();
                    item.put("title", msg.getTitle());
                    item.put("content", msg.getContent());
                    unsentList.add(item);
                    
                    // 更新 Redis 中的状态为 sent
                    msg.setStatus("sent");
                    redisTemplate.opsForList().set(key, i, msg);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("flag", flag);
        response.put("list", unsentList);
        
        return Result.success(response);
    }
}
