package problemModule;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileStringSearch implements ProblemModule {
	
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String fileName;
	private List<String> searchSpace;
	private String targetString;
	private Integer lineNumber;
	private boolean targetFound;
	
	/*
	 * Default constructor
	 */
	public FileStringSearch(String file, String target) {
		id = 0;
		fileName = file;
		targetString = target; 
		targetFound = false;
	}
	
	private FileStringSearch(List<String> text, String target, Integer startingLineNumber,  Integer identifier) {
		id = identifier;
		searchSpace = text;
		targetString = target;
		lineNumber = startingLineNumber;
		targetFound = false;
	}

	@Override
	public ProblemModule[] breakDown(Integer nodes) {
		ProblemModule[] distributed = null;
		
		if(nodes > 0) {
			try {
				searchSpace = Files.readAllLines(Paths.get(fileName), Charset.defaultCharset());
				Integer length = searchSpace.size();
				Integer divisionLength = (int)(((double)length / nodes) + 0.5);
				Integer fromIndex = 0;
				Integer toIndex = divisionLength;
				Integer lineCounter = 0;
				
				if(nodes > length) {
					distributed = new FileStringSearch[length];
					toIndex = 1;
					divisionLength = 1;
				}
				else {
					if(divisionLength == 1) {
						distributed = new FileStringSearch[length];
					}
					else {
						distributed = new FileStringSearch[(int)(((double)length / divisionLength) + 0.5)];
					}
				}
				
				for(int  i = 0; i < distributed.length; i++) {
					List<String> temp = searchSpace.subList(fromIndex, toIndex);
					distributed[i] = new FileStringSearch(temp, targetString, fromIndex, i + 1);
					fromIndex = toIndex;
					toIndex = Math.min((toIndex + divisionLength), length);
					if((i + 1) == distributed.length) {
						if(lineCounter < length) {
							distributed[i] = new FileStringSearch(searchSpace.subList(fromIndex, length), targetString, fromIndex, i + 1);
						}
					}
					else {
						distributed[i] = new FileStringSearch(searchSpace.subList(fromIndex, toIndex), targetString, fromIndex, i + 1);
						lineCounter += divisionLength;
						fromIndex = toIndex;
						toIndex += divisionLength;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return distributed;
	}

	@Override
	public void Solve() {
		int length = searchSpace.size();
		
		for(Integer i = 0; i < length; i++) {
			String s = searchSpace.get(i);
			Integer searchResult = s.indexOf(targetString);
			if(searchResult >= 0) {
				lineNumber++;
				targetFound = true;
				break;
			}
		}
	}

	@Override
	public void finalize(ProblemModule[] subproblems) {
		int lineFound = -1;
		ProblemModule[] sorted = sort(subproblems);
		
		for(ProblemModule f : sorted) {
			if(f instanceof FileStringSearch) {
				if(((FileStringSearch) f).getTargetFound()) {
					lineFound = ((FileStringSearch) f).getLineNumber();
					targetFound = true;
					break;
				}
			}
		}
		
		if(targetFound) {
			System.out.println("String \"" + targetString + "\" found on line: " + lineFound);
		}
		else {
			System.out.println("String not found in file: " + fileName);
		}
	}
	
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
	
	private void swap (ProblemModule[] list, int i, int j) {
		ProblemModule swap = list[i];
		
		list[i] = list[j];
		list[j] = swap;
	}
	
	public Integer getID() {
		return id;
	}
	
	public boolean getTargetFound() { 
		return targetFound;
	}
	
	public Integer getLineNumber() {
		return lineNumber;
	}

	@Override
	public Object TestSolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean TEQ(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void DelaySolve() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}
	
}