package com.easycoder.intellij.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.util.Alarm;

public class NotificationHelper {
    private static final String NOTIFICATION_GROUP = "EasyCoder";
    private static final Alarm ALARM = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

    public static void info(String message) { // 移除 title 参数
        notify(message, NotificationType.INFORMATION);
    }

    public static void warn(String message) { // 移除 title 参数
        notify(message, NotificationType.WARNING);
    }

    public static void error(String message) { // 移除 title 参数
        notify(message, NotificationType.ERROR);
    }

    private static void notify(String message, NotificationType type) {
        Notification notification = new Notification(
            NOTIFICATION_GROUP,
            NOTIFICATION_GROUP, // 使用固定的通知组作为标题
            message,
            type
        );
        Notifications.Bus.notify(notification);

        // 安排在3秒后自动关闭通知
        ALARM.addRequest(notification::expire, 3000);
    }
}
