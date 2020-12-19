package autoclave;

public class LinkedListQueue {

	Node front, back;
	
	public LinkedListQueue() {
			front = back = null;
		}
		
	public void printQueue() {
		Node temp = front;
		System.out.println("Values utilized for processing... will be used for a graph etc as well ");
		
		while (temp!= null) {
			System.out.print(temp.datain);
			temp = temp.next;
			
			if (temp != null)
				System.out.print(", ");
			
		}
	}
	
	
	public void enqueue(float datain) {
		Node temp = new Node(datain);
		
		if (this.back == null) {
			this.front = this.back = temp;
		return;
		}
		this.back.next = temp;
		this.back = temp;

	}

		
	
	public void dequeue() {
		if (this.front == null) return;
		this.front = this.front.next;
		if (this.front == null) this.back = null;
	}
	
	
	public boolean isEmpty() {
		return front == null;
	}
	
	
	
	public float front() {
		if (!isEmpty()) {
			return front.datain;
		}
		
		else {
			System.out.println(" Empty Q.");
			return -1;
		}
	}
		
		
		
	public float back() {
		if (!isEmpty())
		return back.datain;
		
		else {
			System.out.println("no temperture values to print.");
			return -1;
		}
	}
	
	public float size() {
		Node temp = front;
		int nodeCounter = 0;
		while (temp != null) {
			temp = temp.next;
			nodeCounter++;
		}
		return nodeCounter;
	}
}