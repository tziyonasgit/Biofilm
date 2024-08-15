package BiofilmSimulation;

// class for managing nutrients
public class Nutrient
{
    Block position;
    int nutrientID;
    // do we need the below attribute //
    SimulationModel sim;

    // paramaterised constructor for nutrient
    public Nutrient(Block position, int nutrientID)
    {
        this.position = position;
        this.nutrientID = nutrientID;
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