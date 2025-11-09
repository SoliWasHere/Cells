package homework;

import java.util.*;

/**
 * Interactive client for Stackify.
 * Users can control their playlist through a menu system.
 */
public class StackifyClient {
    private static Scanner console = new Scanner(System.in);
    private static Stackify stackify;
    
    public static void main(String[] args) {
        printWelcome();
        initializePlaylist();
        
        boolean running = true;
        while (running) {
            displayState();
            printMenu();
            
            String choice = console.nextLine().trim();
            System.out.println();
            
            switch (choice) {
                case "1":
                    playNext();
                    break;
                case "2":
                    goBack();
                    break;
                case "3":
                    addSong();
                    break;
                case "4":
                    removeArtist();
                    break;
                case "5":
                    shuffle();
                    break;
                case "6":
                    viewDetails();
                    break;
                case "7":
                    running = false;
                    System.out.println("👋 Thanks for using Stackify! Keep jamming! 🎵");
                    break;
                default:
                    System.out.println("❌ Invalid choice. Please try again.\n");
            }
            
            if (running && !choice.equals("7")) {
                System.out.print("Press Enter to continue...");
                console.nextLine();
            }
        }
    }
    
    /**
     * Prints welcome message
     */
    private static void printWelcome() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                      STACKIFY                             ║");
        System.out.println("║          Your Interactive Music Player                    ║");
        System.out.println("║        Powered by Queues and Stacks!                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Initializes the playlist with some songs
     */
    private static void initializePlaylist() {
        System.out.println("🎵 Let's set up your initial playlist!\n");
        System.out.println("Choose a starter playlist:");
        System.out.println("  1. Classic Rock");
        System.out.println("  2. 90s Hits");
        System.out.println("  3. Jazz Standards");
        System.out.println("  4. Custom (enter your own songs)");
        System.out.print("\nChoice: ");
        
        String choice = console.nextLine().trim();
        System.out.println();
        
        Queue<Song> songs = new LinkedList<>();
        
        switch (choice) {
            case "1":
                songs.offer(new Song("Bohemian Rhapsody", "Queen"));
                songs.offer(new Song("Stairway to Heaven", "Led Zeppelin"));
                songs.offer(new Song("Hotel California", "Eagles"));
                songs.offer(new Song("Sweet Child O' Mine", "Guns N' Roses"));
                songs.offer(new Song("Back in Black", "AC/DC"));
                System.out.println("🎸 Classic Rock playlist loaded!\n");
                break;
            case "2":
                songs.offer(new Song("Smells Like Teen Spirit", "Nirvana"));
                songs.offer(new Song("Wonderwall", "Oasis"));
                songs.offer(new Song("Black Hole Sun", "Soundgarden"));
                songs.offer(new Song("Creep", "Radiohead"));
                songs.offer(new Song("No Rain", "Blind Melon"));
                System.out.println("📼 90s Hits playlist loaded!\n");
                break;
            case "3":
                songs.offer(new Song("Take Five", "Dave Brubeck"));
                songs.offer(new Song("So What", "Miles Davis"));
                songs.offer(new Song("Round Midnight", "Thelonious Monk"));
                songs.offer(new Song("My Funny Valentine", "Chet Baker"));
                songs.offer(new Song("Blue in Green", "Bill Evans"));
                System.out.println("🎷 Jazz Standards playlist loaded!\n");
                break;
            case "4":
                System.out.println("Enter songs (format: Title, Artist). Type 'done' when finished.");
                while (true) {
                    System.out.print("Song: ");
                    String input = console.nextLine().trim();
                    if (input.equalsIgnoreCase("done")) {
                        break;
                    }
                    String[] parts = input.split(",");
                    if (parts.length == 2) {
                        songs.offer(new Song(parts[0].trim(), parts[1].trim()));
                    } else {
                        System.out.println("Invalid format. Try again.");
                    }
                }
                if (songs.isEmpty()) {
                    songs.offer(new Song("Default Song", "Default Artist"));
                }
                System.out.println("\n🎵 Custom playlist created!\n");
                break;
            default:
                songs.offer(new Song("Imagine", "John Lennon"));
                songs.offer(new Song("Yesterday", "The Beatles"));
                songs.offer(new Song("Purple Haze", "Jimi Hendrix"));
                System.out.println("🎵 Default playlist loaded!\n");
        }
        
        stackify = new Stackify(songs);
        System.out.println("✨ Stackify ready! Let's go!\n");
    }
    
    /**
     * Displays current state
     */
    private static void displayState() {
        System.out.println("\n" + "═".repeat(61));
        System.out.println("                    CURRENT STATE");
        System.out.println("═".repeat(61));
        System.out.println();
        
        // Current Song
        Song current = stackify.getCurrentSong();
        if (current == null) {
            System.out.println("🎵 NOW PLAYING: (playlist empty)");
        } else {
            System.out.println("🎵 NOW PLAYING: " + current);
        }
        
        // Last played
        Song last = stackify.getPreviousSong();
        if (last != null) {
            System.out.println("⏮️  LAST PLAYED: " + last);
        }
        
        // Queue preview
        Queue<Song> playlist = stackify.getPlaylist();
        System.out.print("\n📋 UP NEXT: ");
        if (playlist.isEmpty()) {
            System.out.println("(empty)");
        } else {
            int count = Math.min(3, playlist.size());
            playlist.add(playlist.poll()); // To skip current song
            for (int i = 0; i < playlist.size()-1; i++) {
                Song song = playlist.poll();
                if (i < count) {
	                if (i == 0) {
	                    System.out.print(song.getTitle());
	                } else {
	                    System.out.print(", " + song.getTitle());
	                }
                }
                playlist.offer(song);
            }
            if (playlist.size() > 3) {
                System.out.print(" (+" + (playlist.size() - 3) + " more)");
            }
            System.out.println();
        }
        
        // History preview
        Stack<Song> history = stackify.getHistory();
        System.out.print("📚 HISTORY: ");
        if (history.isEmpty()) {
            System.out.println("(empty)");
        } else {
            System.out.println(history.size() + " song(s) played");
        }
        
        System.out.println();
    }
    
    /**
     * Prints the menu
     */
    private static void printMenu() {
        System.out.println("─".repeat(61));
        System.out.println("WHAT WOULD YOU LIKE TO DO?");
        System.out.println("─".repeat(61));
        System.out.println("  1. ▶️  Play Next Song");
        System.out.println("  2. ⏮️  Go Back (replay last song)");
        System.out.println("  3. ➕ Add Song to Playlist");
        System.out.println("  4. 🗑️  Remove All Songs by Artist");
        System.out.println("  5. 🎲 Shuffle Playlist");
        System.out.println("  6. 👁️  View Full Details");
        System.out.println("  7. 🚪 Exit");
        System.out.println("─".repeat(61));
        System.out.print("Enter choice: ");
    }
    
    /**
     * Action: Play next song
     */
    private static void playNext() {
        Song current = stackify.getCurrentSong();
        if (current == null) {
            System.out.println("❌ Playlist is empty! Add some songs first.\n");
            return;
        }
        
        System.out.println("▶️  Played: " + current);
        
        Song next = stackify.playNext();
        if (next == null) {
            System.out.println("📭 Playlist is now empty.\n");
        } else {
            System.out.println("⏭️  Up next: " + next + "\n");
        }
    }
    
    /**
     * Action: Go back
     */
    private static void goBack() {
        Song previous = stackify.goBack();
        if (previous == null) {
            System.out.println("❌ No history yet! Play some songs first.\n");
        } else {	
	        System.out.println("⏮️  Going back to: " + previous);
	        System.out.println("✓ Song moved to front of playlist!\n");
        }
    }
    
    /**
     * Action: Add song
     */
    private static void addSong() {
        System.out.print("🎵 Song title: ");
        String title = console.nextLine().trim();
        
        System.out.print("🎤 Artist: ");
        String artist = console.nextLine().trim();
        
        if (title.isEmpty() || artist.isEmpty()) {
            System.out.println("❌ Title and artist cannot be empty!\n");
            return;
        }
        
        Song song = new Song(title, artist);
        stackify.addToPlaylist(song);
        System.out.println("✓ Added: " + song + "\n");
    }
    
    /**
     * Action: Remove artist
     */
    private static void removeArtist() {
        System.out.print("🗑️  Artist to remove: ");
        String artist = console.nextLine().trim();
        
        if (artist.isEmpty()) {
            System.out.println("❌ Artist name cannot be empty!\n");
            return;
        }
        
        int removed = stackify.removeArtist(artist);
        if (removed == 0) {
            System.out.println("❌ No songs by \"" + artist + "\" found.\n");
        } else {
            System.out.println("✓ Removed " + removed + " song(s) by \"" + artist + "\"\n");
        }
    }
    
    /**
     * Action: Shuffle
     */
    private static void shuffle() {
        int size = stackify.getPlaylist().size();
        if (size < 3) {
            System.out.println("❌ Need at least 3 songs to shuffle!\n");
            return;
        }
        
        System.out.println("🎲 Shuffling playlist using pile algorithm...");
        stackify.shuffle();
        System.out.println("✓ Playlist shuffled!\n");
    }
    
    /**
     * Action: View full details
     */
    private static void viewDetails() {
        System.out.println("\n" + "═".repeat(61));
        System.out.println("                   DETAILED VIEW");
        System.out.println("═".repeat(61));
        System.out.println();
        
        // History
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ 📚 HISTORY (Stack - Most Recent on Top)                 │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        
        if (stackify.getHistory().isEmpty()) {
            System.out.println("│   (empty)                                               │");
        } else {
            Stack<Song> temp = new Stack<>();
            int count = 1;
            while (!stackify.getHistory().isEmpty()) {
                Song song = stackify.getHistory().pop();
                String line = String.format("│ %d. %-53s │", count, song);
                System.out.println(line);
                temp.push(song);
                count++;
            }
            // Restore
            while (!temp.isEmpty()) {
                stackify.getHistory().push(temp.pop());
            }
        }
        
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // Current Song
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ 🎵 NOW PLAYING                                          │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        
        Song current = stackify.getCurrentSong();
        if (current == null) {
            System.out.println("│   (playlist empty)                                      │");
        } else {
            String line = String.format("│ ♪ %-54s │", current);
            System.out.println(line);
        }
        
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // Playlist
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ 📋 UP NEXT (Queue)                                      │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        
        if (stackify.getPlaylist().isEmpty()) {
            System.out.println("│   (empty)                                               │");
        } else {
            Queue<Song> playlist = stackify.getPlaylist();
            int size = playlist.size();
            playlist.add(playlist.poll()); // To skip current song
            for (int i = 0; i < size-1; i++) { // -1 to account for skipped song
                Song song = playlist.poll();
                String line = String.format("│ %d. %-53s │", i + 1, song);
                System.out.println(line);
                playlist.offer(song);
            }
        }
        
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
    }
}