package edu.brandeis.glycodenovo.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cluster {
    private String name;
    private Cluster parent;
    private List<Cluster> children;
    private List<String> leafNames;
    private Distance distance = new Distance();

    public Cluster(String name) {
        this.name = name;
        this.leafNames = new ArrayList();
    }

    public Distance getDistance() {
        return this.distance;
    }

    public Double getWeightValue() {
        return this.distance.getWeight();
    }

    public Double getDistanceValue() {
        return this.distance.getDistance();
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public List<Cluster> getChildren() {
        if (this.children == null) {
            this.children = new ArrayList();
        }

        return this.children;
    }

    public void addLeafName(String lname) {
        this.leafNames.add(lname);
    }

    public void appendLeafNames(List<String> lnames) {
        this.leafNames.addAll(lnames);
    }

    public List<String> getLeafNames() {
        return this.leafNames;
    }

    public void setChildren(List<Cluster> children) {
        this.children = children;
    }

    public Cluster getParent() {
        return this.parent;
    }

    public void setParent(Cluster parent) {
        this.parent = parent;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addChild(Cluster cluster) {
        this.getChildren().add(cluster);
    }

    public boolean contains(Cluster cluster) {
        return this.getChildren().contains(cluster);
    }

    public String toString() {
        return "Cluster " + this.name;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Cluster other = (Cluster)obj;
            if (this.name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!this.name.equals(other.name)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        return this.name == null ? 0 : this.name.hashCode();
    }

    public boolean isLeaf() {
        return this.getChildren().size() == 0;
    }

    public int countLeafs() {
        return this.countLeafs(this, 0);
    }

    public int countLeafs(Cluster node, int count) {
        if (node.isLeaf()) {
            ++count;
        }

        Cluster child;
        for(Iterator var3 = node.getChildren().iterator(); var3.hasNext(); count += child.countLeafs()) {
            child = (Cluster)var3.next();
        }

        return count;
    }

    public void toConsole(int indent) {
        for(int i = 0; i < indent; ++i) {
            System.out.print("  ");
        }

        String name = this.getName() + (this.isLeaf() ? " (leaf)" : "") + (this.distance != null ? "  distance: " + this.distance : "");
        System.out.println(name);
        Iterator var3 = this.getChildren().iterator();

        while(var3.hasNext()) {
            Cluster child = (Cluster)var3.next();
            child.toConsole(indent + 1);
        }

    }

    public double getTotalDistance() {
        Double dist = this.getDistance() == null ? 0.0D : this.getDistance().getDistance();
        if (this.getChildren().size() > 0) {
            dist = dist + ((Cluster)this.children.get(0)).getTotalDistance();
        }

        return dist;
    }
}
