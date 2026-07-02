package com.projeto.navalstrikeAPI.domain.coordinate.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
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
