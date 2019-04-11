package hw3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Customers are simulation actors that have two fields: a name, and a list of
 * Food items that constitute the Customer's order. When running, an customer
 * attempts to enter the coffee shop (only successful if the coffee shop has a
 * free table), place its order, and then leave the coffee shop when the order
 * is complete.
 */
public class Customer implements Runnable {
	// JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	static Random random = new Random();
	private final String name;
	private final List<Food> order;
	private final int orderNum;
	private final int priority;
	private final int eatTime;

	public int getPriority() {
		return priority;
	}

	public int getOrderNum() {
		return orderNum;
	}

	private static int runningCounter = 0;

	public List<Food> getOrder() {
		List<Food> newList = new ArrayList<Food>();
		newList.addAll(order);
		return newList;
	}

	/**
	 * You can feel free modify this constructor. It must take at least the name and
	 * order but may take other parameters if you would find adding them useful.
	 */
	public Customer(String name, List<Food> order, int priority, int eatTime) {
		this.name = name;
		this.order = order;
		this.orderNum = runningCounter++;
		this.priority = priority;
		this.eatTime = eatTime;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method defines what an Customer does: The customer attempts to enter the
	 * coffee shop (only successful when the coffee shop has a free table), place
	 * its order, and then leave the coffee shop when the order is complete.
	 */
	// invariant: 0<= numOfCustomers <= numTables;
	// 0<= orderList

	// precondition:do not interrupt the thread while it is running

	// post condition: logEvent customerStarting,
	// customerEnteredCoffeeShop,customerPlacedOrder,customerReceivedOrder,customerLeavingCoffeeShop
	// while numOfCustomers is no bigger than numTables, add the customer to the
	// numOfCustomers list and notifyAll. Otherwise wait().
	// place order by adding his order into the orderList
	// customer finished and delete it form numOfCustomers list.

	// exception:interruptedException thrown if interrupted accidently.
	public void run() {
		// YOUR CODE GOES HERE...
		// log event customer starting
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		try {

			synchronized (Simulation.numOfCustomers) {
				
				while (Simulation.numOfCustomers.size() >= Simulation.events.get(0).simParams[2]) {

					Simulation.numOfCustomers.wait();

				}
				Simulation.numOfCustomers.add(this);
				Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
				
				Simulation.numOfCustomers.notifyAll();// important to do
			}

			synchronized(Simulation.orderList) {
				Simulation.orderList.add(this);
				Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, this.order, this.orderNum));
				Simulation.orderList.notifyAll();
			}
			
			Simulation.customer_recieve_order.put(this, false);
			synchronized (Simulation.customer_recieve_order) {
				while (Simulation.customer_recieve_order.get(this) == false) {

					Simulation.customer_recieve_order.wait();

				}
				Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, this.order, this.orderNum));
				Simulation.customer_recieve_order.notifyAll();
			}
			Thread.sleep(eatTime);
			synchronized (Simulation.numOfCustomers) {
				Simulation.numOfCustomers.remove(this);
				Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
				Simulation.numOfCustomers.notifyAll();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}