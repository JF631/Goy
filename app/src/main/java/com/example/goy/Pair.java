package com.example.goy;

import androidx.annotation.NonNull;

public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second){
        this.first = first;
        this.second = second;
    }

    public A getFirst(){return first;}

    public B getSecond(){
        return second;
    }

    @NonNull
    public String toString(){
        return "(" + first.toString() + "," + second.toString() + ")";
    }
}