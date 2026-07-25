package com.legal.platform.repository;

import com.legal.platform.model.AIChatHistory;
import com.legal.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AIChatHistoryRepository extends JpaRepository<AIChatHistory, Long> {
    List<AIChatHistory> findByUserOrderByCreatedAtDesc(User user);
}
