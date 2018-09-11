package edu.brandeis.glycodenovo.clustering;

import java.util.Collection;
import java.util.Iterator;

public class SingleLinkageStrategy implements LinkageStrategy {
 public SingleLinkageStrategy() {
 }

 public Distance calculateDistance(Collection<Distance> distances) {
     double min = 0.0D / 0.0;
     Iterator var4 = distances.iterator();

     while(true) {
         Distance dist;
         do {
             if (!var4.hasNext()) {
                 return new Distance(min);
             }

             dist = (Distance)var4.next();
         } while(!Double.isNaN(min) && dist.getDistance() >= min);

         min = dist.getDistance();
     }
 }
}

