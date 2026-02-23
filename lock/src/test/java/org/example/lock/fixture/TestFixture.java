package org.example.lock.fixture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.example.lock.deadlock.entity.Event;
import org.example.lock.deadlock.entity.EventWithLock;
import org.example.lock.deadlock.entity.Member;
import org.example.lock.deadlock.entity.Role;

public class TestFixture {

    public static Event createEvent(String name, int maxParticipants) {
        return Event.builder()
                    .name(name != null ? name : "테스트 이벤트")
                    .description("테스트 이벤트입니다.")
                    .eventDate(LocalDateTime.now().plusDays(7))
                    .maxParticipants(maxParticipants > 0 ? maxParticipants : 100)
                    .build();
    }

    public static Member createTestMember(String nickname) {
        return Member.builder()
                     .email("test" + UUID.randomUUID() + "@test.com")
                     .password("password")
                     .nickname(nickname != null ? nickname : "테스트유저")
                     .role(Role.USER)
                     .build();
    }

    public static List<Member> createTestMembers(int count) {
        return IntStream.range(0, count)
                        .mapToObj(i -> createTestMember("테스트유저" + i))
                        .toList();
    }

    public static EventWithLock createEventWithLock(String name, int maxParticipants) {
        return EventWithLock.builder()
                            .name(name != null ? name : "테스트 이벤트")
                            .description("테스트 이벤트입니다.")
                            .eventDate(LocalDateTime.now().plusDays(7))
                            .maxParticipants(maxParticipants > 0 ? maxParticipants : 100)
                            .build();
    }
}
