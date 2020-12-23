package testclasses;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import ezcol.debug.Debugger;
import ezcol.debug.Debugger;
import ezcol.visual.visual3D.Element3D;

public class TestTreeSet {
	
	static int N = 160;
	static Float[] nums = new Float[N];
	static Comparator<Element> comparator = new Comparator<Element>() {

		@Override
		public int compare(Element o1, Element o2) {
			// TODO Auto-generated method stub
			if (o1.getzpos() < o2.getzpos())
				return 1;
			else if (o1.getzpos() > o2.getzpos())
				return -1;
			else
				return 0;
		}

	};
	
	static Set<Element> set = new TreeSet<Element>(comparator);
	static List<Element> vect = new Vector<Element>();
	

	public static void test() {
		// TODO Auto-generated method stub
		
		Random rnd = new Random();
		for(int i=0;i<N;i++)
			nums[i] = rnd.nextFloat();
		
		//for(int i=0;i<N;i++)
		//	set.add(new Element((float)i));
		
		for(int i=0;i<N;i++)
			vect.add(new Element(nums[i]));
		
		Iterator it;
		
		/*it = set.iterator();
		System.out.println("TreeSet: ");
		while(it.hasNext())
			System.out.print(((Element)it.next()).getzpos()+" ");
		System.out.println();*/
		
		
		/*it = vect.iterator();
		System.out.println("Vector: ");
		while(it.hasNext())
			System.out.print(((Element)it.next()).getzpos()+" ");
		System.out.println();*/
		
		Element[] elements = vect.toArray(new Element[0]);
		Debugger.setTime("set");
		for(int i=0;i<N;i++){
			set.remove(elements[i]);
			set.add(elements[i]);
		}
		Debugger.printTime("set", "ms");
		
		/*it = set.iterator();
		System.out.println("TreeSet: ");
		while(it.hasNext())
			System.out.print(((Element)it.next()).getzpos()+" ");
		System.out.println();*/
		
		Debugger.setTime("sort");
		Collections.sort(vect, comparator);
		Debugger.printTime("sort", "ms");
	
		/*it = vect.iterator();
		System.out.println("Vector: ");
		while(it.hasNext())
			System.out.print(((Element)it.next()).getzpos()+" ");
		System.out.println();*/
		
		
		
	}
	
	
}

class Element{
	
	float data;
	
	public Element(float data){
		this.data = data;
	}
	
	public void set(float data){
		this.data = data;
	}
	
	public float getzpos(){
		return data;
	}
}
