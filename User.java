import java.io.DataOutputStream;

public class User {

    private String username;
    private String currentRoom;
    private DataOutputStream out;

    public User(DataOutputStream out) {
        this.out = out;
        this.username = "Anonymous";
        this.currentRoom = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

    public DataOutputStream getOut() {
        return out;
    }
}