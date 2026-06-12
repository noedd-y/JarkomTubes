import java.util.Vector;
public class Room {
    private String roomName;
    private User owner;
    private Vector<User> users;

    public Room(String roomName, User owner) {
        this.roomName = roomName;
        this.owner = owner;
        this.users = new Vector<>();
        this.users.add(owner);
    }

    public User getOwner() {
        return owner;
    }

    public Vector<User> getUsers() {
        return users;
        }

    public String getName(){
        return roomName;
    }

    public int size(){
        return users.size();
            }

    public void add(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public boolean remove(User user) {
        //butuh handle klo dia remove owner
        if(user.equals(getOwner()))
            return false;

        return(users.remove(user));
        }
}
