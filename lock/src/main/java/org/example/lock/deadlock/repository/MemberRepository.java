package org.example.lock.deadlock.repository;

import org.example.lock.deadlock.entity.Event;
import org.example.lock.deadlock.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

}
