package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

	
	File fileItself;
	TupleDesc tdItself;
	
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
    	fileItself = f;
    	tdItself = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
    	return fileItself;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
    	return fileItself.getAbsoluteFile().hashCode();
        // throw new UnsupportedOperationException("implement this");
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
    	return tdItself;
        // throw new UnsupportedOperationException("implement this");
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
    	RandomAccessFile file = null;
		byte[] tmpBytes = new byte[BufferPool.PAGE_SIZE];
		HeapPage tmpPage = null;
		try {
			file = new RandomAccessFile(getFile(), "r");
			file.seek(pid.pageNumber()*BufferPool.PAGE_SIZE);
			file.read(tmpBytes);
			file.close();
			tmpPage = new HeapPage((HeapPageId)pid, tmpBytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			//do nothing
			e.printStackTrace();
			return null;
		}
		return tmpPage;

    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
    	return (int) Math.ceil(fileItself.length()/BufferPool.PAGE_SIZE);
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
	public DbFileIterator iterator(TransactionId tid) {
		// some code goes here
		return new HeapFileIterator(tid, this);
	}

	public class HeapFileIterator implements DbFileIterator {
		private Iterator<Tuple> i;
		private TransactionId tranId;
		private int pgNum;
		private HeapFile f;

		public HeapFileIterator(TransactionId tid, HeapFile f) {
			this.tranId = tid;
			this.f = f;
		}

		@Override
		public void open() throws DbException, TransactionAbortedException {
			pgNum = 0;
			i = getTupleList(pgNum).iterator();
		}

		@Override
		public boolean hasNext() throws DbException,TransactionAbortedException {
			if (i == null) 
				return false;
			
			if (i.hasNext())
				return true;
			else if (pgNum < f.numPages() - 1) 
			{
				if (getTupleList(pgNum + 1).size() != 0) 
				{
					return true;
				} 
				else 
					return false;
			} 
			else 
				return false;
		}

		@Override
		public Tuple next() throws DbException, TransactionAbortedException,NoSuchElementException {
			if (i == null) 
				throw new NoSuchElementException("tuple is null");
		

			if (i.hasNext()) 
			{
				// there are tuples available on page
				Tuple t = i.next();
				return t;
			} 
			else if (!i.hasNext() && pgNum < f.numPages() - 1) 
			{
				pgNum++;
				i = getTupleList(pgNum).iterator();
				if (i.hasNext())
					return i.next();
				else 
				{
					throw new NoSuchElementException("No more Tuples");
				}
			} 
			else {
		// no more tuples on current page and no more pages in file
				throw new NoSuchElementException("No more Tuples");
			}

		}

		// Returns a list of tuples from page
		private List<Tuple> getTupleList(int pgNum)throws TransactionAbortedException, DbException {

			PageId pageId = new HeapPageId(f.getId(), pgNum);
			Page page = Database.getBufferPool().getPage(tranId, pageId,
			Permissions.READ_ONLY);
	
			List<Tuple> tupleList = new ArrayList<Tuple>();
	
			// get all tuples from the first page in the file
			HeapPage hp = (HeapPage) page;
			Iterator<Tuple> itr = hp.iterator();
			while (itr.hasNext()) {
				tupleList.add(itr.next());
			}
			return tupleList;
		}

		@Override
		public void rewind() throws DbException, TransactionAbortedException {
			close();
			open();
		}

		@Override
		public void close() {
			i = null;
		}
	}
}

