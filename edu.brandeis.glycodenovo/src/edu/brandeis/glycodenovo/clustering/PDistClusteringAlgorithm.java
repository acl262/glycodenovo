//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.brandeis.glycodenovo.clustering;

import java.util.ArrayList;
import java.util.List;

public class PDistClusteringAlgorithm implements ClusteringAlgorithm {
    public PDistClusteringAlgorithm() {
    }

    public Cluster performClustering(double[][] distances, String[] clusterNames, LinkageStrategy linkageStrategy) {
        if (distances != null && distances.length != 0) {
            if (distances[0].length != clusterNames.length * (clusterNames.length - 1) / 2) {
                throw new IllegalArgumentException("Invalid cluster name array");
            } else if (linkageStrategy == null) {
                throw new IllegalArgumentException("Undefined linkage strategy");
            } else {
                List<Cluster> clusters = this.createClusters(clusterNames);
                DistanceMap linkages = this.createLinkages(distances, clusters);
                HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);

                while(!builder.isTreeComplete()) {
                    builder.agglomerate(linkageStrategy);
                }

                return builder.getRootCluster();
            }
        } else {
            throw new IllegalArgumentException("Invalid distance matrix");
        }
    }

    public List<Cluster> performFlatClustering(double[][] distances, String[] clusterNames, LinkageStrategy linkageStrategy, Double threshold) {
        if (distances != null && distances.length != 0) {
            if (distances[0].length != clusterNames.length * (clusterNames.length - 1) / 2) {
                throw new IllegalArgumentException("Invalid cluster name array");
            } else if (linkageStrategy == null) {
                throw new IllegalArgumentException("Undefined linkage strategy");
            } else {
                List<Cluster> clusters = this.createClusters(clusterNames);
                DistanceMap linkages = this.createLinkages(distances, clusters);
                HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
                return builder.flatAgg(linkageStrategy, threshold);
            }
        } else {
            throw new IllegalArgumentException("Invalid distance matrix");
        }
    }

    public Cluster performWeightedClustering(double[][] distances, String[] clusterNames, double[] weights, LinkageStrategy linkageStrategy) {
        return this.performClustering(distances, clusterNames, linkageStrategy);
    }

    private DistanceMap createLinkages(double[][] distances, List<Cluster> clusters) {
        DistanceMap linkages = new DistanceMap();

        for(int col = 0; col < clusters.size(); ++col) {
            Cluster cluster_col = (Cluster)clusters.get(col);

            for(int row = col + 1; row < clusters.size(); ++row) {
                ClusterPair link = new ClusterPair();
                Double d = distances[0][accessFunction(row, col, clusters.size())];
                link.setLinkageDistance(d);
                link.setlCluster(cluster_col);
                link.setrCluster((Cluster)clusters.get(row));
                linkages.add(link);
            }
        }

        return linkages;
    }

    private List<Cluster> createClusters(String[] clusterNames) {
        List<Cluster> clusters = new ArrayList();
        String[] var3 = clusterNames;
        int var4 = clusterNames.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String clusterName = var3[var5];
            Cluster cluster = new Cluster(clusterName);
            cluster.addLeafName(clusterName);
            clusters.add(cluster);
        }

        return clusters;
    }

    private static int accessFunction(int i, int j, int n) {
        return n * j - j * (j + 1) / 2 + i - 1 - j;
    }
}
