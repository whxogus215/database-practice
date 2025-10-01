package org.example.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transaction.entity.DevilFlesh;
import org.example.transaction.repository.DevilFleshRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionReportService {

    private final Long FIND_ID = 1L;
    private final DevilFleshRepository devilFleshRepository;

    @Transactional
    public void settleMission(final long plus, final long minus) {
        final DevilFlesh findDevilFlesh = devilFleshRepository.findById(FIND_ID).orElseThrow(IllegalArgumentException::new);

        log.info("덴지 살점 정산 시작");
        findDevilFlesh.updateTotalPieces(plus);
        devilFleshRepository.saveAndFlush(findDevilFlesh);
        log.info("덴지 살점 정산 완료");

        findDevilFlesh.updateTotalPieces(minus);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public Long findTotalPieces() {
        return devilFleshRepository.findById(FIND_ID).orElseThrow(IllegalArgumentException::new)
                                   .getTotalPieces();
    }
}
