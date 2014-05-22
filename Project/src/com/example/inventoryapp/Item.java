package com.example.inventoryapp;

public class Item {
	private long id;
	double min, max, current;
	String name, type, supplier;
	
	public Item(){
		name = "";
		type = "";
		id = 0;
		min = 0;
		max = 0;
		supplier = "";
		current = 0;
	}
	public Item (long id, String name, String type, double min, double max, String supplier, double current){
		this.name = name;
		this.type = type;
		this.id = id;
		this.min = min;
		this.max = max;
		this.supplier = supplier;
		this.current = current;
	}
	public long getId(){
		return id;
	}
	public void setId(long id){
		this.id = id;
	}
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public double getMin(){
		return min;
	}
	public double getMax(){
		return max;
	}
	public void setQuota(int min){
		this.min = min;
	}
	public String getType(){
		return type;
	}
	public String getSupplier(){
		return supplier;
	}
	public double getCurrent(){
		return current;
	}
	@Override
	public String toString(){
		return name;
	}
}
