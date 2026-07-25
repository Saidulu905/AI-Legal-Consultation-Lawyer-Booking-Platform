package com.legal.platform.service;

import com.legal.platform.model.Notification;
import java.util.List;

public interface NotificationService {
    List<Notification> getNotificationsForUser(Long userId);
    List<Notification> getUnreadNotificationsForUser(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    Notification createNotification(Long userId, String message);
}
