import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

class Room implements Serializable {
    private int roomNumber;
    private String type;
    private double price;
    private boolean isAvailable;

    public Room(int roomNumber, String type, double price, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return String.format("Room %d | Type: %s | Price: $%.2f | %s",
            roomNumber, type, price, isAvailable ? "Available" : "Booked");
    }
}

class Guest implements Serializable {
    private String name;
    private String contactInfo;
    private String idProof;

    public Guest(String name, String contactInfo, String idProof) {
        this.name = name;
        this.contactInfo = contactInfo;
        this.idProof = idProof;
    }

    @Override
    public String toString() {
        return String.format("Guest: %s | Contact: %s | ID: %s", name, contactInfo, idProof);
    }
}

class Booking implements Serializable {
    private Guest guest;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double totalPrice;

    public Booking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = calculateTotal();
    }

    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }

    public double calculateTotal() {
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        return days * room.getPrice();
    }

    @Override
    public String toString() {
        return String.format("%s\n%s\nDates: %s to %s\nTotal: $%.2f",
            guest, room, checkIn, checkOut, totalPrice);
    }
}

class HotelSystem {
    private List<Room> rooms = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getAvailableRooms(LocalDate date) {
        List<Room> available = new ArrayList<>();
        for (Room room : rooms) {
            if (isRoomAvailable(room, date)) {
                available.add(room);
            }
        }
        return available;
    }

    private boolean isRoomAvailable(Room room, LocalDate date) {
        for (Booking booking : bookings) {
            if (booking.getRoom().getRoomNumber() == room.getRoomNumber() &&
                !date.isBefore(booking.getCheckIn()) &&
                !date.isAfter(booking.getCheckOut())) {
                return false;
            }
        }
        return true;
    }

    public Booking bookRoom(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        room.setAvailable(false);
        Booking booking = new Booking(guest, room, checkIn, checkOut);
        bookings.add(booking);
        return booking;
    }

    public void listRooms() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms available.");
            return;
        }
        System.out.println("\n--- ROOMS ---");
        rooms.forEach(System.out::println);
    }

    public void listBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings yet.");
            return;
        }
        System.out.println("\n--- BOOKINGS ---");
        bookings.forEach(System.out::println);
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(rooms);
            oos.writeObject(bookings);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            rooms = (List<Room>) ois.readObject();
            bookings = (List<Booking>) ois.readObject();
            System.out.println("Data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}

public class HotelApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HotelSystem system = new HotelSystem();
        String filename = "hotel.dat";

        system.loadFromFile(filename);

        if (system.getAvailableRooms(LocalDate.now()).isEmpty()) {
            system.addRoom(new Room(101, "Single", 100.0, true));
            system.addRoom(new Room(102, "Double", 150.0, true));
            system.addRoom(new Room(103, "Suite", 250.0, true));
        }

        while (true) {
            System.out.println("\n=== HOTEL BOOKING SYSTEM ===");
            System.out.println("1. View Rooms");
            System.out.println("2. Book a Room");
            System.out.println("3. View Bookings");
            System.out.println("4. Save & Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    system.listRooms();
                    break;

                case 2:
                    System.out.print("Enter Guest Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Contact Info: ");
                    String contact = scanner.nextLine();
                    System.out.print("Enter ID Proof: ");
                    String idProof = scanner.nextLine();

                    System.out.print("Enter Check-In Date (YYYY-MM-DD): ");
                    LocalDate checkIn = LocalDate.parse(scanner.nextLine());
                    System.out.print("Enter Check-Out Date (YYYY-MM-DD): ");
                    LocalDate checkOut = LocalDate.parse(scanner.nextLine());

                    List<Room> available = system.getAvailableRooms(checkIn);
                    if (available.isEmpty()) {
                        System.out.println("No rooms available for selected dates.");
                        break;
                    }

                    System.out.println("\nAvailable Rooms:");
                    available.forEach(System.out::println);

                    System.out.print("Enter Room Number to Book: ");
                    int roomNum = scanner.nextInt();
                    Room selectedRoom = available.stream()
                        .filter(r -> r.getRoomNumber() == roomNum)
                        .findFirst()
                        .orElse(null);

                    if (selectedRoom != null) {
                        Guest guest = new Guest(name, contact, idProof);
                        Booking booking = system.bookRoom(guest, selectedRoom, checkIn, checkOut);
                        System.out.println("\nBooking Successful!\n" + booking);
                    } else {
                        System.out.println("Invalid room selection!");
                    }
                    break;

                case 3:
                    system.listBookings();
                    break;

                case 4:
                    system.saveToFile(filename);
                    System.out.println("Exiting...");
                    System.exit(0);

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}