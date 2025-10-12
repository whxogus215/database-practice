package org.example.transaction.repository;

import java.util.List;
import org.example.transaction.entity.Devil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevilRepository extends JpaRepository<Devil, Long> {

    List<Devil> findDevilsByArea(String area);
}
