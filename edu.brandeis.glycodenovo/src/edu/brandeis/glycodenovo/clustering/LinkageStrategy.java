
package edu.brandeis.glycodenovo.clustering;

import java.util.Collection;

public interface LinkageStrategy {
    Distance calculateDistance(Collection<Distance> var1);
}
