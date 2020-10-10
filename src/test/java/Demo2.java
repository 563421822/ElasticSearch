import com.alibaba.fastjson.JSON;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.Date;

public class Demo2 {
    @Test
    public void set() {
        Jedis jedis = new Jedis("47.112.147.214", 6379);
        jedis.set("aa", "bb");
        jedis.close();
        System.out.println("未报异常");
    }

    @Test
    public void get() {
//        连接Redis
        Jedis jedis = new Jedis("47.112.147.214", 6379);
        System.out.println(jedis.get("aa"));
//        释放资源
        jedis.close();
    }

    //存储对象,已byte[]形式存储在Redis中
    @Test
    public void setByteArray() {
//        连接Redis服务
        Jedis jedis = new Jedis("47.112.147.214", 6379);
//        准备key(String)-value(User)
        String key = "user";
        User value = new User(1, "张三", new Date());
//        将key和value转换为byte[]
        byte[] byteKey = SerializationUtils.serialize(key);
        byte[] byteValue = SerializationUtils.serialize(value);
        System.out.println(jedis.set(byteKey, byteValue));
        jedis.close();
    }

    @Test
    public void getByteArray() {
        Jedis jedis = new Jedis("47.112.147.214", 6379);
//        准备key
        String key = "user";
//        将key转换为byte数组
        byte[] byteKey = SerializationUtils.serialize(key);
//        jedis去Redis中获取value
        byte[] value = jedis.get(byteKey);
        User user = (User) SerializationUtils.deserialize(value);
        System.out.println(user);
        jedis.close();
    }

    @Test
    public void setString() {
//        连接Redis
        Jedis jedis = new Jedis("47.112.147.214", 6379);
//        准备key(String)-value(User)
        String stringUser = "stringUser";
        User value = new User(2, "wuwu", new Date());
        System.out.println(value);
//        使用fastJson将值转化为json字符串
        String s = JSON.toJSONString(value);
        System.out.println(s);
//        存储到Redis中
        System.out.println(jedis.set(stringUser, s));
        jedis.close();
    }

    //获取对象
    @Test
    public void getString() {
        //        连接Redis
        Jedis jedis = new Jedis("47.112.147.214", 6379);
//        准备key(String)-value(User)
        String stringUser = "stringUser";
        String s = jedis.get(stringUser);
        System.out.println(s);
        User user = JSON.parseObject(s, User.class);
        System.out.println(user);
        jedis.close();
    }

    @Test
    public void pool2() {
//        创建连接池配置信息
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(100);//连接池中最大的活跃数
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxWaitMillis(3000);//当连接池空了之后,多久没获取到Jedis对象,就超时
//        创建连接池
        JedisPool pool = new JedisPool(poolConfig, "47.112.147.214", 6379);
//        通过连接池获取jedis对象
        Jedis resource = pool.getResource();
//        操作
        String value = resource.get("stringUser");
        System.out.println("value = " + value);
//        释放资源
        resource.close();
    }

    @Test
    public void pipeline() {
//创建连接池
        JedisPool pool = new JedisPool("47.112.147.214", 6379);
        long start = System.currentTimeMillis();
//        获取一个连接对象
        Jedis jedis = pool.getResource();
//        执行incr -10000次
        for (int i = 0; i < 1000; i++) {
            jedis.incr("pp");
        }
//        释放资源
        jedis.close();
        System.out.println(System.currentTimeMillis() - start);
    }
    @Test
    public void testPipeline(){
        JedisPool pool = new JedisPool("47.112.147.214",6379);
        long start = System.currentTimeMillis();
//        获取一个连接对象
        Jedis jedis = pool.getResource();
//        创建管道
        Pipeline pipelined = jedis.pipelined();
//        执行incr-10000次放到管道中
        for (int i = 0; i < 10000; i++) {
            pipelined.incr("qq");
        }
//        执行命令
        System.out.println(pipelined.syncAndReturnAll());
        jedis.close();
        System.out.println(System.currentTimeMillis()-start);
    }
}