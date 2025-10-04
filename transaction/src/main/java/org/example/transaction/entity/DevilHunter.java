package org.example.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DevilHunter {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "hunter_rank")
    private DevilHunterRank rank;

    public void update(final DevilHunterRank rank) {
        this.rank = rank;
    }

    public DevilHunter(final String name, final DevilHunterRank rank) {
        this.name = name;
        this.rank = rank;
    }
}
