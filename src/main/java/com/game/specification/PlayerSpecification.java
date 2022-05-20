package com.game.specification;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class PlayerSpecification {
    // r - root, cq - criteriaQuery, cb - criteriaBuilder
    public static Specification<Player> nameLike(String name){
        return (r, cq, cb) -> cb.like(r.get("name"), "%" + name + "%");
    }

    public static Specification<Player> titleLike(String title){
        return (r, cq, cb) -> cb.like(r.get("title"), "%" + title + "%");
    }

    public static Specification<Player> raceIs(Race race) {
        return (r, cq, cb) -> cb.equal(r.get("race"), race);
    }

    public static Specification<Player> professionIs(Profession profession) {
        return (r, cq, cb) -> cb.equal(r.get("profession"), profession);
    }

    public static Specification<Player> bornBetween(Long after, Long before) {
        return (r, cq, cb) -> cb.between(r.get("birthday"), new Date(after), new Date(before));
    }

    public static Specification<Player> isBanned(Boolean banned) {
        return (r, cq, cb) -> cb.equal(r.get("banned"), banned);
    }

    public static Specification<Player> experienceBetween(Integer after, Integer before) {
        return (r, cq, cb) -> cb.between(r.get("experience"), after, before);
    }

    public static Specification<Player> levelBetween(Integer after, Integer before) {
        return (r, cq, cb) -> cb.between(r.get("level"), after, before);
    }


}
