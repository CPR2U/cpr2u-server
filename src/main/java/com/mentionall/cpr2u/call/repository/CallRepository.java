package com.mentionall.cpr2u.call.repository;

import com.mentionall.cpr2u.call.domain.CPRCall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRepository extends JpaRepository<CPRCall, Long> {
}
