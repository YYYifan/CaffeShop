package hw3;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A Machine is used to make a particular Food. Each Machine makes just one kind
 * of Food. Each machine has a capacity: it can make that many food items in
 * parallel; if the machine is asked to produce a food item beyond its capacity,
 * the requester blocks. Each food item takes at least item.cookTimeMS
 * milliseconds to produce.
 */
public class Machine {
	public final String machineName;
	public final Food machineFoodType;
	public int capacity;
	// YOUR CODE GOES HERE...
	public Queue<Food> foodQueue;

	/**
	 * The constructor takes at least the name of the machine, the Food item it
	 * makes, and its capacity. You may extend it with other arguments, if you wish.
	 * Notice that the constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever initialization etc.
	 * you need).
	 */
	public Machine(String nameIn, Food foodIn, int capacityIn) {
		this.machineName = nameIn;
		this.machineFoodType = foodIn;
		foodQueue = new LinkedList<Food>();
		this.capacity = capacityIn;
		// YOUR CODE GOES HERE...

	}

	// invariant: 0 <= foodQueue <=capacityIn

	/**
	 * This method is called by a Cook in order to make the Machine's food item. You
	 * can extend this method however you like, e.g., you can have it take extra
	 * parameters or return something other than Object. It should block if the
	 * machine is currently at full capacity. If not, the method should return, so
	 * the Cook making the call can proceed. You will need to implement some means
	 * to notify the calling Cook when the food item is finished.
	 */
	// precondition: do not interrupt the thread while it is running
	// post condition: logging machine cooking food event into the list in
	// simulation class.
	// let cooks wait when the machine reaches it's max capacity, notify cooks when
	// their food is finished
	// and let other cooks in.
	// exception: interruptedException thrown if interrupted accidently.
	public void makeFood(Cook cook, int count, int orderNumber) throws InterruptedException {
		synchronized (foodQueue) {
			for (int i = 0; i < count; i++) {
				foodQueue.add(machineFoodType);
			}
		}
		new Thread(new CookAnItem(cook, count, orderNumber)).start();
	}

	// THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {

		// precondition: do not interrupt the thread while it is running

		// post condition:logEvent machineCookingFood,machineDoneFood,cookFinishedFood
		// let the thread sleep specific time then remove an item form the food queue.
		// add the finished food to the cook's DoneCooked list.

		// exception: interruptedException thrown if interrupted accidently.
		Cook cook;
		int count;
		int orderNumber;

		public CookAnItem(Cook cook, int count, int orderNumber) {
			this.cook = cook;
			this.count = count;
			this.orderNumber = orderNumber;
		}

		public void run() {
			Simulation.logEvent(SimulationEvent.machineCookingFood(Machine.this, machineFoodType));
			try {
				// YOUR CODE GOES HERE...
				Thread.sleep(machineFoodType.cookTimeMS * count);
				Simulation.logEvent(SimulationEvent.machineDoneFood(Machine.this, machineFoodType));
				Simulation.logEvent(SimulationEvent.cookFinishedFood(cook, machineFoodType, orderNumber));
				synchronized (foodQueue) {
					for (int i = 0; i < count; i++) {
						foodQueue.remove();
					}
					foodQueue.notifyAll();
				}
				synchronized (cook.doneCooked) {
					for (int i = 0; i < count; i++) {
						cook.doneCooked.add(machineFoodType);
					}
					cook.doneCooked.notifyAll();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				// Simulation.logEvent(SimulationEvent.machineEnding(Machine.this));
			}
		}
	}

	public String toString() {
		return machineName;
	}
}