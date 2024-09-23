package com.parking.keyboard.engine;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;


final public class RowEntry extends ArrayList<KeyEntry> {

    public RowEntry(int initialCapacity) {
        super(initialCapacity);
    }

    public RowEntry() {
    }

    public RowEntry(@NonNull Collection<? extends KeyEntry> c) {
        super(c);
    }
}
