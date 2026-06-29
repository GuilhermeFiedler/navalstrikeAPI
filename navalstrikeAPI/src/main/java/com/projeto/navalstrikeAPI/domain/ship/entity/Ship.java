package com.projeto.navalstrikeAPI.domain.ship.entity;

import com.projeto.navalstrikeAPI.common.enums.ShipType;
import com.projeto.navalstrikeAPI.domain.coordinate.entity.Coordinate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Entity
@Table(name ="ships")
@Getter
@Setter
@NoArgsConstructor
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    ShipType shipType;

    @ElementCollection
    @CollectionTable(name = "ship_coordinates", joinColumns = @JoinColumn(name = "ship_id"))
    private Set<Coordinate> coordinates = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "ship_hits", joinColumns =  @JoinColumn(name = "ship_id"))
    private Set<Coordinate> hits = new HashSet<>();

    public Ship(ShipType shipType, Set<Coordinate> coordinates){
        if (coordinates.size() != shipType.getSize()) {
            throw new IllegalArgumentException("Coordinates size doesn't match ship type");
        }
        this.shipType = shipType;
        this.coordinates = new HashSet<Coordinate>(coordinates);
        this.hits = new HashSet<Coordinate>();
    }
    public boolean hit(Coordinate coord){
        if (coordinates.contains(coord)){
            hits.add(coord);
            return true;
        }
        return false;
    }
    public boolean isSunk(){
        return hits.equals(coordinates);
    }
}
