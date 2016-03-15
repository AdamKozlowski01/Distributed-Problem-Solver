package MVP.problemModule;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileStringSearch implements MVP.problemModule.ProblemModule {
	
	private static final long serialVersionUID = 5L;
	private Integer id;					// id of this problem module
	private String fileName;			// text file name
	private List<String> searchSpace;	// list of strings to search through
	private String targetString;		// string to search for
	private Integer lineNumber;			// counter for proper line number
	private boolean targetFound;		// boolean for quick result checking in finalize()
	
	/*
	 * Parent ProblemModule constructor. Initializes all fields and
	 * throws IOException if the file can't be found.
	 */
	public FileStringSearch(String file, String target) throws IOException {
		id = 0;
		fileName = file;
		lineNumber = 1;
		targetString = target; 
		searchSpace = Files.readAllLines(Paths.get(fileName), Charset.defaultCharset());
		targetFound = false;
	}
	
	/*
	 * Child ProblemModule constructor. Not really to be used outside the class.
	 * Does not need to throw IOException cause the searchSpace is only generated if
	 * the parent ProblemModule successfully read in an existing text file.
	 */
	private FileStringSearch(List<String> text, String target, Integer startingLineNumber,  Integer identifier) {
		id = identifier;
		searchSpace = text;
		targetString = target;
		lineNumber = startingLineNumber;
		targetFound = false;
	}

	/*
	 * Breakdown method. Returns array of ProblemModules to the server.
	 * Has a lot of validation and special cases such as:
	 * 1. When there are more nodes than lines in the file.
	 * 2. When there is only one node
	 * 3. When the length of each division 
	 */
	@Override
	public ProblemModule[] breakDown(Integer nodes) {
		ProblemModule[] distributed = null;
		
		if(nodes > 0) {	// if 0 nodes, return null, there's something wrong
			Integer length = searchSpace.size();
			Integer divisionLength = (int)(((double)length / nodes) + 0.5);	// number of each division
			Integer fromIndex = 0;				// starting index of the sublist created for child modules
			Integer toIndex = divisionLength;	// ending index of the sublist created for child modules
			Integer lineCounter = 0;			// 
			
			if(nodes > length) {	// more nodes than lines
				distributed = new FileStringSearch[length];
				toIndex = 1;
				divisionLength = 1;
			}
			else {	// less nodes than lines
				if(divisionLength == 1) {	// case when one node
					distributed = new FileStringSearch[length];
				}
				else {	// number of nodes is greater than 1
					distributed = new FileStringSearch[(int)(((double)length / divisionLength) + 0.5)];
				}
			}
			
			for(int  i = 0; i < distributed.length; i++) {	// for every available node
				if((i + 1) == distributed.length) {	// special case where 
					if(lineCounter < length) {
						List<String> temp2 = new ArrayList<String>(searchSpace.subList(fromIndex, length));
						distributed[i] = new FileStringSearch(temp2, targetString, fromIndex, i + 1);
					}
				}
				else {	// default case
					List<String> temp = new ArrayList<String>(searchSpace.subList(fromIndex, toIndex));
					distributed[i] = new FileStringSearch(temp, targetString, fromIndex, i + 1);
					lineCounter += divisionLength;
					fromIndex = toIndex;
					toIndex += divisionLength;
				}
			}
		}
		
		return distributed;	// returns null if some kind of error
	}
	
	/*
	 * Solve method. Goes through the entire searchSpace\list and uses the indexOf()
	 * function to get the index of targetString. If targetString is not found,
	 * indexOf() returns -1. Otherwise it returns the index where targetString is 
	 * located. Then the loop is exited cause we found targetString. Also keeps updates
	 * lineCounter so the result will be printed out correctly later.
	 */
	@Override
	public void Solve() {
		int length = searchSpace.size();
		
		for(Integer i = 0; i < length; i++) {
			String s = searchSpace.get(i);
			Integer searchResult = s.indexOf(targetString);
			if(searchResult >= 0) {
				targetFound = true;
				break;
			}
			lineNumber++;
		}
	}

	
	/*
	 * F
	 */
	@Override
	public void finalize(ProblemModule[] subproblems) {
		ProblemModule[] sorted = sort(subproblems);
		
		for(ProblemModule f : sorted) {
			if(f instanceof FileStringSearch) {
				if(((FileStringSearch) f).getTargetFound()) {
					lineNumber = ((FileStringSearch) f).getLineNumber();
					targetFound = true;
					break;
				}
			}
		}
	}
	
	/*
	 * Sorting method for finalize(). Takes an array of ProblemModules
	 * and returns a sorted array of them according to id. Essentially
	 * just insertion sort.
	 */
	private ProblemModule[] sort(ProblemModule[] list) {
		int length = list.length;
		ProblemModule[] sorted = new FileStringSearch[length];
		
		ProblemModule temp;
		
		for(int i = 1; i < length; i++) {
			if(list[i] instanceof FileStringSearch) {
				temp = list[i];
			}
			for(int j = i; j > 0; j--) {
				if((list[j - 1] instanceof FileStringSearch) && (list[j] instanceof FileStringSearch)) {
					if(((FileStringSearch) list[j - 1]).getID() > ((FileStringSearch) list[j]).getID()) {
						swap(list, j - 1, j);
					}
					else {
						break;
					}
				}
			}
		}
		
		return sorted;
	}
	
	/*
	 * Swap helper method for sort(). Swaps the elements in list
	 * at index i and index j.
	 */
	private void swap (ProblemModule[] list, int i, int j) {
		ProblemModule swap = list[i];
		
		list[i] = list[j];
		list[j] = swap;
	}
	
	/*
	 * Get method for id.
	 */
	public Integer getID() {
		return id;
	}
	
	/*
	 * Get method for targetFound.
	 */
	public boolean getTargetFound() { 
		return targetFound;
	}
	
	/*
	 * Get method for lineNumber.
	 */
	public Integer getLineNumber() {
		return lineNumber;
	}

	/*
	 * Solve method used in JUnit testing.
	 */
	@Override
	public Object TestSolver() {
		String testResult = "String not found in file: " + fileName;
		int length = searchSpace.size();
		
		for(Integer i = 0; i < length; i++) {
			String s = searchSpace.get(i);
			Integer searchResult = s.indexOf(targetString);
			if(searchResult >= 0) {
				targetFound = true;
				testResult = "String \"" + targetString + "\" found on line: " + lineNumber;
				break;
			}
			lineNumber++;
		}
		
		return testResult;
	}

	/*
	 * Tests if the string generated through the splitting is equal to the known answer.
	 * I think.
	 */
	@Override
	public boolean TEQ(Object TestResult) {
		if(TestResult instanceof String){
			return ((String) TestResult).equals((String) this.TestSolver());
			}
			return false;
	}

	@Override
	public void DelaySolve() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}
	
}