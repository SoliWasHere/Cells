package homework.Save;

/*
 * Name: Timothy Lopez
 * Assignment: Queue Exercise - VIPs at the Club
 * Period: 6
 */

import java.util.*;

/*
 * Name: QueueVip
 * Output: Displays a system to sort people in a queue based on their status
 */
public class QueueVip {
	public static void main(String[] args) {

		//Test with both non VIP, and VIP
		testBasic();
		System.out.println();
		
		//Test with empty Queue
		testEmpty();
		System.out.println();

		//Test with 0 VIPs
		testNoVIP();
		System.out.println();

		//Test with only VIPS
		testAllVIP();
		System.out.println();
		
		// Extra Credit (Test with 1 Owner, some VIPs and non VIPs)
		testBasicWithOwner();
		System.out.println();
	}
	

	/*
	 * Name: PrioritizeVips
	 * Input: clubList (Queue of PartyPerson Objects)
	 * 				   [Order matters, the order of adding relative to other
	 * 					people of the same status is kept the same]
	 * Output: Reorders the queue so that VIPs are in front and non VIPs are
	 * 		   in the back. (Maintains relative order)
	 * Exceptions: Club List can't be null.
	 */
	public static void prioritizeVips(Queue<PartyPerson> clubList) {
		if (clubList == null) {
			throw new IllegalArgumentException("Club List can't be null.");
		}

		Queue<PartyPerson> temp = new LinkedList<>();
		int size = clubList.size();

		for (int i = 0; i < size; i++) {
			temp.add(clubList.remove());
		}

		//Add the VIP people first
		for (int i = 0; i < size; i++) {
			PartyPerson current = temp.remove();
			if (current.getStatus().equals("VIP")) {
				clubList.add(current);
			} else {
				temp.add(current);
			}
		}

		//Add whoever is left
		size = temp.size();
		for (int i = 0; i < size; i++) {
			clubList.add(temp.remove());
		}
	}

	/*
	 * Name: PrioritizeVipsPlus
	 * Input: clubList (Queue of PartyPerson Objects)
	 * 				   [Order matters, the order of adding relative to other
	 * 					people of the same status is kept the same]
	 * Output: Reorders the queue so that the Owner is in front, VIPs are
	 * 		   second and non VIPs are in the back. (Maintains relative order)
	 * Exceptions: Club List can't be null.
	 */
	public static void prioritizeVipsPlus(Queue<PartyPerson> clubList) {
		if (clubList == null) {
			throw new IllegalArgumentException("Club List can't be null.");
		}

		Queue<PartyPerson> temp = new LinkedList<>();
		int size = clubList.size();

		for (int i = 0; i < size; i++) {
			temp.add(clubList.remove());
		}

		//Add the Owner first
		for (int i = 0; i < size; i++) {
			PartyPerson current = temp.remove();
			if (current.getStatus().equals("OWNER")) {
				clubList.add(current);
			} else {
				temp.add(current);
			}
		}

		//Add the VIP people second
		size = temp.size();
		for (int i = 0; i < size; i++) {
			PartyPerson current = temp.remove();
			if (current.getStatus().equals("VIP")) {
				clubList.add(current);
			} else {
				temp.add(current);
			}
		}

		//Add whoever is left
		size = temp.size();
		for (int i = 0; i < size; i++) {
			clubList.add(temp.remove());
		}
	}
	
	/*
	 * Name: testBasic
	 * Output: Test with a mix of VIPs and normals
	 */
	public static void testBasic() {
		// testing code
		Queue<PartyPerson> people = new LinkedList<>();
		people.add(new PartyPerson("Mario", "VIP"));
		people.add(new PartyPerson("Koopa Troopa"));
		people.add(new PartyPerson("Bowser", "VIP"));
		people.add(new PartyPerson("Goomba"));
		people.add(new PartyPerson("Captain Toad", "VIP"));
		
		// actual answer
		Queue<PartyPerson> peopleExpected = new LinkedList<>();
		peopleExpected.add(new PartyPerson("Mario", "VIP"));
		peopleExpected.add(new PartyPerson("Bowser", "VIP"));
		peopleExpected.add(new PartyPerson("Captain Toad", "VIP"));
		peopleExpected.add(new PartyPerson("Koopa Troopa"));
		peopleExpected.add(new PartyPerson("Goomba"));
		
		prioritizeVips(people);
		
		System.out.println("Test Basic");
		System.out.println("  Actual:   " + queueToString(people));
		System.out.println("  Expected: " + queueToString(peopleExpected));
	}

	/*
	 * Name: testEmpty
	 * Output: Test with an empty Queue
	 */
	public static void testEmpty() {
		// testing code
		Queue<PartyPerson> people = new LinkedList<>();
		
		// actual answer
		Queue<PartyPerson> peopleExpected = new LinkedList<>();
		
		prioritizeVips(people);
		
		System.out.println("Test with empty Queue");
		System.out.println("  Actual:   " + queueToString(people));
		System.out.println("  Expected: " + queueToString(peopleExpected));
	}

	/*
	 * Name: testAllVIP
	 * Output: Test with everyone as a VIP
	 */
	public static void testAllVIP() {
		// testing code
		Queue<PartyPerson> people = new LinkedList<>();
		people.add(new PartyPerson("Mario", "VIP"));
		people.add(new PartyPerson("Koopa Troopa", "VIP"));
		people.add(new PartyPerson("Bowser", "VIP"));
		people.add(new PartyPerson("Goomba", "VIP"));
		people.add(new PartyPerson("Captain Toad", "VIP"));
		
		// actual answer
		Queue<PartyPerson> peopleExpected = new LinkedList<>();
		peopleExpected.add(new PartyPerson("Mario", "VIP"));
		peopleExpected.add(new PartyPerson("Koopa Troopa", "VIP"));
		peopleExpected.add(new PartyPerson("Bowser", "VIP"));
		peopleExpected.add(new PartyPerson("Goomba", "VIP"));
		peopleExpected.add(new PartyPerson("Captain Toad", "VIP"));
		
		prioritizeVips(people);
		
		System.out.println("Test with all people as VIP");
		System.out.println("  Actual:   " + queueToString(people));
		System.out.println("  Expected: " + queueToString(peopleExpected));
	}

	/*
	 * Name: testNoVIP
	 * Output: Test with everyone as normal
	 */
	public static void testNoVIP() {
		// testing code
		Queue<PartyPerson> people = new LinkedList<>();
		people.add(new PartyPerson("Mario"));
		people.add(new PartyPerson("Koopa Troopa"));
		people.add(new PartyPerson("Bowser"));
		people.add(new PartyPerson("Goomba"));
		people.add(new PartyPerson("Captain Toad"));
		
		// actual answer
		Queue<PartyPerson> peopleExpected = new LinkedList<>();
		peopleExpected.add(new PartyPerson("Mario"));
		peopleExpected.add(new PartyPerson("Koopa Troopa"));
		peopleExpected.add(new PartyPerson("Bowser"));
		peopleExpected.add(new PartyPerson("Goomba"));
		peopleExpected.add(new PartyPerson("Captain Toad"));
		
		prioritizeVips(people);
		
		System.out.println("Test with all people as normal");
		System.out.println("  Actual:   " + queueToString(people));
		System.out.println("  Expected: " + queueToString(peopleExpected));
	}
	
	/*
	 * Name: testOwner
	 * Output: Test with one owner, some VIP, and some normal
	 */
	public static void testBasicWithOwner() {
		// testing code
		Queue<PartyPerson> people = new LinkedList<>();
		people.add(new PartyPerson("Mario", "VIP"));
		people.add(new PartyPerson("Koopa Troopa"));
		people.add(new PartyPerson("Bowser", "VIP"));
		people.add(new PartyPerson("Rosalina", "OWNER"));
		people.add(new PartyPerson("Goomba"));
		people.add(new PartyPerson("Captain Toad", "VIP"));
		
		// actual answer
		Queue<PartyPerson> peopleExpected = new LinkedList<>();
		peopleExpected.add(new PartyPerson("Rosalina", "OWNER"));
		peopleExpected.add(new PartyPerson("Mario", "VIP"));
		peopleExpected.add(new PartyPerson("Bowser", "VIP"));
		peopleExpected.add(new PartyPerson("Captain Toad", "VIP"));
		peopleExpected.add(new PartyPerson("Koopa Troopa"));
		peopleExpected.add(new PartyPerson("Goomba"));
		
		prioritizeVipsPlus(people);
		
		System.out.println("Test Basic with Owner");
		System.out.println("  Expected: " + queueToString(peopleExpected));
		System.out.println("  Actual:   " + queueToString(people));
	}
	
	// Returns a String representation of the given queue from front to back
	// e.g.) front <-  "Mario (VIP)", "Bowser (VIP)", "Koopa Troopa", "Goomba" <- back
	public static String queueToString(Queue<PartyPerson> queue) {
		if (queue == null) {
			return "Null queue";
		}

		//Note to Teacher: Had to add this to make code work.
		if (queue.isEmpty()) {
        	return "front <-  <- back";
    	}	

		String s = "front <-  ";
		
		// fencepost - print the first item
		PartyPerson first = queue.remove();
		s += "\"" + first + "\"";
		queue.add(first);
		
		// print the remaining items
		for (int i = 1; i < queue.size(); i++) {
			PartyPerson person = queue.remove();
			s += ",  \"" + person + "\"";
			queue.add(person);
		}
		
		s += "  <- back";
		
		return s;
	}
}