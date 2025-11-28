package homework.Save;

import java.util.Arrays;

// A simple representation of a PartyPerson with a name and a String status
// Valid statuses are: "NORMAL", "VIP", or "OWNER"

public class PartyPerson {
	private String name;
	private String status;
	
	public static final String[] statuses = {"NORMAL", "VIP", "OWNER"};

	// Constructs a PartyPerson with the given name and a status of NORMAL
	// Throws an IllegalArgumentException if the name is null or empty
	public PartyPerson(String name) {
		this(name, "NORMAL");
	}

	// Constructs a PartyPerson with the given name and VIP status
	// Valid statuses are: "NORMAL", "VIP", or "OWNER"
	// Throws an IllegalArgumentException if the name is null or empty
	// Throws an IllegalArgumentException if the status is null or invalid
	public PartyPerson(String name, String status) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be null or empty");
		}

		if (status == null) {
			throw new IllegalArgumentException("Status cannot be null");
		} else if (!Arrays.asList(statuses).contains(status)) {
			throw new IllegalArgumentException("Status is invalid: " + status);
		}

		this.name = name;
		this.status = status;
	}

	// Returns the name of this PartyPerson
	public String getName() {
		return name;
	}

	// Returns the String status of this PartyPerson
	// i.e.) "NORMAL", "VIP", or "OWNER"
	public String getStatus() {
		return status;
	}

	// Sets the name of this PartyPerson to the given name
	// Throws an IllegalArgumentException if the name is null or empty
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be null or empty");
		}

		this.name = name;
	}

	// Sets the VIP status of this PartyPerson to the given String status
	// Valid statuses are: "NORMAL", "VIP", or "OWNER"
	// Throws an IllegalArgumentException if the status is null or invalid
	public void changeStatus(String status) {
		if (status == null) {
			throw new IllegalArgumentException("Status cannot be null");
		} else if (!Arrays.asList(statuses).contains(status)) {
			throw new IllegalArgumentException("Status is invalid: " + status);
		}
		
		this.status = status;
	}

	// Returns a String representation of this PartyPerson
	// e.g.) "Ali" or "Ryan (VIP)" or "Mr. Brown (Owner)"
	@Override
	public String toString() {
		String s = this.name;
		if (status.equals("OWNER")) {
			s += " (Owner)";
		} else if (status.equals("VIP")) {
			s += " (VIP)";
		}

		return s;
	}
	
	// Returns whether or not the given object is equal to this PartyPerson
	// Two PartyPerson objects are considered equal if they have the same name
	// and status.
	@Override
	public boolean equals(Object obj) {
		// Check if same instance of PartyPerson
		if (this == obj) {
			return true; 
		}
          
		// Check if not a PartyPerson object
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        
        // Type cast to PartyPerson, because we know it is
        PartyPerson other = (PartyPerson) obj; 
          
        return other.name.equals(this.name) && other.status.equals(this.status); 
	}
}