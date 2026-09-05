/**
 * Represents the resource part of the game.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */
public class Resource implements Cloneable {
    private String name;

    /**
     * Creates a new Resource object.
     *
     * @param name the name value
     */
    public Resource(String name) {
        this.name = name;
    }

    /**
     * Returns the name.
     *
     * @param o the o value
     *
     * @return the name
     */
    public String getName()       { return name; }

    /**
     * Two Resources are equal if their names match (case sensitive)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        return name.equals(((Resource) o).name);
    }

    @Override
    /**
     * Checks whether h code.
     *
     * @return true if h code, false otherwise
     */
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    /**
     * Handles the clone behavior.
     *
     * @return copy of this object
     */
    public Resource clone() {
        try {
            return (Resource) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    /**
     * Handles the to string behavior.
     *
     * @return text description of this object
     */
    public String toString() { return name; }
}