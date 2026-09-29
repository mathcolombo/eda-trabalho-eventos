package com.eda_project.notifications.repositories;

import com.eda_project.notifications.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {
}
