package com.jdkcn.jabber;

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

public class BlockingQueueTest {

	@Test
	public void testBlockingQueye() throws Exception {
		BlockingQueue<String> testQueue = new ArrayBlockingQueue<String>(5);
		testQueue.add("1");
		assertEquals(1, testQueue.size());
		testQueue.add("2");
		assertEquals(2, testQueue.size());
		testQueue.add("3");
		assertEquals(3, testQueue.size());
		testQueue.add("4");
		assertEquals(4, testQueue.size());
		testQueue.add("5");
		assertEquals(5, testQueue.size());
		if (testQueue.size() >= 5) {
			testQueue.poll();
		}
		testQueue.add("6");
		assertEquals(5, testQueue.size());
		assertTrue(testQueue.contains("5"));
	}
}
