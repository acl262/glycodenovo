package edu.brandeis.glycodenovo.clustering;

public class Distance implements Comparable<Distance>, Cloneable {
    private Double distance;
    private Double weight;

    public Distance() {
        this(0.0D);
    }

    public Distance(Double distance) {
        this(distance, 1.0D);
    }

    public Distance(Double distance, Double weight) {
        this.distance = distance;
        this.weight = weight;
    }

    public Double getDistance() {
        return this.distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getWeight() {
        return this.weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public boolean isNaN() {
        return this.distance == null || this.distance.isNaN();
    }

    public int compareTo(Distance distance) {
        return distance == null ? 1 : this.getDistance().compareTo(distance.getDistance());
    }

    public String toString() {
        return String.format("distance : %.2f, weight : %.2f", this.distance, this.weight);
    }
}
