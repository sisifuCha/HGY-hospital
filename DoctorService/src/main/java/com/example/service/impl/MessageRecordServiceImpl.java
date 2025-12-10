package com.example.service.impl;

import com.example.entity.MessageRecord;
import com.example.mapper.MessageRecordMapper;
import com.example.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息记录服务实现类
 */
@Service
public class MessageRecordServiceImpl implements MessageRecordService {

    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Override
    @Transactional
    public MessageRecord createAddNumberApplicationMessage(String patientName, String docId) {
        MessageRecord record = new MessageRecord();
        record.setTitle("患者加号申请：" + patientName);
        record.setContent("患者" + patientName + "申请加号，详细信息见加号审核界面");
        record.setSenderType("server");
        record.setReceiverType("specific_doctor");
        record.setReceiverId(docId);
        record.setStatus("unsent");
        record.setReadStatus("unconfirmed");
        
        messageRecordMapper.insertMessage(record);
        return record;
    }

    @Override
    public List<MessageRecord> getUnsentMessagesForDoctor(String docId) {
        return messageRecordMapper.selectUnsentMessagesForDoctor(docId);
    }

    @Override
    public List<MessageRecord> getActiveMessagesForDoctor(String docId) {
        return messageRecordMapper.selectActiveMessagesForDoctor(docId);
    }

    @Override
    @Transactional
    public boolean markAsSent(Integer messageId) {
        return messageRecordMapper.updateStatusToSent(messageId) > 0;
    }

    @Override
    @Transactional
    public int batchMarkAsSent(List<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }
        return messageRecordMapper.batchUpdateStatusToSent(messageIds);
    }

    @Override
    @Transactional
    public boolean markAsConfirmed(Integer messageId) {
        return messageRecordMapper.updateReadStatusToConfirmed(messageId) > 0;
    }
}
