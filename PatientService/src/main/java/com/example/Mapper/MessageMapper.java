package com.example.Mapper;

import com.example.pojo.dto.MessageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Select("SELECT id, title, content, status, receiver_id as receiverId, to_char(created_time, 'YYYY-MM-DD HH24:MI:SS') as createdTime " +
            "FROM message_record " +
            "WHERE receiver_type = 'specific_patient' AND status = 'unsent'")
    List<MessageDto> findUnsentMessages();

    @Select("SELECT id, title, content, status, receiver_id as receiverId, to_char(created_time, 'YYYY-MM-DD HH24:MI:SS') as createdTime " +
            "FROM message_record WHERE id = #{id}")
    MessageDto findMessageById(@Param("id") Long id);

    @Update("UPDATE message_record SET status = #{status} WHERE id = #{id}")
    int updateMessageStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE message_record SET status = 'pushed_to_redis' WHERE id = #{id}")
    int markAsPushed(@Param("id") Long id);
}
