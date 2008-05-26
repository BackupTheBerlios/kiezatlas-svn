package de.kiezatlas.deepamehta;

import java.awt.Point;
import java.util.Vector;

import de.deepamehta.PresentableTopic;

public class Cluster {
	
	private Point p;
	private String icon;
	private Vector presentables = new Vector();
	
	public Cluster (PresentableTopic presentableOne, PresentableTopic presentableTwo, String clusterIconPath) {
		
		// first object is the point of this cluster
		p = presentableOne.getGeometry();
		icon = clusterIconPath + "redball-bigger.gif";
		presentables.add(presentableOne);
		presentables.add(presentableTwo);
				
	}
	
	//Override
	public String toString() {
		return ":: " +this.presentables.toString()+" :: ";
	}
	
	public void addPresentable(PresentableTopic presentable) {
		if (!presentables.contains(presentable)) {
			//System.out.println("added "+ presentable.getID()+" into " + this.p);
			presentables.add(presentable);
		}
		// else {
//			//System.out.println("Object already present in Cluster, omitting ... ");
//		}
	}
	
	public String getIcon() {
		return icon;
	}
	
	public Vector getPresentables(){
		return this.presentables;
	}
	
	public Point getPoint() {
		return this.p;
	}

}
