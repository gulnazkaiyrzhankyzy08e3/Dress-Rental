package com.example.rental.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class RentalJpa {

    private final EntityManager em;

    public RentalJpa(EntityManager em) {
        this.em = em;
    }

    public void insert(RentalEntity rental) {
        em.persist(rental);
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
