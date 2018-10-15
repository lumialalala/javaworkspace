package com.ict.mcg.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
	 private static JedisPool pool = null;
	    
	    /**
	     * 构建redis连接池
	     * 
	     * @param ip
	     * @param port
	     * @return JedisPool
	     */
	    private static JedisPool getPool() {
	        if (pool == null) {
	            JedisPoolConfig config = new JedisPoolConfig();
	            //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
	            //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
	            config.setMaxActive(50);
	            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
	            config.setMaxIdle(10);
	            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
	            config.setMaxWait(1000 * 20);
	            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
	            config.setTestOnBorrow(true);
	            config.setTestOnReturn(true);
	            
	            //目前redis只有一个服务器
				pool = new JedisPool(config, ParamUtil.REDIS_ADDRESS[0], ParamUtil.REDIS_PORT);
//	            pool = new JedisPool(config, ParamUtil.REDIS_ADDRESS[0], ParamUtil.REDIS_PORT, 100000,"Ictmcg!2014&2015&2016");
//	            pool = new JedisPool(config, ParamUtil.REDIS_ADDRESS[0], ParamUtil.REDIS_PORT, 100000);
	        }
	        return pool;
	    }
	    
	    private static Jedis getJedis() {
	    	Jedis jedis = null;
	    	int count = 0;
	    	do {
		    	try {
		    		pool = getPool();
		    		jedis = pool.getResource();
//		    		jedis.auth("Ictmcg!2014&2015&2016");
		    	} catch(Exception e) {
//		    		System.out.println(e.getMessage());
		    		e.printStackTrace();
		    		pool.returnBrokenResource(jedis);
		    	}
		    	
		    	count++;
	    	} while (jedis==null && count < 3);
	    
	    	return jedis;
	    }
	    
	    
	    public static byte[] hget(byte[] key, byte[] field) {
	    	byte[] data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
	        	
	        	if (!jedis.isConnected()) {
	        		jedis.connect();
	        	}
		        try {
		            data = jedis.hget(key, field);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Long hset(byte[] key, byte[] field, byte[] value) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		            data = jedis.hset(key, field, value);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Long hset(String key, String field, String value) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		            data = jedis.hset(key, field, value);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    //hmset
	    public static String hmset(byte[] key, Map<byte[], byte[]> hash) {
	    	String data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		            data = jedis.hmset(key, hash);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static List<byte[]> hmget(byte[] key, byte[]... field) {
	    	List<byte[]> data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		            data = jedis.hmget(key, field);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static boolean hexists(byte[] key, byte[] field) {
	    	boolean flag = false;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	flag = jedis.hexists(key, field);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return flag;
	    }
	    
	    public static boolean exists(byte[] key) {
	    	boolean flag = false;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	flag = jedis.exists(key);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return flag;
	    }
	    
	    public static Long del(byte[] key) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.del(key);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Set<byte[]> keys(byte[] pattern) {
	    	Set<byte[]> data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.keys(pattern);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Set<byte[]> hkeys(byte[] key) {
	    	Set<byte[]> data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.hkeys(key);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Long zremrangeByRank(String key, long start ,long end) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.zremrangeByRank(key, start, end);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Double zincrby(String key, double score ,String member) {
	    	Double data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.zincrby(key, score, member);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	return data;
	    }
	    
	    public static Set<String> zrange(String key, long start ,long end) {
	    	Set<String> data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.zrange(key, start, end);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Set<String> zrangeByScore(String key, double min ,double max) {
	    	Set<String> data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.zrangeByScore(key, min, max);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Long zrem(String key, String...members) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.zrem(key, members);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Long zcard(String key) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.zcard(key);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Long zadd(String key, double score, String member) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.zadd(key, score, member);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
	    
	    public static Long hdel(byte[] key, byte[]...fields) {
	    	Long data = null;
	    	Jedis jedis = getJedis();
	        if (jedis != null) {
		        try {
		        	data = jedis.hdel(key, fields);
		        } catch (Exception e) {
		            //释放redis对象
		            e.printStackTrace();
		        } finally {
		            //返还到连接池
		            pool.returnResource(jedis);
		        }
	        }
	    	
	    	return data;
	    }
}
