package com.parking.keyboard.neighbor;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;


public class Province {

    public final String name;
    public final String shortName;

    private final Set<Province> neighbors = new HashSet<>(10);

    public Province(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public Province link(Province province) {
        if (!this.neighbors.contains(province)) {
            neighbors.add(province);
            province.link(this);
        }
        return this;
    }

    public Set<Province> getNeighbors() {
        return new HashSet<>(this.neighbors);
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(shortName);
    }
}
