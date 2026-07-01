package com.projeto.navalstrikeAPI.domain.coordinate.entity;

import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@Embeddable
public class Coordinate {
    private int x;
    private int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj){
        if(this==obj) return true;
        if(!(obj instanceof Coordinate other)) return false;
        return x == other.x && y == other.y;
    }
    @Override
    public int hashCode(){
        return Objects.hash(x,y);
    }

}
