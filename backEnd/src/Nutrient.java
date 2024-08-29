package backEnd;

// class for managing nutrients
public class Nutrient
{
    Block position;
    int nutrientID;
    // do we need the below attributes //
    String type;
    SimulationModel sim;

    // paramaterised constructor for nutrient
    public Nutrient(Block position, int nutrientID, String type)
    {
        this.position = position;
        this.nutrientID = nutrientID;
        this.type = type;
    }

    // method for returning nutrient type
    // do we need this method //
    public String getType()
    {
        return this.type;
    }

    // method for setting nutrient type
    // do we need this method //
    public void setType(String type)
    {
        this.type = type;
    }

    // Need to figure out what this will do //
    public void getConsumed()
    {
        // die and increase energy count //
    }

    // method for returning ID of nutrient
    public int getNID()
    {
        return this.nutrientID;
    }
    
}