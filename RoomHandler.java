import java.util.*;

public class RoomHandler {

    // nama room → list user di room itu
    //public static HashMap<String, Vector<DataOutputStream>> rooms = new HashMap<>();

    // user → room name (biar tahu dia ada di room mana)
    //public static HashMap<DataOutputStream, String> userRoom = new HashMap<>();
    public static HashMap<String, Room> rooms = new HashMap<>();

    // user → username
    //public static HashMap<DataOutputStream, String> userNames = new HashMap<>();
    // public static HashMap<SSLContext, User> users = new HashMap<>();

    //buat room baru
    public static Room createRoom(String roomName, User owner) {
        rooms.putIfAbsent(roomName, new Room(roomName, owner));
        System.out.println("Room created: " + roomName);
        return rooms.get(roomName);
    }

    //set username
    public static void setUserName(User user, String name) {
        user.setUsername(name);
    }
    // public static void setUserName(DataOutputStream user, String name) {
    //     userNames.put(user, name);
    //     System.out.println("User set name: " + name);
    // }

    //user masuk room
    public static void joinRoom(String roomName, User user) {
        // keluar dari room lama dulu
        if (user.getCurrentRoom() != null) 
            leaveRoom(user);

        createRoom(roomName, user);

        Room room = rooms.get(roomName);
        room.add(user);
        user.setCurrentRoom(room);
    }
    // public static void joinRoom(String roomName, DataOutputStream user) {

    //     createRoom(roomName);

    //     rooms.get(roomName).add(user);
    //     userRoom.put(user, roomName);

    //     System.out.println("User joined room: " + roomName);
    // }

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
    // public static void broadcast(String message, DataOutputStream sender) {
    //     try {
    //         String roomName = userRoom.get(sender);

    //         if (roomName == null) return;

    //         String userName = userNames.getOrDefault(sender, "Anonymous");
    //         String formattedMsg = "[" + userName + "]: " + message;

    //         Vector<DataOutputStream> users = rooms.get(roomName);

    //         for (DataOutputStream out : users) {
    //             out.writeBytes(formattedMsg + "\n");
    //             out.flush();
    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    //user keluar room
    public static void leaveRoom(User user) {
        if (user == null) return;

        Room room = user.getCurrentRoom();

        if (room != null) {
            room.remove(user);
            user.setCurrentRoom(null);
        }

        System.out.println("User left room: " + room);
    }
    // public static void leaveRoom(DataOutputStream user) {

    //     String roomName = userRoom.get(user);

    //     if (roomName != null) {
    //         rooms.get(roomName).remove(user);
    //         userRoom.remove(user);
    //         userNames.remove(user);

    //         System.out.println("User left room: " + roomName);
    //     }
    // }

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
                    u.setCurrentRoom(null);
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
}