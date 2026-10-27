package com.fplassistant.controller;

import com.fplassistant.dto.PlayerDetailDto;
import com.fplassistant.dto.PlayerMetaDto;
import com.fplassistant.dto.PlayerPageResponse;
import com.fplassistant.service.PlayerDirectoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerDirectoryService playerDirectoryService;

    public PlayerController(PlayerDirectoryService playerDirectoryService) {
        this.playerDirectoryService = playerDirectoryService;
    }

    @GetMapping
    public ResponseEntity<PlayerPageResponse> getPlayers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> teams,
            @RequestParam(required = false) List<Short> positions,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) Double minOwnership,
            @RequestParam(required = false) Double maxOwnership,
            @RequestParam(required = false) Boolean inSquad,
            @RequestParam(required = false, defaultValue = "total_points") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size) {

        PlayerPageResponse response = playerDirectoryService.getPlayers(
                search, teams, positions, minPrice, maxPrice, status,
                minOwnership, maxOwnership, inSquad, sortBy, sortDir, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDetailDto> getPlayerDetail(@PathVariable Integer id) {
        PlayerDetailDto detail = playerDirectoryService.getPlayerDetail(id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/meta")
    public ResponseEntity<PlayerMetaDto> getMeta() {
        PlayerMetaDto meta = playerDirectoryService.getMeta();
        return ResponseEntity.ok(meta);
    }
}
