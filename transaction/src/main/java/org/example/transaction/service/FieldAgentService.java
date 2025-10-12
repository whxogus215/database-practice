package org.example.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transaction.entity.Devil;
import org.example.transaction.repository.DevilRepository;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FieldAgentService {

    private final DevilRepository devilRepository;

    // 특정 area에 새로운 악마를 추가(INSERT)하는 비즈니스 로직
    public void addDevilInArea(String area) {
        devilRepository.save(new Devil(area));
        log.info("[현장요원] 새로운 악마 추가");
    }
}
