
package edu.brandeis.glycodenovo.clustering;

import java.util.List;

public interface ClusteringAlgorithm {
    Cluster performClustering(double[][] var1, String[] var2, LinkageStrategy var3);

    Cluster performWeightedClustering(double[][] var1, String[] var2, double[] var3, LinkageStrategy var4);

    List<Cluster> performFlatClustering(double[][] var1, String[] var2, LinkageStrategy var3, Double var4);
}
