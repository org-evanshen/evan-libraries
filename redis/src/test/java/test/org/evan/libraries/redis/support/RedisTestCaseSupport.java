package test.org.evan.libraries.redis.support;


import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created on 2017/7/16.
 *
 * @author evan.shen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RedisTestBeansConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RedisTestCaseSupport {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
}
