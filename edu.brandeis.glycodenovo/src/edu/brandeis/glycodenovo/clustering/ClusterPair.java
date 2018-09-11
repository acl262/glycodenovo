package edu.brandeis.glycodenovo.clustering;

public class ClusterPair implements Comparable<ClusterPair> {
    private static long globalIndex = 0L;
    private Cluster lCluster;
    private Cluster rCluster;
    private Double linkageDistance;

    public ClusterPair() {
    }

    public ClusterPair(Cluster left, Cluster right, Double distance) {
        this.lCluster = left;
        this.rCluster = right;
        this.linkageDistance = distance;
    }

    public Cluster getOtherCluster(Cluster c) {
        return this.lCluster == c ? this.rCluster : this.lCluster;
    }

    public Cluster getlCluster() {
        return this.lCluster;
    }

    public void setlCluster(Cluster lCluster) {
        this.lCluster = lCluster;
    }

    public Cluster getrCluster() {
        return this.rCluster;
    }

    public void setrCluster(Cluster rCluster) {
        this.rCluster = rCluster;
    }

    public Double getLinkageDistance() {
        return this.linkageDistance;
    }

    public void setLinkageDistance(Double distance) {
        this.linkageDistance = distance;
    }

    public ClusterPair reverse() {
        return new ClusterPair(this.getrCluster(), this.getlCluster(), this.getLinkageDistance());
    }

    public int compareTo(ClusterPair o) {
        int result;
        if (o != null && o.getLinkageDistance() != null) {
            if (this.getLinkageDistance() == null) {
                result = 1;
            } else {
                result = this.getLinkageDistance().compareTo(o.getLinkageDistance());
            }
        } else {
            result = -1;
        }

        return result;
    }

    public Cluster agglomerate(String name) {
        if (name == null) {
            name = "clstr#" + ++globalIndex;
        }

        Cluster cluster = new Cluster(name);
        cluster.setDistance(new Distance(this.getLinkageDistance()));
        cluster.appendLeafNames(this.lCluster.getLeafNames());
        cluster.appendLeafNames(this.rCluster.getLeafNames());
        cluster.addChild(this.lCluster);
        cluster.addChild(this.rCluster);
        this.lCluster.setParent(cluster);
        this.rCluster.setParent(cluster);
        Double lWeight = this.lCluster.getWeightValue();
        Double rWeight = this.rCluster.getWeightValue();
        double weight = lWeight + rWeight;
        cluster.getDistance().setWeight(weight);
        return cluster;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.lCluster != null) {
            sb.append(this.lCluster.getName());
        }

        if (this.rCluster != null) {
            if (sb.length() > 0) {
                sb.append(" + ");
            }

            sb.append(this.rCluster.getName());
        }

        sb.append(" : ").append(this.linkageDistance);
        return sb.toString();
    }
}
