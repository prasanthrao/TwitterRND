package com.avishkar.sentiword;

import java.util.HashSet;
import java.util.Set;


public class SentiTweetData {
	double score = 0;
	Set<String> namedEntities = new HashSet<String>();
	
	@Override
	public String toString() {
		return "[Score:"+score+"][NamedEntities:"+namedEntities.toString()+"]";
	}

	public Set<String> getNamedEntities() {
		return namedEntities;
	}

	public void setNamedEntities(Set<String> namedEntities) {
		this.namedEntities = namedEntities;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	

}