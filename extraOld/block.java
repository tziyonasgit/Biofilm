package extraOld;

public class block {
    float positionX, positionY;
    int levelEPS = 0;
    boolean occupied = false;

    public block(float positionX, float positionY, int levelEPS, boolean occupied) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.levelEPS = levelEPS;
        this.occupied = occupied;

    }

    private void setEPS(int level) {
        this.levelEPS = level;
    }

    private void setPositon(float x, float y) {
        this.positionX = x;
        this.positionY = y;
    }

    private void setOccupied(boolean value) {
        this.occupied = value;
    }

    // need a getter for position, can't decide if we should do one for x and one
    // for y or if together what type

    private int getLevelEPS() {
        return levelEPS;
    }

    private boolean occupied() {
        return occupied;
    }
}
