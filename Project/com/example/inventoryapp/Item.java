package com.example.inventoryapp;

public class Item {
	private long id;
	int qty_in_stock, qty_quota, qty_to_order;
	String name;
	
	public Item(){
		name = "";
		id = 0;
		qty_in_stock = 0;
		qty_quota = 0;
		qty_to_order = 0;
	}
	public Item (String name, long id, int qty_in_stock, int qty_quota){
		this.name = name;
		this.id = id;
		this.qty_in_stock = qty_in_stock;
		this.qty_quota = qty_quota;
		this.qty_to_order = qty_quota - qty_in_stock;
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
	public int getStock(){
		return qty_in_stock;
	}
	public void setStock(int qty_in_stock){
		this.qty_in_stock = qty_in_stock;
	}
	public int getQuota(){
		return qty_quota;
	}
	public void setQuota(int qty_quota){
		this.qty_quota = qty_quota;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
