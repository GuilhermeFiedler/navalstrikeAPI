package com.projeto.navalstrikeAPI.domain.coordinate.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
@Embeddable
public class Coordinate {
    private int x;
    private int y;

    @Override
    public boolean equals(Object obj){
        if(this==obj) return true;
        if(!(obj instanceof Coordinate)) return false;
        Coordinate other = (Coordinate) obj;
        return x == other.x && y == other.y;
    }
    @Override
    public int hashCode(){
        return Objects.hash(x,y);
    }

}
