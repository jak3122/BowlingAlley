package Control;
/* ControlDesk.java
 *
 *  Version:
 *  		$Id$
 * 
 *  Revisions:
 * 		$Log: ControlDesk.java,v $
 * 		Revision 1.13  2003/02/02 23:26:32  ???
 * 		ControlDesk now runs its own thread and polls for free lanes to assign queue members to
 * 		
 * 		Revision 1.12  2003/02/02 20:46:13  ???
 * 		Added " 's Party" to party names.
 * 		
 * 		Revision 1.11  2003/02/02 20:43:25  ???
 * 		misc cleanup
 * 		
 * 		Revision 1.10  2003/02/02 17:49:10  ???
 * 		Fixed problem in getPartyQueue that was returning the first element as every element.
 * 		
 * 		Revision 1.9  2003/02/02 17:39:48  ???
 * 		Added accessor for lanes.
 * 		
 * 		Revision 1.8  2003/02/02 16:53:59  ???
 * 		Updated comments to match javadoc format.
 * 		
 * 		Revision 1.7  2003/02/02 16:29:52  ???
 * 		Added ControlDeskEvent and ControlDeskObserver. Updated Queue to allow access to Vector so that contents could be viewed without destroying. Implemented observer model for most of ControlDesk.
 * 		
 * 		Revision 1.6  2003/02/02 06:09:39  ???
 * 		Updated many classes to support the ControlDeskView.
 * 		
 * 		Revision 1.5  2003/01/26 23:16:10  ???
 * 		Improved thread handeling in lane/controldesk
 * 		
 * 
 */

/**
 * Class that represents control desk
 *
 */

import java.util.*;
import java.io.*;

import Model.Bowler;
import Model.BowlerFile;
import Model.Lane;
import Model.Party;
import View.ControlDeskView;




public class ControlDesk extends Thread implements Observable{

	/** The collection of Lanes */
	private HashSet<Lane> lanes;

	/** The party wait queue */
	private Vector<Party> parties;

	/** The number of lanes represented */
	private int numLanes;
	
	/** The collection of subscribers */
	private Vector subscribers;
	
	private Iterator<Lane> laneIt;
	private Iterator<Party> partyIt;

    /**
     * Constructor for the ControlDesk class
     *
     * @param numlanes	the numbler of lanes to be represented
     *
     */

	public ControlDesk(int numLanes) {
		this.numLanes = numLanes;
		lanes = new HashSet<Lane>(numLanes);
		parties = new Vector<Party>();

		subscribers = new Vector();

		for (int i = 0; i < numLanes; i++) {
			lanes.add(new Lane());
		}
		
		this.start();

	}
	
	/**
	 * Main loop for ControlDesk's thread
	 * 
	 */
	public void run() {
		while (true) {
			
			assignLane();
			
			try {
				sleep(250);
			} catch (Exception e) {}
		}
	}
		

    /**
     * Retrieves a matching Bowler from the bowler database.
     *
     * @param nickName	The NickName of the Bowler
     *
     * @return a Bowler object.
     *
     */

	private Bowler registerPatron(String nickName) {
		Bowler patron = null;

		try {
			// only one patron / nick.... no dupes, no checks

			patron = BowlerFile.getBowlerInfo(nickName);

		} catch (FileNotFoundException e) {
			System.err.println("Error..." + e);
		} catch (IOException e) {
			System.err.println("Error..." + e);
		}

		return patron;
	}

    /**
     * Iterate through the available lanes and assign the paties in the wait queue if lanes are available.
     *
     */

	public void assignLane() {
		laneIt = lanes.iterator();
		partyIt = parties.iterator();
		while (laneIt.hasNext() && partyIt.hasNext()) {
			Lane curLane = laneIt.next();
			Party nextParty = partyIt.next();
			if (curLane.isPartyAssigned() == false && !nextParty.isAssigned()) {
				System.out.println("ok... assigning this party");
				curLane.assignParty(nextParty);
			}
		}
		notifyObservers();
	}

    /**
     */

	public void viewScores(Lane ln) {
		// TODO: attach a LaneScoreView object to that lane
	}

    /**
     * Creates a party from a Vector of nickNAmes and adds them to the wait queue.
     *
     * @param partyNicks	A Vector of NickNames
     *
     */

	public void addPartyQueue(Vector<String> partyNicks) {
		Vector<Bowler> partyBowlers = new Vector<Bowler>();
		for (int i = 0; i < partyNicks.size(); i++) {
			Bowler newBowler = registerPatron(((String) partyNicks.get(i)));
			partyBowlers.add(newBowler);
		}
		Party newParty = new Party(partyBowlers);
		parties.add(newParty);
		notifyObservers();
	}

    /**
     * Returns a Vector of party names to be displayed in the GUI representation of the wait queue.
	 *
     * @return a Vecotr of Strings
     *
     */

	public Vector<String> getPartyQueue() {
		Vector<String> displayPartyQueue = new Vector<String>();
		for ( int i=0; i < parties.size(); i++ ) {
			String nextParty =parties.get(i).getMembers().get(0).getNickName() + "'s Party";
			displayPartyQueue.addElement(nextParty);
		}
		return displayPartyQueue;
	}

    /**
     * Accessor for the number of lanes represented by the ControlDesk
     * 
     * @return an int containing the number of lanes represented
     *
     */

	public int getNumLanes() {
		return numLanes;
	}

    /**
     * Accessor method for lanes
     * 
     * @return a HashSet of Lanes
     *
     */

	public HashSet<Lane> getLanes() {
		return lanes;
	}
	
	public static void main(String[] args) {
		
		int numLanes = 3;
		int maxPatronsPerParty=5;
		
		ControlDesk c = new ControlDesk(numLanes);
		ControlDeskView cdv = new ControlDeskView( c, maxPatronsPerParty);
		c.addObserver( cdv );
	}

	@Override
	public void notifyObservers() {
		for(Observer o : observers){
			o.update(null,this);
		}
		
	}

	@Override
	public void addObserver(Observer o) {
		observers.add(o);
	}
}
