package uk.co.plogic.gwt.lib.cluster.uncoil;

import uk.co.plogic.gwt.lib.cluster.domain.Coord;



public class Nest {

	private Nest nextNest;
	private Nest lastNest;

	private int leftID;
	private int rightID;
	private Coord coord;
	private int weight = 0; // number of records in input data that exist below this node
								// use getNodeWeight()

	public Nest(int leftId, int rightId, Coord c, int weight) {
		leftID = leftId;
		rightID = rightId;
		coord = c;
		this.weight = weight;
	}

	public Coord getCoord() { return coord; }
	public void setCoord(Coord c) { coord = c; }
	
	public int getWeight() { return weight; }
	public void setWeight(int w) { weight = w; }
	
	public Nest getNextNest() { return nextNest; }
	public void setNextNest(Nest nextNest) { this.nextNest = nextNest; }

	public Nest getLastNest() {	return lastNest; }
	public void setLastNest(Nest lastNest) { this.lastNest = lastNest; }

	public int getLeftID() { return leftID; }
	public void setLeftID(int leftID) {	this.leftID = leftID; }

	public int getRightID() { return rightID; }
	public void setRightID(int rightID) { this.rightID = rightID; }
	
	public String toString() {
		//return String.format("(%f,%f)", getX(), getY());
		String msg = "["+getLeftID()+"-"+getRightID()+" weight:"+getWeight();
		msg += " "+getCoord().toString()+"]";
		return msg;
	}
}
