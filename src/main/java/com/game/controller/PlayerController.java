package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import com.game.specification.PlayerSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {
    private PlayerService playerService;
    Date minBirthday;
    Date maxBirthday;

    public PlayerController(PlayerService playerService) {
        Calendar min = Calendar.getInstance();
        min.set(2000, Calendar.JANUARY, 1);
        minBirthday = min.getTime();

        Calendar max = Calendar.getInstance();
        max.set(3000, Calendar.DECEMBER, 31, 23, 59, 59);
        maxBirthday = max.getTime();
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers(@RequestParam Map<String, String> params) {
        Specification<Player> specification = createSpecification(params);
        PageRequest pageRequest = createPageRequest(params);

        List<Player> players = playerService.getPlayersList(specification, pageRequest);

        return new ResponseEntity<>(players, HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getPlayersCount(@RequestParam Map<String, String> params) {
        Specification<Player> specification = createSpecification(params);

        Integer playersCount = playerService.getPlayersCount(specification);

        return new ResponseEntity<>(playersCount, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!isPlayerHasAllValidFields(player)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        if (player.getBanned() == null) {
            player.setBanned(false);
        }

        player.setLevel(countLevel(player.getExperience()));
        player.setUntilNextLevel(countUntilNextLevel(player.getExperience()));

        player = playerService.createPlayer(player);

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable Long id) {
        if (isIdBad(id)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        Player player;
        try {
            player = playerService.getPlayer(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player update) {
        if (isIdBad(id)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        Player player;
        try {
            player = playerService.getPlayer(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        if (!isPlayerFieldsAreValid(update)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        if (update.getName() != null) {
            player.setName(update.getName());
        }

        if (update.getTitle() != null) {
            player.setTitle(update.getTitle());
        }

        if (update.getRace() != null) {
            player.setRace(update.getRace());
        }

        if (update.getProfession() != null) {
            player.setProfession(update.getProfession());
        }

        if (update.getBirthday() != null) {
            player.setBirthday(update.getBirthday());
        }

        if (update.getBanned() != null) {
            player.setBanned(update.getBanned());
        }

        if (update.getExperience() != null) {
            int exp = update.getExperience();
            player.setExperience(exp);
            player.setLevel(countLevel(exp));
            player.setUntilNextLevel(countUntilNextLevel(exp));
        }


        return new ResponseEntity<>(playerService.updatePlayer(player), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable Long id) {
        if (isIdBad(id)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        try {
            playerService.getPlayer(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        playerService.deletePlayer(id);

        return new ResponseEntity<>(null, HttpStatus.OK);
    }


    private boolean isPlayerHasAllValidFields(Player player) {
        if (player.getName() != null && player.getTitle() != null
                && player.getRace() != null && player.getProfession() != null
                && player.getBirthday() != null && player.getExperience() != null) {
            return isPlayerFieldsAreValid(player);
        }
        return false;
    }

    private boolean isPlayerFieldsAreValid(Player player) {
        if (player.getName() != null
                && (player.getName().equals("") || player.getName().length() > 12)) {
            return false;
        }

        if (player.getTitle() != null
                && player.getTitle().length() > 30) {
            return false;
        }

        if (player.getExperience() != null
                && (player.getExperience() < 0 || player.getExperience() > 10_000_000)) {
            return false;
        }

        return player.getBirthday() == null
                || (player.getBirthday().getTime() >= 0
                && !player.getBirthday().before(minBirthday) && !player.getBirthday().after(maxBirthday));
    }

    private boolean isIdBad(Long id){
        return (id == null || id <= 0);
    }

    private int countLevel(int exp) {
        return (int) (Math.sqrt(2500 + 200 * exp) - 50) / 100;
    }

    private int countUntilNextLevel(int exp) {
        int lvl = countLevel(exp);
        return 50 * (lvl + 1) * (lvl + 2) - exp;
    }

    private Specification<Player> createSpecification(Map<String, String> params) {
        Specification<Player> specification = Specification.where(null);

        String name = params.get("name");
        String title = params.get("title");
        Race race = params.get("race") == null ? null : Race.valueOf(params.get("race"));
        Profession profession = params.get("profession") == null ? null : Profession.valueOf(params.get("profession"));
        long after = params.get("after") == null ? 0L : Long.parseLong(params.get("after"));
        long before = params.get("before") == null ? maxBirthday.getTime() + 1 : Long.parseLong(params.get("before"));
        Boolean banned = params.get("banned") == null ? null : Boolean.valueOf(params.get("banned"));
        int minExperience = params.get("minExperience") == null ? 0 : Integer.parseInt(params.get("minExperience"));
        int maxExperience = params.get("maxExperience") == null ? Integer.MAX_VALUE : Integer.parseInt(params.get("maxExperience"));
        int minLevel = params.get("minLevel") == null ? 0 : Integer.parseInt(params.get("minLevel"));
        int maxLevel = params.get("maxLevel") == null ? Integer.MAX_VALUE : Integer.parseInt(params.get("maxLevel"));

        if (name != null) {
            specification = specification.and(PlayerSpecification.nameLike(name));
        }
        if (title != null) {
            specification = specification.and(PlayerSpecification.titleLike(title));
        }
        if (race != null) {
            specification = specification.and(PlayerSpecification.raceIs(race));
        }
        if (profession != null) {
            specification = specification.and(PlayerSpecification.professionIs(profession));
        }
        if (banned != null) {
            specification = specification.and(PlayerSpecification.isBanned(banned));
        }
        if (after != 0L || before != Long.MAX_VALUE) {
            specification = specification.and(PlayerSpecification.bornBetween(after, before));
        }
        if (minExperience != 0 || maxExperience != Integer.MAX_VALUE) {
            specification = specification.and(PlayerSpecification.experienceBetween(minExperience, maxExperience));
        }
        if (minLevel != 0 || maxLevel != Integer.MAX_VALUE) {
            specification = specification.and(PlayerSpecification.levelBetween(minLevel, maxLevel));
        }

        return specification;
    }

    private PageRequest createPageRequest(Map<String, String> params) {
        PlayerOrder order = params.get("order") == null ? PlayerOrder.ID : PlayerOrder.valueOf(params.get("order"));
        int pageNumber = params.get("pageNumber") == null ? 0 : Integer.parseInt(params.get("pageNumber"));
        int pageSize = params.get("pageSize") == null ? 3 : Integer.parseInt(params.get("pageSize"));

        return PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
    }



}
