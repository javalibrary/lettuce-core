// Copyright (C) 2012 - Will Glozer.  All rights reserved.

package com.lambdaworks.redis;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.lambdaworks.redis.codec.Utf8StringCodec;

public class BitCommandTest extends AbstractCommandTest {
    protected RedisConnection<String, String> bitstring;

    @Before
    public final void openBitStringConnection() throws Exception {
        bitstring = (RedisConnection) client.connect(new BitStringCodec());
    }

    @After
    public final void closeBitStringConnection() throws Exception {
        bitstring.close();
    }

    @Test
    public void bitcount() throws Exception {
        assertEquals(0, (long) redis.bitcount(key));
        redis.setbit(key, 0, 1);
        redis.setbit(key, 1, 1);
        redis.setbit(key, 2, 1);
        assertEquals(3, (long) redis.bitcount(key));
        // assertEquals(2, (long) redis.bitcount(key, 1, 3));
        assertEquals(0, (long) redis.bitcount(key, 3, -1));
    }

    @Test
    public void bitpos() throws Exception {
        assertEquals(0, (long) redis.bitcount(key));
        redis.setbit(key, 0, 0);
        redis.setbit(key, 1, 1);

        assertEquals("00000010", bitstring.get(key));
        assertEquals(1, (long) redis.bitpos(key, true));
    }

    @Test
    public void bitposOffset() throws Exception {
        assertEquals(0, (long) redis.bitcount(key));
        redis.setbit(key, 0, 1);
        redis.setbit(key, 1, 1);
        redis.setbit(key, 2, 0);
        redis.setbit(key, 3, 0);
        redis.setbit(key, 4, 0);
        redis.setbit(key, 5, 1);

        assertEquals(1, (long) bitstring.getbit(key, 1));
        assertEquals(0, (long) bitstring.getbit(key, 4));
        assertEquals(1, (long) bitstring.getbit(key, 5));
        assertEquals("00100011", bitstring.get(key));
        assertEquals(2, (long) redis.bitpos(key, false, 0, 0));
    }

    @Test
    public void bitopAnd() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("bar", 1, 1);
        redis.setbit("baz", 2, 1);
        assertEquals(1, (long) redis.bitopAnd(key, "foo", "bar", "baz"));
        assertEquals(0, (long) redis.bitcount(key));
        assertEquals("00000000", bitstring.get(key));
    }

    @Test
    public void bitopNot() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("foo", 2, 1);
        assertEquals(1, (long) redis.bitopNot(key, "foo"));
        assertEquals(6, (long) redis.bitcount(key));
        assertEquals("11111010", bitstring.get(key));
    }

    @Test
    public void bitopOr() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("bar", 1, 1);
        redis.setbit("baz", 2, 1);
        assertEquals(1, (long) redis.bitopOr(key, "foo", "bar", "baz"));
        assertEquals("00000111", bitstring.get(key));
    }

    @Test
    public void bitopXor() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("bar", 0, 1);
        redis.setbit("baz", 2, 1);
        assertEquals(1, (long) redis.bitopXor(key, "foo", "bar", "baz"));
        assertEquals("00000100", bitstring.get(key));
    }

    @Test
    public void getbit() throws Exception {
        assertEquals(0, (long) redis.getbit(key, 0));
        redis.setbit(key, 0, 1);
        assertEquals(1, (long) redis.getbit(key, 0));
    }

    @Test
    public void setbit() throws Exception {
        assertEquals(0, (long) redis.setbit(key, 0, 1));
        assertEquals(1, (long) redis.setbit(key, 0, 0));
    }

    static class BitStringCodec extends Utf8StringCodec {
        @Override
        public String decodeValue(ByteBuffer bytes) {
            StringBuilder bits = new StringBuilder(bytes.remaining() * 8);
            while (bytes.remaining() > 0) {
                byte b = bytes.get();
                for (int i = 0; i < 8; i++) {
                    bits.append(Integer.valueOf(b >>> i & 1));
                }
            }
            return bits.toString();
        }
    }
}
