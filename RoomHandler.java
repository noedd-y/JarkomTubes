import java.util.*;

public class RoomHandler {

    // nama room dan list room currently
    public static HashMap<String, Room> rooms = new HashMap<>();

    //buat room baru
    public static Room createRoom(String roomName, User owner) {
        if(isOwner(owner)){
            return null;
        }
        rooms.putIfAbsent(roomName, new Room(roomName, owner));
        System.out.println("Room created: " + roomName); //log
        joinRoom(roomName, owner);
        return rooms.get(roomName);
    }

    //user masuk room
    public static boolean joinRoom(String roomName, User user) {
        // keluar dari room lama dulu, kalo bisa
        if(leaveRoom(user));{
            Room room = rooms.get(roomName);

            //kalo ada roomnya
            if(room != null){
                room.add(user);
                user.setCurrentRoom(room);
                broadcast("Has joined the room", user);
                System.out.println(user.getUsername()+" has successfully joined room");
                return true;
            }
        }
        System.out.println(user.getUsername()+" has unable to join room");
        return false;
    }

    //broadcast hanya ke 1 room
    public static void broadcast(String message, User sender) {
        Room room = sender.getCurrentRoom();

        if (room == null) return;

        String formatted = "[" + sender.getUsername() + "]: " + message;

        for (User u : room.getUsers()) {
            try {
                u.getOut().writeBytes(formatted + "\n");
                u.getOut().flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //user keluar room
    public static boolean leaveRoom(User user) {
        // if (user == null) return;

        Room room = user.getCurrentRoom();

        if (room != null) {
            boolean removed = room.remove(user);

            System.out.println("Removed: " + removed);

            if (removed) {
                user.setCurrentRoom(null);
                System.out.println("User left room: " + room); //log
                return true;
            }
        }
        System.out.println("Unable to leave room: " + room);
        return false;
    }

    // Menampilkan semua room yang tersedia
    public static String listRooms() {

        if (rooms.isEmpty()) {
            return "Belum ada room yang tersedia.";
        }

        StringBuilder result = new StringBuilder();

        result.append("=== DAFTAR ROOM ===\n");

        for (String roomName : rooms.keySet()) {

            int jumlahUser = rooms.get(roomName).size();

            result.append(roomName)
                .append(" (")
                .append(jumlahUser)
                .append(" user)\n");
        }

        return result.toString();
    }

    public static String infoRoom(Room currentRoom) {
        if(!rooms.containsValue(currentRoom))
            return "Room not found";

        StringBuilder result = new StringBuilder("=== ROOM INFO: "+currentRoom.getName()+" ===");

        result.append(currentRoom.getOwner().getUsername()).append(" (Owner)\n");
        for(User listUser: currentRoom.getUsers()){
            if(currentRoom.getOwner() == listUser)
                continue;
            result.append(listUser.getUsername())
            .append("\n");
        }

        return result.toString();
    }

    //untuk kick user
    public static void kickUser(User owner, String username) {
        Room room = owner.getCurrentRoom();

        if(room == null)
            return;

        if(room.getOwner() != owner)
            return;

        for(User u : room.getUsers()) {

            if(u.getUsername().equals(username)) {

                room.remove(u);
                u.setCurrentRoom(null);

                try {
                    u.getOut().writeBytes(
                        "You were kicked from room.\n"
                    );
                    u.getOut().flush();
                }
                catch(Exception e){}

                break;
            }
        }
    }

    //untuk owner menutup room 
    public static void closeRoom(User owner) {
        Room room = owner.getCurrentRoom();
        if(room == null)
            return;

        if(room.getOwner() != owner)
            return;

        for(User u : room.getUsers()) {
            try {
                if(u != owner) {
                    leaveRoom(u);
                    u.getOut().writeBytes(
                        "/roomclosed\n"
                    );
                    u.getOut().flush();
                }
            } catch(Exception e){}
        }
        rooms.remove(room.getName());
        owner.setCurrentRoom(null);
    }

    //check kalo user == owner
    public static boolean isOwner(User user) {
        Room room = user.getCurrentRoom();

        return room != null &&
            room.getOwner().equals(user);
    }
}