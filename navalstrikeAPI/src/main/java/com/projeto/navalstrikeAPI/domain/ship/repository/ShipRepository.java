package com.projeto.navalstrikeAPI.domain.ship.repository;

import com.projeto.navalstrikeAPI.domain.ship.entity.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShipRepository extends JpaRepository<Ship, UUID> {
}
