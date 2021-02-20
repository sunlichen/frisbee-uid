package com.sunlichen.frisbee;

import com.sunlichen.frisbee.utils.Uid;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;

/**
 * @author me@sunlichen.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
	private static final Logger log =  org.slf4j.LoggerFactory.getLogger(ApplicationTests.class);

	@LocalServerPort
	private int port;
	private URL base;

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("${frisbeeUid.startStamp}")
	private long startStamp ;               //系统初始运行配置的时间戳

	@Before
	public void setUp() throws Exception {
		String url = String.format("http://localhost:%d/", port);
		this.base = new URL(url);
	}

	@Test
	public void testGetQueueUid() {
		ResponseEntity<String> response = this.restTemplate.getForEntity(
				this.base.toString() + "/queue_uid", String.class, "");
		log.info("Queue UID:{},{}", response.getBody(),Uid.parseUID(Long.parseLong(response.getBody()),startStamp));
	}

	@Test
	public void testGetCurrentUid(){
		ResponseEntity<String> response = this.restTemplate.getForEntity(
				this.base.toString() + "/current_uid", String.class, "");
		log.info("Current UID:{},{}",response.getBody(), Uid.parseUID(Long.parseLong(response.getBody()),startStamp));
	}
	@Test
	public void testGetStatus(){
		ResponseEntity<String> response = this.restTemplate.getForEntity(
				this.base.toString() + "/status", String.class, "");
		log.info("Status:{}",response.getBody());
	}

}
