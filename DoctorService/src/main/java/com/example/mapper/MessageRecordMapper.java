package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.MessageRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息记录Mapper接口
 */
@Mapper
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

    /**
     * 查询特定医生的未发送消息
     * receiver_type 为 specific_doctor 且 receiver_id 为医生ID
     * 或 receiver_type 为 all_doctors
     * 状态为 unsent
     */
    @Select("SELECT * FROM \"message_record\" " +
            "WHERE \"status\" = 'unsent' " +
            "AND ((\"receiver_type\" = 'specific_doctor' AND \"receiver_id\" = #{docId}) " +
            "     OR \"receiver_type\" = 'all_doctors') " +
            "ORDER BY \"created_time\" DESC")
    List<MessageRecord> selectUnsentMessagesForDoctor(@Param("docId") String docId);

    /**
     * 查询特定医生的所有有效消息（未过期的）
     */
    @Select("SELECT * FROM \"message_record\" " +
            "WHERE ((\"receiver_type\" = 'specific_doctor' AND \"receiver_id\" = #{docId}) " +
            "       OR \"receiver_type\" = 'all_doctors') " +
            "AND \"status\" != 'expired' " +
            "ORDER BY \"created_time\" DESC")
    List<MessageRecord> selectActiveMessagesForDoctor(@Param("docId") String docId);

    /**
     * 更新消息状态为已发送，并更新updated_time
     */
    @Update("UPDATE \"message_record\" " +
            "SET \"status\" = 'sent', \"updated_time\" = CURRENT_TIMESTAMP " +
            "WHERE \"id\" = #{id}")
    int updateStatusToSent(@Param("id") Integer id);

    /**
     * 批量更新消息状态为已发送
     */
    @Update("<script>" +
            "UPDATE \"message_record\" " +
            "SET \"status\" = 'sent', \"updated_time\" = CURRENT_TIMESTAMP " +
            "WHERE \"id\" IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatusToSent(@Param("ids") List<Integer> ids);

    /**
     * 更新消息阅读状态为已确认
     */
    @Update("UPDATE \"message_record\" " +
            "SET \"read_status\" = 'confirmed', \"updated_time\" = CURRENT_TIMESTAMP " +
            "WHERE \"id\" = #{id}")
    int updateReadStatusToConfirmed(@Param("id") Integer id);
}
