package com.legal.platform.repository;

import com.legal.platform.model.Document;
import com.legal.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByClientOrderByCreatedAtDesc(User client);
}
