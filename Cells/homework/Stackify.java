// Name: Timothy Lopez
// Period: 6

// NOTE TO TEACHER: I know the package homework isn't supposed to be there, but since I am coding it in a small folder, it is required to run code.
// Please ignore the package line.
package homework;

// Import statements
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/* 
 * Class: Stackify 
 * Input: A Queue of Song objects representing the playlist. (Or no input, which initializes an empty playlist.)
 * Output: A Stackify object that manages the playlist and history of played songs. Includes methods to manipulate 
 * and retrieve songs from the playlist and history.
 */

public class Stackify {
    private Queue<Song> playlist; //add,remove,peek
    private Stack<Song> history; //push,pop,peek

    // Number of piles to use in shuffle methods
    private int pilesCount = 3;

    /*
     * Method: Stackify
     * Input: A Queue of Song objects representing the playlist.
     * Output: Initializes the Stackify object with the given playlist and an empty history stack.
     */
    public Stackify(Queue<Song> playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null");
        }

        this.playlist = playlist;
        this.history = new Stack<>();
    }

    /*
     * Method: Stackify
     * Input: A Queue of Song objects representing the playlist.
     * Output: Initializes the Stackify object with an empty playlist and an empty history stack.
     */
    public Stackify() {
        this.playlist = new LinkedList<>();
        this.history = new Stack<>();
    }

    /*
     * Method: getPlaylist
     * Input: None
     * Output: Returns the current playlist as a Queue of Song objects.
     */
    public Queue<Song> getPlaylist() {
        return playlist;
    }

    /*
     * Method: getHistory
     * Input: None
     * Output: Returns the history of played songs as a Stack of Song objects.
     */
    public Stack<Song> getHistory() {
        return history;
    }

    /*
     * Method: getCurrentSong
     * Input: None
     * Output: Returns the current song at the front of the playlist without removing it.
     */
    public Song getCurrentSong() {
        if (playlist.isEmpty()) {
            return null;
        }

        return playlist.peek();
    }

    /*
     * Method: getPreviousSong
     * Input: None
     * Output: Returns the most recently played song from the history without removing it.
     */
    public Song getPreviousSong() {
        if (history.isEmpty()) {
            return null;
        }

        return history.peek();
    }

    /*
     * Method: addToPlaylist
     * Input: A Song object to be added to the playlist.
     * Output: Returns nothing. Adds the given song to the end of the playlist.
     */
    public void addToPlaylist(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null");
        }

        playlist.add(song);
    }

    /*
     * Method: clearHistory
     * Input: None
     * Output: Returns nothing. Clears the history stack of played songs.
     */
    public void clearHistory() {
        history = new Stack<>();
    }

    /*
     * Method: playNext
     * Input: None
     * Output: Returns the next Song in the playlist after moving the current song to history.
     */
    public Song playNext() {
        if (playlist.isEmpty()) {
            return null;
        }

        Song hold = playlist.remove();
        history.push(hold);
        return playlist.peek();
    }

    /*
     * Method: goBack
     * Input: None
     * Output: Returns the most recently played Song from history and adds it back to the front of the playlist.
     */
    public Song goBack() {
        if (history.isEmpty()) {
            return null;
        }

        Song hold = history.pop();
        playlist.add(hold);
        
        // Rotates the playlist to move the added song to the front
        for (int i = 0; i < playlist.size()-1; i++) {
            playlist.add(playlist.remove());
        }

        return hold;
    }

    /*
     * Method: removeArtist
     * Input: A String representing the artist whose songs should be removed from the playlist.
     * Output: Returns an int representing the number of songs removed from the playlist by the specified artist.
     */
    public int removeArtist(String artist) {
        if (artist == null) {
            throw new IllegalArgumentException("Artist cannot be null");
        }

        int size = playlist.size();
        int count = 0;
        for (int i = 0; i < size; i++) {
            Song current = playlist.remove();
            if (!current.getArtist().equals(artist)) {
                playlist.add(current);
            } else {
                count++;
            }
        }
        return count;
    }

    /*
     * Method: shuffle
     * Input: None
     * Output: Returns nothing. Shuffles the playlist using a default number of piles (3).
     * Explanation: This method divides the playlist into a specified number of piles (stacks),
     * randomly spreading the songs into these piles, and then remakes the playlist by
     * randomly choosing songs from the piles until all songs are back in the playlist.
     */
    public void shuffle() {
        int piles = pilesCount;

        // Ensure there are enough songs to shuffle
        if (piles <= playlist.size()) {
            ArrayList<Stack<Song>> stacks = new ArrayList<>(piles);

            // Create the stacks
            for (int i = 0; i < piles; i++) {
                stacks.add(new Stack<>());
            }

            // Distribute songs into the stacks
            int size = playlist.size();
            for (int i = 0; i< size; i++) {
                Song hold = playlist.remove();
                int index = (int) (Math.random() * piles);
                stacks.get(index).push(hold);
            }

            // Rebuild the playlist by randomly selecting from the stacks until all songs are back
            int totalSongs = size;
            while (totalSongs > 0) {
                int index = (int) (Math.random() * piles);
                if (!stacks.get(index).isEmpty()) {
                    playlist.add(stacks.get(index).pop());
                    totalSongs--;
                }
            }
        }
    }

    /*
     * Method: shuffle
     * Input: An int representing the number of piles to use for shuffling.
     * Output: Returns nothing. Shuffles the playlist using the specified number of piles.
     * Explanation: See above method for detailed explanation.
     */
    public void shuffle(int piles) {
        if (piles <= 0) {
            throw new IllegalArgumentException("Piles must be greater than 0");
        }

        // Ensure there are enough songs to shuffle
        if (piles <= playlist.size()) {
            ArrayList<Stack<Song>> stacks = new ArrayList<>(piles);

            // Create the stacks
            for (int i = 0; i < piles; i++) {
                stacks.add(new Stack<>());
            }

            // Distribute songs into the stacks
            int size = playlist.size();
            for (int i = 0; i< size; i++) {
                Song hold = playlist.remove();
                int index = (int) (Math.random() * piles);
                stacks.get(index).push(hold);
            }

            // Rebuild the playlist by randomly selecting from the stacks until all songs are back
            int totalSongs = size;
            while (totalSongs > 0) {
                int index = (int) (Math.random() * piles);
                if (!stacks.get(index).isEmpty()) {
                    playlist.add(stacks.get(index).pop());
                    totalSongs--;
                }
            }
        }
    }
}
