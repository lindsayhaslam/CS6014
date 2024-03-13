package lindsay;
import java.util.HashMap;
import java.util.Set;

public class Router {

    private HashMap<Router, Integer> distances;
    private String name;
    public Router(String name) {
        this.distances = new HashMap<>();
        this.name = name;
    }

    public void onInit() throws InterruptedException {

        //TODO: IMPLEMENT ME
        //As soon as the network is online,
        //fill in your initial distance table and broadcast it to your neighbors
        this.distances.put(this, 0);
        for (Neighbor neighbor : Network.getNeighbors(this)) {
            this.distances.put(neighbor.getRouter(), neighbor.getCost());
        }
        for (Neighbor neighbor : Network.getNeighbors(this)){
            Network.sendDistanceMessage(new Message(this, neighbor.router, new HashMap<>(this.distances)));
        }
    }

    public void onDistanceMessage(Message message) throws InterruptedException {
        //update your distance table and broadcast it to your neighbors if it changed
        boolean updated = false;
        HashMap<Router, Integer> receivedDistances = message.distances;
        Router sender = message.sender;
        Integer distanceToSender = this.distances.get(sender);

        if(distanceToSender == null){
            distanceToSender = Integer.MAX_VALUE;
            this.distances.put(sender, distanceToSender);
        }

        for (HashMap.Entry<Router, Integer> entry : receivedDistances.entrySet()){
            Router currentRouter = entry.getKey();
            Integer currentDistance = entry.getValue();
            Integer currentKnownDistance = this.distances.getOrDefault(currentRouter, Integer.MAX_VALUE);

            int calculatedDistance = currentDistance + distanceToSender;
            if(calculatedDistance < currentKnownDistance){
                this.distances.put(currentRouter, calculatedDistance);
                updated = true;
            }
        }

//        for (Router neighbor : receivedDistances.keySet()){
//            int currentDistance = distances.getOrDefault(neighbor, Integer.MAX_VALUE);
//            int receivedDistance = receivedDistances.get(neighbor);
//
//            if (receivedDistance < currentDistance){
//                distances.put(neighbor, receivedDistance);
//                updated = true;
//
//            }
//        }

        if (updated){
            for (Neighbor neighbor : Network.getNeighbors(this)){
                Network.sendDistanceMessage(new Message(this, neighbor.router, new HashMap<>(this.distances)));
            }
        }
    }

    public void dumpDistanceTable() {
        System.out.println("router: " + this);
        for(Router r : distances.keySet()){
            System.out.println("\t" + r + "\t" + distances.get(r));
        }
    }

    @Override
    public String toString(){
        return "Router: " + name;
    }
}