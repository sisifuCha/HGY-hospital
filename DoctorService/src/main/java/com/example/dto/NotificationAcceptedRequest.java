package com.example.dto;

/**
 * 消息确认请求 DTO
 * 用于前端确认已收到通知消息
 */
public class NotificationAcceptedRequest {
    
    /**
     * 消息记录ID (格式: "msg-123")
     */
    private String id;
    
    /**
     * 医生ID
     */
    private String docId;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 从 "msg-123" 格式中提取数字ID
     */
    public Integer getMessageId() {
        if (id == null || id.isEmpty()) {
            return null;
        }
        // 去掉 "msg-" 前缀
        String numStr = id.startsWith("msg-") ? id.substring(4) : id;
        try {
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public String toString() {
        return "NotificationAcceptedRequest{" +
                "id='" + id + '\'' +
                ", docId='" + docId + '\'' +
                '}';
    }
}
