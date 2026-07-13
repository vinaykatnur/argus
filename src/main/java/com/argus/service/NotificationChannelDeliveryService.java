package com.argus.service;

import com.argus.entity.NotificationDelivery;
import com.argus.enums.NotificationChannel;

public interface NotificationChannelDeliveryService {

    NotificationChannel channel();

    void send(NotificationDelivery delivery, NotificationContent content);
}
