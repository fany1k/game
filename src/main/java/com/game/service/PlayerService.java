package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface PlayerService {

    List<Player> getPlayersList(Specification<Player> spec, Pageable pageable);

    Integer getPlayersCount(Specification<Player> spec);

    Player createPlayer(Player player);

    Player getPlayer(Long id) throws NoSuchElementException;

    Player updatePlayer(Player player);

    void deletePlayer(Long id);



}
