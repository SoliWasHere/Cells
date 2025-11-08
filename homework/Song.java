package homework;

/**
 * Represents a song with a title and artist.
 */
public class Song {
    private String title;
    private String artist;
    
    /**
     * Constructs a Song with the given title and artist.
     */
    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }
    
    /**
     * Returns the title of this song.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Returns the artist of this song.
     */
    public String getArtist() {
        return artist;
    }
    
    @Override
    public String toString() {
        return "\"" + title + "\" by " + artist;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song other = (Song) o;
        return this.title.equals(other.title) && this.artist.equals(other.artist);
    }
}