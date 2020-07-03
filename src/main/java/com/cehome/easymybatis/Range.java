package com.cehome.easymybatis;

public class Range {
    private Object min;
    private Object max;
    private boolean includeMin;
    private boolean includeMax;
    private boolean inRange;

    public Range(){

    }

    public static Range inRange(Object min, Object max, boolean includeMin, boolean isIncludeMax){
        return new Range(min,max,includeMin,isIncludeMax,true);
    }

    public static Range notInRange(Object min, Object max, boolean includeMin, boolean isIncludeMax){
        return new Range(min,max,includeMin,isIncludeMax,false);
    }

    protected Range(Object min, Object max, boolean includeMin, boolean isIncludeMax,boolean inRange) {
        this.min = min;
        this.max = max;
        this.includeMin = includeMin;
        this.includeMax = isIncludeMax;
        this.inRange=inRange;
    }

    public Object getMin() {
        return min;
    }

    public void setMin(Object min) {
        this.min = min;
    }

    public Object getMax() {
        return max;
    }

    public void setMax(Object max) {
        this.max = max;
    }

    public boolean isIncludeMin() {
        return includeMin;
    }

    public void setIncludeMin(boolean includeMin) {
        this.includeMin = includeMin;
    }

    public boolean isIncludeMax() {
        return includeMax;
    }

    public void setIncludeMax(boolean includeMax) {
        includeMax = includeMax;
    }

    public boolean isInRange() {
        return inRange;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }
}
