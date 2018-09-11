//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.brandeis.glycodenovo.clustering;

import java.util.*;

public class DistanceMap {
    private Map<String, DistanceMap.Item> pairHash = new HashMap();
    private PriorityQueue<DistanceMap.Item> data = new PriorityQueue();

    public DistanceMap() {
    }

    public List<ClusterPair> list() {
        List<ClusterPair> l = new ArrayList();
        Iterator var2 = this.data.iterator();

        while(var2.hasNext()) {
            DistanceMap.Item clusterPair = (DistanceMap.Item)var2.next();
            l.add(clusterPair.pair);
        }

        return l;
    }

    public ClusterPair findByCodePair(Cluster c1, Cluster c2) {
        String inCode = this.hashCodePair(c1, c2);
        return ((DistanceMap.Item)this.pairHash.get(inCode)).pair;
    }

    public ClusterPair removeFirst() {
        DistanceMap.Item poll;
        for(poll = (DistanceMap.Item)this.data.poll(); poll != null && poll.removed; poll = (DistanceMap.Item)this.data.poll()) {
            ;
        }

        if (poll == null) {
            return null;
        } else {
            ClusterPair link = poll.pair;
            this.pairHash.remove(poll.hash);
            return link;
        }
    }

    public boolean remove(ClusterPair link) {
        DistanceMap.Item remove = (DistanceMap.Item)this.pairHash.remove(this.hashCodePair(link));
        if (remove == null) {
            return false;
        } else {
            remove.removed = true;
            this.data.remove(remove);
            return true;
        }
    }

    public boolean add(ClusterPair link) {
        DistanceMap.Item e = new DistanceMap.Item(link);
        DistanceMap.Item existingItem = (DistanceMap.Item)this.pairHash.get(e.hash);
        if (existingItem != null) {
            System.err.println("hashCode = " + existingItem.hash + " adding redundant link:" + link + " (exist:" + existingItem + ")");
            return false;
        } else {
            this.pairHash.put(e.hash, e);
            this.data.add(e);
            return true;
        }
    }

    public Double minDist() {
        DistanceMap.Item peek = (DistanceMap.Item)this.data.peek();
        return peek != null ? peek.pair.getLinkageDistance() : null;
    }

    String hashCodePair(ClusterPair link) {
        return this.hashCodePair(link.getlCluster(), link.getrCluster());
    }

    String hashCodePair(Cluster lCluster, Cluster rCluster) {
        return this.hashCodePairNames(lCluster.getName(), rCluster.getName());
    }

    String hashCodePairNames(String lName, String rName) {
        return lName.compareTo(rName) < 0 ? lName + "~~~" + rName : rName + "~~~" + lName;
    }

    public String toString() {
        return this.data.toString();
    }

    private class Item implements Comparable<DistanceMap.Item> {
        final ClusterPair pair;
        final String hash;
        boolean removed = false;

        Item(ClusterPair p) {
            this.pair = p;
            this.hash = DistanceMap.this.hashCodePair(p);
        }

        public int compareTo(DistanceMap.Item o) {
            return this.pair.compareTo(o.pair);
        }

        public String toString() {
            return this.hash;
        }
    }
}
