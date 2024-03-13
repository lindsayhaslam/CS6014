package lindsay;
public class Neighbor {
    Router router;
    int cost;

    public Neighbor(Router router, int cost) {
        this.router = router;
        this.cost = cost;
    }

    public Router getRouter(){
        return router;
    }

    public int getCost(){
        return cost;
    }

    @Override
    public String toString(){
        return "to " + router + " cost: " + cost;
    }
}