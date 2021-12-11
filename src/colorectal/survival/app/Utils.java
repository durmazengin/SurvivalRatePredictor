package colorectal.survival.app;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Utils 
{
	public static double calculateMean(double [] data)
	{
		double total = 0.0;
		for(int i = 0; i < data.length; i++)
		{
			total += data[i];
		}
		
		return total / data.length;
	}
	public static double calculateCov(double [] data)
	{
		double mean = calculateMean(data);
		double total = 0.0;
		for(int i = 0; i < data.length; i++)
		{
			total += (data[i] - mean) * (data[i] - mean);
		}
		
		return total / (data.length - 1);
	}
	
	public static double calculateMean(int [] data)
	{
		double total = 0.0;
		for(int i = 0; i < data.length; i++)
		{
			total += data[i];
		}
		
		return total / data.length;
	}
	
	public static double[][] readFileToMatrix(String path)
	{
		try
		{
			java.util.Scanner input = new java.util.Scanner (new java.io.File(path));
			// pre-read in the number of rows/columns
			int rowCount = 0;
			int columnCount = 0;
			
			while(input.hasNextLine())
			{
				if(rowCount == 0)
				{
				    java.util.Scanner colReader = new java.util.Scanner(input.nextLine());
				    while(colReader.hasNextInt())
				    {
				    	columnCount++;
				    }
				    colReader.close();
				}
				rowCount++;
			}
			input.close();
			
			return readFileToMatrix(path, rowCount, columnCount);
		}
		catch(FileNotFoundException fnfe)
		{
			return null;
		}
	}
	public static double[][] readFileToMatrix(String path, int rowCount, int columnCount) 
	{
		try
		{			
			java.util.Scanner input = new java.util.Scanner (new java.io.File(path));
			List<String> lines = new ArrayList<String>();
	
			for(int i = 0; i < rowCount; i++)// i for row counter
			{
				String line = input.nextLine();
				if(line.length() == 0)
				{
					break;
				}
				lines.add(line);
			}
			rowCount = lines.size();

			double[][] matrix = new double[rowCount][columnCount];
			
			for(int i = 0; i < rowCount; i++)
			{
				String line = lines.get(i);
				
			    java.util.Scanner colReader = new java.util.Scanner(line);
			    for(int j = 0; j < columnCount; j++) // j for row counter
			    {
			    	try
			    	{
			    		matrix[i][j] = colReader.nextInt();
			    	}
			    	catch(Exception ex) {}
			    }
			    colReader.close();
			}
			input.close();
			
			return matrix;
		}
		catch(FileNotFoundException fnfe)
		{
			return null;
		}
	}
	public static double[] getColumn(double[][] matrix, int columnIndex) 
	{
		double [] column = new double[matrix.length];
		for(int i = 0; i < column.length; i++)
		{
			column[i] = matrix[i][columnIndex];
		}
		return column;
	}
	
	public static double[][] getPartial(double[][] allData, int startRow, int startColumn, int rows, int columns) 
	{
		double [][] partialData = new double[rows][columns];

		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				partialData[i][j] = allData[i + startRow][j + startColumn];
			}
		}
		
		return partialData;
	}
	
	public static double[][] copySpecial(double[][] allData, int columnIndex, int columnValue) 
	{
		int rowCount = 0;
		
		for(int i = 0; i < allData.length; i++)
		{
			if(allData[i][columnIndex] == columnValue)
			{
				rowCount++;
			}
		}

		double [][] copyData = new double[rowCount][allData[0].length];
		int rowIndex = 0;

		for(int i = 0; i < allData.length; i++)
		{
			if(allData[i][columnIndex] == columnValue)
			{
				for(int j = 0; j < allData[0].length; j++)
				{
					copyData[rowIndex][j] = allData[i][j];
				}
				rowIndex++;
			}
		}
		
		return copyData;
	}
	
}
