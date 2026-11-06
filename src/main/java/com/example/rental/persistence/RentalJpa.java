
package com.example.rental.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RentalJpa {

    private final EntityManager em;

    public RentalJpa(EntityManager em) {
        this.em = em;
    }

    public void insert(RentalEntity rental) {
        em.persist(rental);
    }

    public Optional<RentalEntity> findById(UUID id) {
        return Optional.ofNullable(
                em.find(RentalEntity.class, id));
    }

    public int count(String businessKey) {
        Long n = em.createQuery("""
                select count(r) from RentalEntity r
                where r.businessKey = :key
                """, Long.class)
                .setParameter("key", businessKey)
                .getSingleResult();
        return n.intValue();
    }

    public void flush() {
        em.flush();
    }
}
