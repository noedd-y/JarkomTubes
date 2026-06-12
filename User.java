import java.io.DataOutputStream;

public class User {

    private String username;
    private Room currentRoom;
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

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public DataOutputStream getOut() {
        return out;
    }

    // REVISI: Mengembalikan objek 'out' asli agar bisa dipakai untuk menulis bytes ke Client
    public DataOutputStream getOutputStream() {
        return out;
    }
}