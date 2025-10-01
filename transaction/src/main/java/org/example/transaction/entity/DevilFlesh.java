package org.example.transaction.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class DevilFlesh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    private Long totalPieces;

    public void updateTotalPieces(final long count) {
        totalPieces += count;
    }

    public DevilFlesh(final Long totalPieces) {
        this.totalPieces = totalPieces;
    }

    public DevilFlesh(final Long id, final Long totalPieces) {
        this.id = id;
        this.totalPieces = totalPieces;
    }
}
