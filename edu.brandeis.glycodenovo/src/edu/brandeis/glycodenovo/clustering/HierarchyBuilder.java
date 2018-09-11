package edu.brandeis.glycodenovo.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class HierarchyBuilder {
    private DistanceMap distances;
    private List<Cluster> clusters;

    public DistanceMap getDistances() {
        return this.distances;
    }

    public List<Cluster> getClusters() {
        return this.clusters;
    }

    public HierarchyBuilder(List<Cluster> clusters, DistanceMap distances) {
        this.clusters = clusters;
        this.distances = distances;
    }

    public List<Cluster> flatAgg(LinkageStrategy linkageStrategy, Double threshold) {
        while(!this.isTreeComplete() && this.distances.minDist() != null && this.distances.minDist() <= threshold) {
            this.agglomerate(linkageStrategy);
        }

        return this.clusters;
    }

    public void agglomerate(LinkageStrategy linkageStrategy) {
        ClusterPair minDistLink = this.distances.removeFirst();
        if (minDistLink != null) {
            this.clusters.remove(minDistLink.getrCluster());
            this.clusters.remove(minDistLink.getlCluster());
            Cluster oldClusterL = minDistLink.getlCluster();
            Cluster oldClusterR = minDistLink.getrCluster();
            Cluster newCluster = minDistLink.agglomerate((String)null);
            Iterator var6 = this.clusters.iterator();

            while(var6.hasNext()) {
                Cluster iClust = (Cluster)var6.next();
                ClusterPair link1 = this.findByClusters(iClust, oldClusterL);
                ClusterPair link2 = this.findByClusters(iClust, oldClusterR);
                ClusterPair newLinkage = new ClusterPair();
                newLinkage.setlCluster(iClust);
                newLinkage.setrCluster(newCluster);
                Collection<Distance> distanceValues = new ArrayList();
                Double distVal;
                Double weightVal;
                if (link1 != null) {
                    distVal = link1.getLinkageDistance();
                    weightVal = link1.getOtherCluster(iClust).getWeightValue();
                    distanceValues.add(new Distance(distVal, weightVal));
                    this.distances.remove(link1);
                }

                if (link2 != null) {
                    distVal = link2.getLinkageDistance();
                    weightVal = link2.getOtherCluster(iClust).getWeightValue();
                    distanceValues.add(new Distance(distVal, weightVal));
                    this.distances.remove(link2);
                }

                Distance newDistance = linkageStrategy.calculateDistance(distanceValues);
                newLinkage.setLinkageDistance(newDistance.getDistance());
                this.distances.add(newLinkage);
            }

            this.clusters.add(newCluster);
        }

    }

    private ClusterPair findByClusters(Cluster c1, Cluster c2) {
        return this.distances.findByCodePair(c1, c2);
    }

    public boolean isTreeComplete() {
        return this.clusters.size() == 1;
    }

    public Cluster getRootCluster() {
        if (!this.isTreeComplete()) {
            throw new RuntimeException("No root available");
        } else {
            return (Cluster)this.clusters.get(0);
        }
    }
}
