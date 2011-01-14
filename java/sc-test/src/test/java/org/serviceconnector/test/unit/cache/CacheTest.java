/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright � 2010 STABILIT Informatik AG, Switzerland                  *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *  http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 *-----------------------------------------------------------------------------*/
package org.serviceconnector.test.unit.cache;

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.cache.Cache;
import org.serviceconnector.cache.CacheComposite;
import org.serviceconnector.cache.CacheException;
import org.serviceconnector.cache.CacheId;
import org.serviceconnector.cache.CacheManager;
import org.serviceconnector.cache.CacheMessage;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.registry.ServiceRegistry;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPPart;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.SessionService;
import org.serviceconnector.test.unit.SuperUnitTest;

/**
 * The Class CacheTest tests the core cache functionality.
 * 
 * @author ds
 */
public class CacheTest extends SuperUnitTest {

	private CacheManager cacheManager;

	/**
	 * Run before each test and setup the dummy environment (services and cache manager)<br/>
	 * 
	 * @throws Exception
	 * 
	 */
	@Before
	public void beforeOneTest() throws Exception {
		super.beforeOneTest();
		AppContext.setSCEnvironment(true);
		ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
		Service service = new SessionService("dummy");
		serviceRegistry.addService("dummy", service);
		service = new SessionService("dummy1");
		serviceRegistry.addService("dummy1", service);
		service = new SessionService("dummy2");
		serviceRegistry.addService("dummy2", service);
		cacheManager = new CacheManager();
		cacheManager.initialize();
	}

	/**
	 * Run after each test, destroy cache manager<br/>
	 */
	@After
	public void afterOneTest() {
		cacheManager.destroy();
	}

	/**
	 * Description: Simple cache write test.
	 * Write a message into the cache using a dummy id and nr and read the message from cache again, checking if both contents
	 * (body) equals. Verify if cache size is 1.<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_simpleCacheWriteTest() throws CacheException {
		Cache scmpCache = this.cacheManager.getCache("dummy");
		String stringWrite = "this is the buffer";
		byte[] buffer = stringWrite.getBytes();
		SCMPMessage scmpMessageWrite = new SCMPMessage(buffer);

		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		CacheId msgCacheId = scmpCache.putMessage(scmpMessageWrite);
		SCMPMessage scmpMessageRead = new SCMPMessage();
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, msgCacheId.getFullCacheId());
		CacheMessage cacheMessage = scmpCache.getMessage(scmpMessageRead.getCacheId());
		byte[] bufferRead = (byte[]) cacheMessage.getBody();
		String stringRead = new String(bufferRead);
		Assert.assertEquals(stringWrite, stringRead);
		// get composite cache of given id
		CacheComposite cacheComposite = scmpCache.getComposite(msgCacheId.getCacheId());
		int size = cacheComposite.getSize();
		Assert.assertEquals(1, size);
	}

	/**
	 * Description: Simple cache write test into two separate cache instances (Cache).
	 * Write the same message into two cache instances using dummy nr and id. Read both messages from its cache instance and check
	 * for equality.<br>
	 * 
	 * Expectation: passes
	 */
	@Test
	public void t02_duplicateCacheWriteTest() throws CacheException {
		Cache scmpCache1 = this.cacheManager.getCache("dummy1");
		Cache scmpCache2 = this.cacheManager.getCache("dummy2");
		String stringWrite = "this is the buffer";
		byte[] buffer = stringWrite.getBytes();
		SCMPMessage scmpMessageWrite = new SCMPMessage(buffer);

		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		CacheId msgCacheId1 = scmpCache1.putMessage(scmpMessageWrite);
		CacheId msgCacheId2 = scmpCache2.putMessage(scmpMessageWrite);
		SCMPMessage scmpMessageRead = new SCMPMessage();
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		CacheMessage cacheMessage1 = scmpCache1.getMessage(msgCacheId1);
		byte[] bufferRead1 = (byte[]) cacheMessage1.getBody();
		String stringRead1 = new String(bufferRead1);
		Assert.assertEquals(stringWrite, stringRead1);
		CacheMessage cacheMessage2 = scmpCache2.getMessage(msgCacheId2);
		byte[] bufferRead2 = (byte[]) cacheMessage2.getBody();
		String stringRead2 = new String(bufferRead2);
		Assert.assertEquals(stringWrite, stringRead2);
	}

	/**
	 * Description: Large message (parts) cache write test.
	 * Write 10 part messages into the cache using a dummy cache id and nr's. All messages belong to the same cache id building a
	 * tree. Inside the cache a composite node is created and 10 message instances were assigned to this composite node. This test
	 * reads the composite and tries to get all assigned part messages. Each part message will be identified by a concatenated key
	 * using format CACHE_ID/SEQUENCE NR. All messages bodies were tested for equality.<br>
	 * 
	 * Expectation: passes
	 */
	@Test
	public void t03_partSCMPCacheWriteTest() throws CacheException {
		Cache scmpCache = this.cacheManager.getCache("dummy");
		String stringWrite = "this is the part buffer nr = ";
		for (int i = 1; i <= 10; i++) {
			String partWrite = stringWrite + i;
			byte[] buffer = partWrite.getBytes();
			SCMPMessage scmpMessageWrite = null;
			if (i < 10) {
			   scmpMessageWrite = new SCMPPart();
			} else {
			   scmpMessageWrite = new SCMPMessage();			
			}
			scmpMessageWrite.setBody(buffer);
			scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, String.valueOf(1233 + i));
			scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
			scmpCache.putMessage(scmpMessageWrite);
		}
		// get composite cache of given id
		CacheComposite cacheComposite = scmpCache.getComposite("dummy.cache.id");
		int size = cacheComposite.getSize();
		Assert.assertEquals(10, size);
		for (int i = 1; i <= 11; i++) {
			String partWrite = stringWrite + i;
			SCMPMessage scmpMessageRead = new SCMPMessage();
			scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id/" + i);
			CacheMessage cacheMessage = scmpCache.getMessage(scmpMessageRead.getCacheId());
			if (cacheMessage == null) {
				if (i < 11) {
					Assert.fail("cacheMessage is null but should not");
					continue;
				}
				break;
			}
			byte[] bufferRead = (byte[]) cacheMessage.getBody();
			String stringRead = new String(bufferRead);
			Assert.assertEquals(partWrite, stringRead);
		}
	}

	/**
	 * Description: Huge message (parts) cache write test.<br>
	 * 
	 * Expectation: passes
	 * 
	 * @see CacheTest#testPartSCMPCacheWrite()
	 */
	@Test
	public void t04_largePartSCMPCacheWriteTest() throws CacheException {
		Cache scmpCache = this.cacheManager.getCache("dummy");
		String stringWrite = "this is the part buffer nr = ";
		for (int i = 1; i <= 10000; i++) {
			String partWrite = stringWrite + i;
			byte[] buffer = partWrite.getBytes();
			SCMPMessage scmpMessageWrite = null;
			if (i < 10000) {
			   scmpMessageWrite = new SCMPPart();
			} else {
			   scmpMessageWrite = new SCMPMessage();			
			}
			scmpMessageWrite.setBody(buffer);
			scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, String.valueOf(1233 + i));
			scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
			scmpCache.putMessage(scmpMessageWrite);
		}
		// get composite cache of given id
		CacheComposite cacheComposite = scmpCache.getComposite("dummy.cache.id");
		int size = cacheComposite.getSize();
		Assert.assertEquals(10000, size);
		for (int i = 1; i <= 10001; i++) {
			String partWrite = stringWrite + i;
			SCMPMessage scmpMessageRead = new SCMPMessage();
			scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id/" + i);
			CacheMessage cacheMessage = scmpCache.getMessage(scmpMessageRead.getCacheId());
			if (cacheMessage == null) {
				if (i < 10001) {
					Assert.fail("cacheMessage is null but should not");
					continue;
				}
				break;
			}
			byte[] bufferRead = (byte[]) cacheMessage.getBody();
			String stringRead = new String(bufferRead);
			Assert.assertEquals(partWrite, stringRead);
		}
	}

	/**
	 * Description: Large message (parts) cache write test using iterator.<br>
	 * 
	 * Expectation: passes
	 * 
	 * @see CacheTest#testPartSCMPCacheWrite() 
	 */
	@Test
	public void t05_partSCMPCacheWriteUsingIteratorTest() throws CacheException {
		Cache scmpCache = this.cacheManager.getCache("dummy");
		String stringWrite = "this is the part buffer nr = ";
		for (int i = 1; i <= 10; i++) {
			String partWrite = stringWrite + i;
			byte[] buffer = partWrite.getBytes();
			SCMPMessage scmpMessageWrite = null;
			if (i < 10) {
			   scmpMessageWrite = new SCMPPart();
			} else {
			   scmpMessageWrite = new SCMPMessage();			
			}
			scmpMessageWrite.setBody(buffer);
			scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, String.valueOf(1233 + i));
			scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
			scmpCache.putMessage(scmpMessageWrite);
		}
		// get composite cache of given id
		CacheComposite cacheComposite = scmpCache.getComposite("dummy.cache.id");
		int size = cacheComposite.getSize();
		Assert.assertEquals(10, size);
		Iterator<CacheMessage> cacheIterator = scmpCache.iterator("dummy.cache.id");
		int index = 0;
		while (cacheIterator.hasNext()) {
			index++;
			String partWrite = stringWrite + index;
			byte[] buffer = partWrite.getBytes();
			CacheMessage cacheMessage = cacheIterator.next();
			if (cacheMessage == null) {
				if (index < 11) {
					Assert.fail("cacheMessage is null but should not");
					continue;
				}
				break;
			}
			byte[] bufferRead = (byte[]) cacheMessage.getBody();
			String stringRead = new String(bufferRead);
			Assert.assertEquals(partWrite, stringRead);
		}
	}
}
