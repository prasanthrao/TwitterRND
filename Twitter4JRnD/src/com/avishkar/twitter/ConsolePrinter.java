package com.avishkar.twitter;

public class ConsolePrinter {

	private boolean WRITE_TO_CONSOLE = false;
	
	public void writeToConsole(boolean required){
		WRITE_TO_CONSOLE = required;
	}
	
	public void write(String str){
		if(WRITE_TO_CONSOLE){
			System.out.println(str);
		}
	}
}
