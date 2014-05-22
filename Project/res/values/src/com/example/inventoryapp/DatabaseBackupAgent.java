package com.example.inventoryapp;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

public class DatabaseBackupAgent extends BackupAgentHelper 
{
	//Create a string variable that would store name of the file 'database' you want to backup
	static final String DATABASE_BACKUP_NAME = "inventory.db";

	//crate a variable that would uniquely identity the backup 
	//This is giving the backup operation an id name
	static final String DATABASE_BACKUP_KEY = "backup";


	 //Create the onCreate method that would add the 'backup helper' to the BackupAgent 
	public void onCreate()
	{
		//Create the backup helper for the agent to use that would perform the backup and restore 
		//and Then give the backup helper to the backupagent 
		 FileBackupHelper database_backup_helper = new FileBackupHelper(this, DATABASE_BACKUP_NAME);
		 
		 //This is adding the backup helper 'database_helper' to the the backup agent 'DatabaseBackupAgent'
		 addHelper(DATABASE_BACKUP_KEY, database_backup_helper); 
		 
	}

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
	          ParcelFileDescriptor newState) throws IOException 
	{
	    
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
	        ParcelFileDescriptor newState) throws IOException 
	{
	    
	}
	
}
