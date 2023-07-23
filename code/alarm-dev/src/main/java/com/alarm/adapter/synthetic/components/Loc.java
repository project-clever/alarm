package com.alarm.adapter.synthetic.components;


/**
 * Abstract class to represent a generic Location.
 *
 * @param <T> Type of stored Location, must be Comparable.
 */
public abstract class Loc<T extends Comparable<T>> {

    private final T location;
    public Loc(T location){
        this.location = location;
    }


    /**
     * Getter to retrieve the value of the Location.
     *
     * @return The value of the Location.
     */
    public T getValue(){
        return location;
    }

    public String eval(Loc<?> l) throws IllegalArgumentException {
        if (l instanceof L) {
            L s = (L) l;
            return "Loc" + s.getValue();
        } else {
            throw new IllegalArgumentException("Invalid Loc!");
        }
    }

    /**
     * Abstract method to calculate the distance between this Loc and another Loc given as a parameter.
     *
     * @param otherLocation The Loc we need to check the distance from.
     * @return An integer representing the distance between the two Loc objects.
     */
    public abstract int distance(Loc<T> otherLocation);
}