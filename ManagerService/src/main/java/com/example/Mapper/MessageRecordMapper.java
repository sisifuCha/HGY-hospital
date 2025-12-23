package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.MessageRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

/**
 * 消息记录Mapper接口
 */
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {

    /**
     * 插入消息记录
     */
    @Insert("INSERT INTO \"message_record\" " +
            "(\"title\", \"content\", \"sender_type\", \"receiver_type\", \"receiver_id\", \"status\", \"read_status\") " +
            "VALUES (#{title}, #{content}, #{senderType}, #{receiverType}, #{receiverId}, " +
            "COALESCE(#{status}, 'unsent'), COALESCE(#{readStatus}, 'unconfirmed'))")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertMessage(MessageRecord record);
}