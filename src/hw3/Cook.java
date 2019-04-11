package hw3;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;
	public Queue<Food> doneCooked = new LinkedList<Food>();
	private Customer customer;
	private Food food_pre = null;
	private Queue<Customer> queue_1 = new LinkedList<Customer>();
	private Queue<Customer> queue_2 = new LinkedList<Customer>();
	private Queue<Customer> queue_3 = new LinkedList<Customer>();
	/**
	 * You can feel free modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	//invariant： none
	
	//precondition: do not interrupt the thread while it is running
	//post condition: logEvent cookStarting,cookReceivedOrder,cookStartedFood,cookFinishedFood,cookCompletedOrder,cookEnding
	////While the orderlist is empty, let them wait. take an order from the orderList and remove it, then start to cook food in this order. Then notifyAll.
	//send foods to the correlated machine by adding food to the machine's food queue. While machines reaches it's max capacity, wait. Ohterwise notifyAll.
	//check the doneCooked list to see if this order is finised. 
	//exception: interruptedException thrown if interrupted accidently.
	public void run() {
		
		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while(true) {
				//YOUR CODE GOES HERE...
				
				synchronized(Simulation.orderList) {
					while(Simulation.orderList.isEmpty()) {
						Simulation.orderList.wait();
					}
					
					for (int k = 0; k < Simulation.orderList.size(); k++) {
						if (Simulation.orderList.get(k).getPriority() == 1) {
							queue_1.add(Simulation.orderList.get(k));
						} else if (Simulation.orderList.get(k).getPriority() == 2) {
							queue_2.add(Simulation.orderList.get(k));
						} else {
							queue_3.add(Simulation.orderList.get(k));
						}
					}
					
					if(!queue_1.isEmpty()) {
						customer = queue_1.poll();
						Simulation.orderList.remove(customer);
					}else if(!queue_2.isEmpty()) {
						customer = queue_2.poll();
						Simulation.orderList.remove(customer);
					}else if(!queue_3.isEmpty()){
						customer = queue_3.poll();
						Simulation.orderList.remove(customer);
					}
					queue_1 = new LinkedList<Customer>();
					queue_2 = new LinkedList<Customer>();
					queue_3 = new LinkedList<Customer>();
					Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, customer.getOrder(), customer.getOrderNum()));
					Simulation.orderList.notifyAll();
				}
				
				for(int i=0; i<customer.getOrder().size(); i++) {
					int count = 1;
					Food food = customer.getOrder().get(i);
					if(food_pre == food) continue;// if the food type is same with the previous one, just go next type because the machine cooked same type food at same time
					food_pre = customer.getOrder().get(i);
					for(int j=i+1; j<customer.getOrder().size(); j++) {
						Food same_food = customer.getOrder().get(j);
						if(food == same_food) count++;
					}

					if(food == FoodType.burger) {
						synchronized(Simulation.burger_machine.foodQueue) {
						while(Simulation.burger_machine.foodQueue.size() + count > Simulation.burger_machine.capacity) {
							Simulation.burger_machine.foodQueue.wait();
						}
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, customer.getOrderNum()));
						Simulation.burger_machine.makeFood(this, count, customer.getOrderNum());
						Simulation.burger_machine.foodQueue.notifyAll();
						}
					}else if(food == FoodType.fries) {
						synchronized(Simulation.fries_machine.foodQueue) {
						while(Simulation.fries_machine.foodQueue.size() + count > Simulation.fries_machine.capacity) {
							Simulation.fries_machine.foodQueue.wait();
						}
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, customer.getOrderNum()));
						Simulation.fries_machine.makeFood(this, count, customer.getOrderNum());
						Simulation.fries_machine.foodQueue.notifyAll();
						}
					}else if(food == FoodType.coffee) {
						synchronized(Simulation.coffee_machine.foodQueue) {
						while(Simulation.coffee_machine.foodQueue.size() + count > Simulation.coffee_machine.capacity) {
							Simulation.coffee_machine.foodQueue.wait();
						}
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, customer.getOrderNum()));
						Simulation.coffee_machine.makeFood(this, count, customer.getOrderNum());
						Simulation.coffee_machine.foodQueue.notifyAll();
						}
					}
				}
				
				synchronized(doneCooked) {
					while(doneCooked.size() != customer.getOrder().size()) {
						doneCooked.wait();
						doneCooked.notifyAll();
					}
				}
				
				Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, customer.getOrderNum()));
				
				synchronized(Simulation.customer_recieve_order) {
					Simulation.customer_recieve_order.put(customer, true);
					Simulation.customer_recieve_order.notifyAll();
				}
				
				doneCooked = new LinkedList<Food>();
				
				
				
				
			}
		}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}
}