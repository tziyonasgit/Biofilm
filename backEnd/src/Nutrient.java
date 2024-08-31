package backEnd;

// class for managing nutrients
public class Nutrient extends Monomer
{
    // paramaterised constructor for nutrient
    public Nutrient(Block position, int nutrientID, String type, char colour)
    {
        super(position, nutrientID, type, colour);
    }

    // Need to figure out what this will do //
    public void getConsumed()
    {
        // die and increase energy count //
    }    
}